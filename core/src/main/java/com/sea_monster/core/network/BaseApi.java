package com.sea_monster.core.network;

import android.content.Context;
import android.util.Log;

import com.sea_monster.core.exception.BaseException;

import org.apache.http.NameValuePair;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseApi {

    protected HttpHandler handler;
    protected Context context;

    public BaseApi(HttpHandler handler, Context context) {
        this.handler = handler;
        this.context = context;
        callMap = new HashMap<WeakReference<ApiCallback>, List<AbstractHttpRequest>>();
    }

    protected Map<WeakReference<ApiCallback>, List<AbstractHttpRequest>> callMap;

    protected final void recordRequest(WeakReference<ApiCallback> callback, AbstractHttpRequest request) {
        synchronized (callMap) {
            if (callMap.containsKey(callback)) {
                callMap.get(callback).add(request);
            } else {
                List<AbstractHttpRequest> requests = new ArrayList<AbstractHttpRequest>();
                requests.add(request);
                callMap.put(callback, requests);
            }
        }
    }

    protected final <T extends ApiCallback> void releaseRequest(WeakReference<T> callback, AbstractHttpRequest request) {
        synchronized (callMap) {
            if (callback == null)
                Log.d("Api", "Null");

            List<AbstractHttpRequest> callIds = callMap.get(callback);
            if (callIds != null && callIds.contains(request)) {
                int index = callIds.indexOf(request);
                callIds.remove(index);
                if (callIds.size() == 0) {
                    callMap.remove(callback);
                }
            }
        }
    }

    public void cancelReqeust(AbstractHttpRequest<?> callId) {
        handler.cancelRequest(callId);
    }

    public void cancelReqeust(ApiCallback callback) {
        List<AbstractHttpRequest> requests = null;
        synchronized (callMap) {
            for (WeakReference<ApiCallback> weakCallback : callMap.keySet()) {
                if (weakCallback.get() != null && weakCallback.get() == callback) {
                    requests = callMap.get(weakCallback);
                }
            }
        }
        if (requests != null && requests.size() > 0) {
            List<AbstractHttpRequest<?>> ids = new ArrayList<AbstractHttpRequest<?>>();
            for (AbstractHttpRequest callId : requests) {
                ids.add(callId);
            }
            for (AbstractHttpRequest request : requests) {
                handler.cancelRequest(request);
            }
        }
    }

    protected class DefaultApiReqeust<T extends Serializable> extends ApiReqeust<T> {
        private WeakReference<ApiCallback<T>> weakCallback;

        public DefaultApiReqeust(int method, URI uri, ApiCallback<T> callback) {
            super(method, uri);
            this.weakCallback = new WeakReference<ApiCallback<T>>(callback);
        }


        public DefaultApiReqeust(int method, URI uri, List<NameValuePair> params, ApiCallback<T> callback) {
            super(method, uri, params);
            this.weakCallback = new WeakReference<ApiCallback<T>>(callback);
        }

        public DefaultApiReqeust(int method, URI uri, List<NameValuePair> params, InputStream stream,
                                 ApiCallback<T> callback) {
            super(method, uri, params, stream);
            this.weakCallback = new WeakReference<ApiCallback<T>>(callback);
        }

        public DefaultApiReqeust(int method, URI uri, InputStream stream, ApiCallback<T> callback) {
            super(method, uri, stream);
            this.weakCallback = new WeakReference<ApiCallback<T>>(callback);
        }

        public DefaultApiReqeust(int method, URI uri, List<NameValuePair> params, InputStream stream, String resName,
                                 ApiCallback<T> callback) {
            super(method, uri, params, stream, resName);
            this.weakCallback = new WeakReference<ApiCallback<T>>(callback);
        }
        public DefaultApiReqeust(int method, URI uri, List<NameValuePair> params, InputStream stream, String name,String fileName,
                                 ApiCallback<T> callback) {
            super(method, uri, params, stream, name,fileName);
            this.weakCallback = new WeakReference<ApiCallback<T>>(callback);
        }

        public DefaultApiReqeust(int method, URI uri, InputStream stream, String resName, ApiCallback<T> callback) {
            super(method, uri, stream, resName);
            this.weakCallback = new WeakReference<ApiCallback<T>>(callback);
        }

        @Override
        public void onComplete(AbstractHttpRequest<T> request, T obj) {
            releaseRequest(weakCallback, request);
            if (weakCallback.get() != null && !weakCallback.enqueue())
                weakCallback.get().onComplete(request, obj);
        }

        @Override
        public void onFailure(AbstractHttpRequest<T> request, BaseException e) {
            releaseRequest(weakCallback, request);
            if (weakCallback.get() != null && !weakCallback.enqueue())
                weakCallback.get().onFailure(request,  e);
        }
    }
}
