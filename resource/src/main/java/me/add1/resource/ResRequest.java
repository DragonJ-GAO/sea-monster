package me.add1.resource;


import android.net.Uri;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import me.add1.exception.BaseException;
import me.add1.network.AbstractHttpRequest;
import me.add1.network.HttpRequest;
import me.add1.network.StoreStatusCallback;

public abstract class ResRequest implements ResCallback {
    private Resource res;
    private ResourceHandler handler;
    private StoreStatusCallback storeStatusCallback;

    public ResRequest(ResourceHandler handler, Resource res) throws URISyntaxException {
        this(handler, res, null);
    }

    public ResRequest(ResourceHandler handler, Resource res, StoreStatusCallback callback) throws URISyntaxException {

        String resScheme = res.getUri().getScheme();

        if (!resScheme.equals("http") && !resScheme.equals("https"))
            throw new URISyntaxException(res.getUri().toString(), "scheme invilidate fail");

        this.handler = handler;
        storeStatusCallback = callback;
        this.res = res;
    }


    public StoreStatusCallback getStoreStatusCallback() {
        return storeStatusCallback;
    }

    public AbstractHttpRequest<File> obtainRequest() {

        final AbstractHttpRequest<File> request = new AbstractHttpRequest<File>(AbstractHttpRequest.GET_METHOD, Uri.parse(res.getUri().toString()), null) {

            @Override
            public void processReadyRequest(HttpRequest request) {

            }


            @Override
            public void onFailure(BaseException e) {
                ResRequest.this.onFailure(this, e);
            }

            @Override
            public void onComplete(File obj) {
                ResRequest.this.onComplete(this, obj);
            }
        };

        if (this.storeStatusCallback != null) {
            request.setStatusCallback(this.storeStatusCallback);
        }

        request.setParser(new ResParser(handler, res));

        return request;
    }
}
