package me.add1.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import me.add1.common.RequestProcess;
import me.add1.exception.BaseException;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseApi {

    protected Context context;
    protected HttpHandler handler;

    public BaseApi(HttpHandler handler, Context context) {
        this.handler = handler;
        this.context = context;
        callMap = new HashMap<>();
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

    protected final void recordRequest(ApiCallback callback, AbstractHttpRequest request){
        WeakReference<ApiCallback> weakReference = new WeakReference<ApiCallback>(callback);
        recordRequest(weakReference, request);
    }

    protected final <T extends ApiCallback> void releaseRequest(WeakReference<T> callback, RequestProcess request) {
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

    protected class DefaultApiReqeust<T extends Serializable> extends ApiRequest<T> {
        private WeakReference<ApiCallback<T>> weakCallback;

        public DefaultApiReqeust(String method, Uri uri, ApiCallback<T> callback) {
            super(method, uri, null);
            this.weakCallback = new WeakReference<ApiCallback<T>>(callback);
        }


        public DefaultApiReqeust(String method, Uri uri, List<ParamPair> params, ApiCallback<T> callback) {
            super(method, uri, params);
            this.weakCallback = new WeakReference<ApiCallback<T>>(callback);
        }


        @Override
        public void onComplete(RequestProcess<T> request, T obj) {
            releaseRequest(weakCallback, request);
            if (weakCallback.get() != null && !weakCallback.enqueue())
                weakCallback.get().onComplete(request, obj);
        }

        @Override
        public void onFailure(RequestProcess<T> request, BaseException e) {
            releaseRequest(weakCallback, request);
            if (weakCallback.get() != null && !weakCallback.enqueue())
                weakCallback.get().onFailure(request,  e);
        }

    }

    protected void execute(AbstractHttpRequest<?> abstractHttpRequest, ApiCallback<?> callback){
        recordRequest(callback, abstractHttpRequest);
        handler.executeRequest(abstractHttpRequest);
    }
}
