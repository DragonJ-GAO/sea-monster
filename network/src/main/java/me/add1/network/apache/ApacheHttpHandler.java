package me.add1.network.apache;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import me.add1.exception.HttpException;
import me.add1.exception.InternalException;
import me.add1.exception.PackException;
import me.add1.exception.ParseException;
import me.add1.network.AbstractHttpRequest;
import me.add1.network.Const;
import me.add1.network.HttpHandler;
import me.add1.network.HttpRequest;
import me.add1.network.NameValuePair;
import me.add1.network.ParamPair;
import me.add1.network.entity.MultipartEntity;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.BasicPooledConnAdapter;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ApacheHttpHandler implements HttpHandler {
    DefaultHttpClient client;
    Map<Integer, ApachePriorityRequestRunnable<?>> requestMap;

    ThreadPoolExecutor executor;
    Context context;

    public ApacheHttpHandler(Context context, ThreadPoolExecutor executor) {
        requestMap = new HashMap<Integer, ApachePriorityRequestRunnable<?>>();
        this.executor = executor;
        this.context = context;
        final SchemeRegistry supportedSchemes = new SchemeRegistry();

        final SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register(new Scheme("http", sf, 80));
        supportedSchemes.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        final HttpParams params = new BasicHttpParams();

        HttpClientParams.setRedirecting(params, false);
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        ConnManagerParams.setMaxTotalConnections(params, Const.HTTP_MAX_CONN_COUNT);
        ConnPerRouteBean bean = new ConnPerRouteBean(Const.HTTP_MAX_ROUTE_COUNT);
        ConnManagerParams.setMaxConnectionsPerRoute(params, bean);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpConnectionParams.setTcpNoDelay(params, true);

        HttpClientParams.setRedirecting(params, true);

        final ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, supportedSchemes);

        client = new DefaultHttpClient(ccm, params);
    }

    protected final HttpParams createHttpParams(int networkType) {
        final HttpParams params = new BasicHttpParams();

        switch (networkType) {

            case ConnectivityManager.TYPE_WIFI:
                HttpConnectionParams.setConnectionTimeout(params, Const.HTTP_WIFI_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, Const.HTTP_WIFI_SOCKET_TIMEOUT);
                break;
            case ConnectivityManager.TYPE_MOBILE:
                HttpConnectionParams.setConnectionTimeout(params, Const.HTTP_GPRS_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, Const.HTTP_GPRS_SOCKET_TIMEOUT);
                break;

            default:
                HttpConnectionParams.setConnectionTimeout(params, Const.HTTP_GPRS_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, Const.HTTP_GPRS_SOCKET_TIMEOUT);
                break;
        }

        return params;
    }

    protected final HttpParams createHttpParams(int networkType, String proxyUri, int port) {
        final HttpParams params = new BasicHttpParams();

        HttpHost host = new HttpHost(proxyUri, port);

        params.setParameter(ConnRouteParams.DEFAULT_PROXY, host);

        switch (networkType) {

            case ConnectivityManager.TYPE_WIFI:
                HttpConnectionParams.setConnectionTimeout(params, Const.HTTP_WIFI_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, Const.HTTP_WIFI_SOCKET_TIMEOUT);
                break;
            case ConnectivityManager.TYPE_MOBILE:
                HttpConnectionParams.setConnectionTimeout(params, Const.HTTP_GPRS_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, Const.HTTP_GPRS_SOCKET_TIMEOUT);
                break;

            default:
                HttpConnectionParams.setConnectionTimeout(params, Const.HTTP_GPRS_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, Const.HTTP_GPRS_SOCKET_TIMEOUT);
                break;
        }

        return params;
    }

    @Override
    public <T> int executeRequest(AbstractHttpRequest<T> request) {

        ApachePriorityRequestRunnable<T> runnable = new ApachePriorityRequestRunnable<T>(request);

        synchronized (requestMap) {
            requestMap.put(request.getCallId(), runnable);
        }

        if (executor.getQueue().size() >= Const.WORK_QUEUE_MAX_COUNT) {
            executor.getRejectedExecutionHandler().rejectedExecution(runnable, executor);

        } else {
            executor.execute(runnable);
        }

        return request.getCallId();
    }

    @Override
    public void cancelRequest(AbstractHttpRequest<?> id) {
        ApachePriorityRequestRunnable<?> request = requestMap.get(id);
        if (request != null) {
            if (request.getUriRequest() != null) {
                request.getUriRequest().abort();
            } else {
                executor.getQueue().remove(request);
                request.getRequest().onFailure(new InternalException("Request Canceled"));
            }
        }
    }

    @Override
    public void cancelRequest() {
        executor.getQueue().clear();

        List<ApachePriorityRequestRunnable<?>> requests = new ArrayList<ApachePriorityRequestRunnable<?>>();

        for (Entry<Integer, ApachePriorityRequestRunnable<?>> entry : requestMap.entrySet()) {
            if (entry.getValue() != null) {
                requests.add(entry.getValue());
            }
        }
        for (ApachePriorityRequestRunnable<?> request : requests) {
            if (request.getUriRequest() != null) {
                request.getUriRequest().abort();
            } else {
                request.getRequest().onFailure(new InternalException(InternalException.DISCARD_TASK, "Request Canceled"));
            }
        }

        requestMap.clear();
    }

    public class ApachePriorityRequestRunnable<T> extends PriorityRequestRunnable {
        private HttpUriRequest uriRequest;

        public ApachePriorityRequestRunnable(AbstractHttpRequest<T> request) {
            super(request);
            this.request = request;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {

            int currentNetworkType, port = 0;
            String host = null;

            try {
                uriRequest = obtainRequest(request);
            } catch (InternalException e) {
                request.onFailure(e);
                return;
            } catch (PackException e) {
                request.onFailure(e);
                return;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            NetworkInfo networkInfo = ((ConnectivityManager) ApacheHttpHandler.this.context.getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();

            if (networkInfo == null || networkInfo.getState() != NetworkInfo.State.CONNECTED) {
                request.onFailure(new InternalException(InternalException.NETWORK_DISABLED, uriRequest.toString()));
                return;
            }

            ProxySelector proxySelector = ProxySelector.getDefault();
            List<Proxy> proxies = proxySelector.select(uriRequest.getURI());
            if (proxies != null && proxies.size() > 0)
                for (Proxy proxy : proxies) {
                    if (proxy.address() != null && proxy.address() instanceof InetSocketAddress) {
                        InetSocketAddress socketAddress = (InetSocketAddress) proxy.address();
                        host = socketAddress.getHostName();
                        port = socketAddress.getPort();
                        break;
                    }

                }

            currentNetworkType = networkInfo.getType();

            if (currentNetworkType == ConnectivityManager.TYPE_MOBILE && host != null && host.length() > 0 && port > 0) {
                uriRequest.setParams(createHttpParams(currentNetworkType, host, port));
            } else {
                uriRequest.setParams(createHttpParams(currentNetworkType));
            }
            if (uriRequest instanceof HttpPost && ((HttpPost) uriRequest).getEntity() instanceof MultipartEntity && currentNetworkType == ConnectivityManager.TYPE_MOBILE) {
                uriRequest.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, Const.HTTP_GPRS_RES_SOCKET_TIMEOUT);
            }

            uriRequest.addHeader("Accept-Encoding", "gzip");

            HttpContext context = new BasicHttpContext();
            BasicPooledConnAdapter adapter = null;
            try {

                context.setAttribute(ClientContext.AUTHSCHEME_REGISTRY, client.getAuthSchemes());
                context.setAttribute(ClientContext.COOKIESPEC_REGISTRY, client.getCookieSpecs());
                context.setAttribute(ClientContext.COOKIE_STORE, client.getCookieStore());
                context.setAttribute(ClientContext.CREDS_PROVIDER, client.getCredentialsProvider());

                HttpResponse response = client.execute(uriRequest, context);


                NameValuePair[] headers = new NameValuePair[response.getAllHeaders().length];
                int i = 0;
                for (Header item : response.getAllHeaders()) {
                    headers[i++] = new NameValuePair(item.getName(), item.getValue());
                }

                request.getParser().onHeaderParsed(headers);

                adapter = (BasicPooledConnAdapter) context.getAttribute(ExecutionContext.HTTP_CONNECTION);

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    Header lenHeader = response.getFirstHeader(HTTP.CONTENT_LEN);
                    if (lenHeader != null) {
                        long length = Long.parseLong(lenHeader.getValue());
                        if (length > 20000) {
                            if (adapter instanceof BasicPooledConnAdapter) {

                                int timeout = 0;
                                if (currentNetworkType == ConnectivityManager.TYPE_WIFI) {
                                    timeout = (int) (length / Const.HTTP_WIFI_SPEED);
                                    timeout = timeout > Const.HTTP_WIFI_SOCKET_TIMEOUT ? timeout : Const.HTTP_WIFI_SOCKET_TIMEOUT;

                                } else {
                                    timeout = (int) (length / Const.HTTP_GPRS_SPEED);
                                    timeout = timeout > Const.HTTP_GPRS_SOCKET_TIMEOUT ? timeout : Const.HTTP_GPRS_SOCKET_TIMEOUT;
                                }
                                adapter.setSocketTimeout(timeout);
                            }
                        }
                    }

                    Header codingHeader = response.getFirstHeader(HTTP.CONTENT_ENCODING);
                    T result;

                    if (codingHeader != null && codingHeader.getValue().equalsIgnoreCase("gzip")) {
                        result = (T) request.getParser().parseGzip(response.getEntity().getContent(), request.getStatusCallback());
                    } else {
                        result = (T) request.getParser().parse(response.getEntity().getContent(), request.getStatusCallback());
                    }
                    request.onComplete(result);
                } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY || response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                    Header locationHeader = response.getFirstHeader("location");
                    if (locationHeader != null) {
                        String location = locationHeader.getValue();
                        System.out.println("The page was redirected to:" + location);
                        request.setUri(Uri.parse(location));
                        run();
                        return;
                    } else {
                        System.err.println("Location field value is null.");
                        Log.e("HTTP", EntityUtils.toString(response.getEntity()));
                        response.getEntity().consumeContent();
                        InternalException exception = new InternalException(response.getStatusLine().getStatusCode(), uriRequest.getRequestLine().toString());

                        int statue = response.getStatusLine().getStatusCode();

                        request.onFailure(exception);
                    }
                } else {
                    Log.e("HTTP", EntityUtils.toString(response.getEntity()));
                    response.getEntity().consumeContent();
                    InternalException exception = new InternalException(response.getStatusLine().getStatusCode(), uriRequest.getRequestLine().toString());

                    int statue = response.getStatusLine().getStatusCode();

                    request.onFailure(exception);
                }

            } catch (ClientProtocolException e) {
                request.onFailure(new HttpException(e));

            } catch (ConnectTimeoutException e) {
                request.onFailure(new HttpException(e));
                // ToastUtils.showShortToast(null, "网络不给力");

            } catch (ConnectException e) {
                request.onFailure(new HttpException(e));
                // ToastUtils.showShortToast(null, "网络不给力");

            } catch (SocketTimeoutException e) {
                request.onFailure(new HttpException(e));
                // ToastUtils.showShortToast(null, "网络不给力");

            } catch (SocketException e) {
                request.onFailure(new HttpException(e));
                // ToastUtils.showShortToast(null, "网络不给力");

            } catch (IOException e) {
                request.onFailure(new HttpException(e));

            } catch (ParseException e) {
                request.onFailure(e);

            } catch (InternalException e) {
                int code = e.getGeneralCode();

                request.onFailure(e);
            } catch (NullPointerException e) {
                e.printStackTrace();
                request.onFailure(new InternalException(InternalException.NETWORK_DISABLED, "NullPointerException"));
            } catch (IllegalStateException e) {
                request.onFailure(new HttpException(e));
            } finally {
                synchronized (requestMap) {
                    requestMap.remove(request.getCallId());
                }
                if (adapter != null) {
                    adapter.releaseConnection();
                }
                client.getConnectionManager().closeIdleConnections(0, TimeUnit.SECONDS);
            }
        }


        public HttpUriRequest getUriRequest() {
            return uriRequest;
        }

    }

    private HttpUriRequest obtainRequest(AbstractHttpRequest request) throws InternalException, UnsupportedEncodingException, PackException {
        if (request.getMethod() == HttpRequest.GET_METHOD) {
            request.processReadyRequest(request);
            HttpGet get = new HttpGet();

            if (request.getAllHeaders() != null) {
                for (NameValuePair item : request.getAllHeaders())
                    get.addHeader(new BasicHeader(item.getName(), item.getValue()));
            }

            Uri uri = request.getUri();

            if (request.getAllParams() != null) {
                Uri.Builder builder = uri.buildUpon();

                for (ParamPair item : request.getAllParams())
                    builder.appendQueryParameter(item.getName(), String.valueOf(item.getValue()));

                uri = builder.build();
            }

            get.setURI(URI.create(uri.toString()));

            return get;
        } else {
            request.processReadyRequest(request);
            HttpPost post = new HttpPost(URI.create(request.getUri().toString()));

            if (request.getAllHeaders() != null) {
                for (NameValuePair item : request.getAllHeaders())
                    post.addHeader(new BasicHeader(item.getName(), item.getValue()));
            }

            if (request.getAllParams() != null) {
                ParamPair[] paramPairs = request.getAllParams();

                boolean isMulti = false;
                InputStream inputStream = null;


                List<org.apache.http.NameValuePair> params = new ArrayList<>();

                for (ParamPair item : paramPairs) {
                    if (item.getValue() instanceof InputStream) {
                        isMulti = true;
                        inputStream = (InputStream) item.getValue();
                    } else {
                        params.add(new BasicNameValuePair(item.getName(), String.valueOf(item.getValue())));
                    }
                }

                HttpEntity entity;

                if (isMulti) {
                    entity = new MultipartEntity(params, inputStream, Const.POST_NAME, Const.POST_FILE_NAME, HTTP.UTF_8);
                } else if (request.getPacker() != null) {
                    entity = new InputStreamEntity(request.getPacker().getContent(), request.getPacker().getLength());
                } else {
                    entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                }

                post.setEntity(entity);
            }
            return post;
        }
    }
}
