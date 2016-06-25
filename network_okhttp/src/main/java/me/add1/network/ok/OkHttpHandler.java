package me.add1.network.ok;

import android.content.Context;
import android.net.Uri;

import me.add1.exception.BaseException;
import me.add1.exception.HttpException;
import me.add1.exception.InternalException;
import me.add1.exception.ParseException;
import me.add1.network.AbstractHttpRequest;
import me.add1.network.Const;
import me.add1.network.HttpHandler;
import me.add1.network.NameValuePair;
import me.add1.network.ParamPair;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by dragonj on 15/11/23.
 */
public class OkHttpHandler implements HttpHandler {

    OkHttpClient client;

    Map<Integer, Call> requestMap;

    public OkHttpHandler(Context context) {
        requestMap = new HashMap<>();
        client = new OkHttpClient();
        client.setConnectTimeout(Const.HTTP_GPRS_TIMEOUT, TimeUnit.MILLISECONDS);
        client.setWriteTimeout(Const.HTTP_GPRS_SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
        client.setReadTimeout(Const.HTTP_GPRS_SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> int executeRequest(final AbstractHttpRequest<T> abstractHttpRequest) {
        final Request request = obtainRequest(abstractHttpRequest);
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                requestMap.remove(abstractHttpRequest.getCallId());
                abstractHttpRequest.onFailure(new HttpException(e));
            }

            @Override
            public void onResponse(Response response) throws IOException {
                requestMap.remove(abstractHttpRequest.getCallId());

                if (response.isSuccessful()) {
                    try {
                        T t = (T) abstractHttpRequest.getParser().parse(response.body().byteStream(),abstractHttpRequest.getStatusCallback());
                        abstractHttpRequest.onComplete(t);
                    } catch (ParseException e) {
                        abstractHttpRequest.onFailure(e);
                    } catch (InternalException e) {
                        abstractHttpRequest.onFailure(e);
                    }
                } else if (response.isRedirect()) {
                    abstractHttpRequest.setUri(Uri.parse(response.message()));
                    abstractHttpRequest.onFailure(new HttpException(response.code(), response.message()));
                } else {
                    abstractHttpRequest.onFailure(new HttpException(response.message()));
                }
            }
        });

        requestMap.put(abstractHttpRequest.getCallId(), call);

        return abstractHttpRequest.getCallId();
    }

    @Override
    public void cancelRequest(AbstractHttpRequest<?> request) {
        synchronized (requestMap) {
            if (requestMap.containsKey(request.getCallId())) {
                if (!requestMap.get(request.getCallId()).isCanceled())
                    requestMap.get(request.getCallId()).cancel();
                requestMap.remove(request.getCallId());
            }
        }
    }

    @Override
    public void cancelRequest() {

        synchronized (requestMap){
            if(requestMap.size() == 0)
                return;

            Set<Map.Entry<Integer, Call>> items = requestMap.entrySet();
            for (Map.Entry<Integer, Call> item : items){
                item.getValue().cancel();
            }

            requestMap.clear();
        }
    }

    private Request obtainRequest(AbstractHttpRequest absHttpRequest) {
        Request.Builder requestBuilder = null;


        absHttpRequest.processReadyRequest(absHttpRequest);
        ParamPair[] paramPairs = absHttpRequest.getAllParams();

        if (absHttpRequest.getMethod() == AbstractHttpRequest.GET_METHOD) {
            if (paramPairs != null) {
                Uri.Builder builder = absHttpRequest.getUri().buildUpon();
                for (ParamPair item : paramPairs) {
                    builder.appendQueryParameter(item.getName(), String.valueOf(item.getValue()));
                }
                requestBuilder = new Request.Builder().url(builder.build().toString());
            } else {
                requestBuilder = new Request.Builder().url(absHttpRequest.getUri().toString());
            }
        } else {

            boolean isMulti = false;

            for (ParamPair item : paramPairs) {
                if (item.getValue() instanceof InputStream) {
                    isMulti = true;
                }
            }

            if (isMulti) {
                MultipartBuilder builder = new MultipartBuilder();
                for (ParamPair item : paramPairs) {
                    if (item.getValue() instanceof InputStream) {
                        final InputStream stream = (InputStream) item.getValue();
                        builder.addFormDataPart(item.getName(), Const.POST_FILE_NAME, new RequestBody() {
                            @Override
                            public MediaType contentType() {
                                return MediaType.parse("image/*");
                            }

                            @Override
                            public void writeTo(BufferedSink sink) throws IOException {
                                Source source = null;
                                try {
                                    source = Okio.source(stream);
                                    sink.writeAll(source);
                                } finally {
                                    Util.closeQuietly(source);
                                }
                            }
                        });

                    } else {
                        builder.addFormDataPart(item.getName(), String.valueOf(item.getValue()));
                    }

                }
                requestBuilder = new Request.Builder().url(absHttpRequest.getUri().toString()).post(builder.build());

            } else {
                FormEncodingBuilder builder = new FormEncodingBuilder();

                for (ParamPair item : paramPairs) {
                    builder.add(item.getName(), String.valueOf(item.getValue()));
                }
                requestBuilder = new Request.Builder().url(absHttpRequest.getUri().toString()).post(builder.build());
            }
        }

        NameValuePair[] headers = absHttpRequest.getAllHeaders();
        if (headers == null)
            return requestBuilder.build();

        for (NameValuePair item : headers) {
            requestBuilder.addHeader(item.getName(), item.getValue());
        }

        return requestBuilder.build();
    }
}
