package com.sea_monster.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.BasicPooledConnAdapter;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
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
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sea_monster.common.PriorityRunnable;
import com.sea_monster.exception.BaseException;
import com.sea_monster.exception.HttpException;
import com.sea_monster.exception.InternalException;
import com.sea_monster.exception.PackException;
import com.sea_monster.exception.ParseException;

public class DefaultHttpHandler implements HttpHandler {
	DefaultHttpClient client;
	Map<Integer, PriorityRequestRunnable<?>> requestMap;

	ThreadPoolExecutor executor;
	Context context;

	public DefaultHttpHandler(Context context, ThreadPoolExecutor executor) {
		requestMap = new HashMap<Integer, PriorityRequestRunnable<?>>();
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

		PriorityRequestRunnable<T> runnable = new PriorityRequestRunnable<T>(request);

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
		PriorityRequestRunnable<?> request = requestMap.get(id);
		if (request != null) {
			if (request.getUriRequest() != null) {
				request.getUriRequest().abort();
			} else {
				executor.getQueue().remove(request);
				request.getRequest().cancelRequest(new InternalException("Request Canceled"));
			}
		}
	}

	@Override
	public void cancelRequest() {
		executor.getQueue().clear();

		List<PriorityRequestRunnable<?>> requests = new ArrayList<PriorityRequestRunnable<?>>();

		for (Entry<Integer, PriorityRequestRunnable<?>> entry : requestMap.entrySet()) {
			if (entry.getValue() != null) {
				requests.add(entry.getValue());
			}
		}
		for (PriorityRequestRunnable<?> request : requests) {
			if (request.getUriRequest() != null) {
				request.getUriRequest().abort();
			} else {
				request.getRequest().cancelRequest(new InternalException(InternalException.DISCARD_TASK, "Request Canceled"));
			}
		}

		requestMap.clear();
	}

	public class PriorityRequestRunnable<T> extends PriorityRunnable {
		private AbstractHttpRequest<T> request;
		private HttpUriRequest uriRequest;

		public PriorityRequestRunnable(AbstractHttpRequest<T> request) {
			super(request.getPriority());
			this.request = request;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {

			int currentNetworkType, port = 0;
			String host = null;

			try {
				uriRequest = request.obtainRequest();
			} catch (InternalException e) {
				request.onFailure(e);
				return;
			} catch (PackException e) {
				request.onFailure(e);
				return;
			}

			NetworkInfo networkInfo = ((ConnectivityManager) DefaultHttpHandler.this.context.getSystemService(Context.CONNECTIVITY_SERVICE))
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
			if (request.getResStream() != null && currentNetworkType == ConnectivityManager.TYPE_MOBILE) {
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

                request.getParser().onHeaderParsed(response.getAllHeaders());

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
						result = (T) request.getParser().parseGzip(response.getEntity(), request.getStatusCallback());
					} else {
						result = (T) request.getParser().parse(response.getEntity(), request.getStatusCallback());
					}
					request.onComplete(result);
				} else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY || response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                    Header locationHeader = response.getFirstHeader("location");
                    if (locationHeader != null) {
                        String location  = locationHeader.getValue();
                        System.out.println("The page was redirected to:" + location);
                        try {
                            request.setUri(new URI(location));
                            run();
                            return;
                        } catch (URISyntaxException e) {
                            request.onFailure(new HttpException(e));
                        }
                    } else {
                        System.err.println("Location field value is null.");
                        Log.e("HTTP", EntityUtils.toString(response.getEntity()));
                        response.getEntity().consumeContent();
                        InternalException exception = new InternalException(response.getStatusLine().getStatusCode(), uriRequest.getRequestLine().toString());

                        int statue = response.getStatusLine().getStatusCode();

                        request.onFailure(exception);
                    }
                }else {
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

		public AbstractHttpRequest<T> getRequest() {
			return request;
		}

		public HttpUriRequest getUriRequest() {
			return uriRequest;
		}

	}

	@Override
	public <T> T executeRequestSync(AbstractHttpRequest<T> request) throws BaseException {
		HttpUriRequest uriRequest;
		int currentNetworkType, port = 0;
		String host = null;
		T result = null;
		try {
			uriRequest = request.obtainRequest();
		} catch (InternalException e) {
			throw e;
		} catch (PackException e) {
			throw e;
		}

		NetworkInfo networkInfo = ((ConnectivityManager) DefaultHttpHandler.this.context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		if (networkInfo == null || networkInfo.getState() != NetworkInfo.State.CONNECTED) {
			throw new InternalException(InternalException.NETWORK_DISABLED, uriRequest.toString());
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
		if (request.getResStream() != null && currentNetworkType == ConnectivityManager.TYPE_MOBILE) {
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

			adapter = (BasicPooledConnAdapter) context.getAttribute(ExecutionContext.HTTP_CONNECTION);

            request.getParser().onHeaderParsed(response.getAllHeaders());

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

				if (codingHeader != null && codingHeader.getValue().equalsIgnoreCase("gzip")) {
					result = (T) request.getParser().parseGzip(response.getEntity(), request.getStatusCallback());
				} else {
					result = (T) request.getParser().parse(response.getEntity(), request.getStatusCallback());
				}
			} else {
				response.getEntity().consumeContent();
				InternalException exception = new InternalException(response.getStatusLine().getStatusCode(), uriRequest.getRequestLine().toString());

				int statue = response.getStatusLine().getStatusCode();

				throw exception;
			}

		} catch (ClientProtocolException e) {
			throw new HttpException(e);

		} catch (ConnectTimeoutException e) {
			throw new HttpException(e);

		} catch (ConnectException e) {
			throw new HttpException(e);

		} catch (SocketTimeoutException e) {
			throw new HttpException(e);

		} catch (SocketException e) {
			throw new HttpException(e);

		} catch (IOException e) {
			throw new HttpException(e);

		} catch (ParseException e) {
			throw e;

		} catch (InternalException e) {
			throw e;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new InternalException(InternalException.NETWORK_DISABLED, "NullPointerException");
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

		return result;
	}

}
