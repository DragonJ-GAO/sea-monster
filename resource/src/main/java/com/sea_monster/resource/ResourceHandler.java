package com.sea_monster.resource;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import com.sea_monster.cache.BaseCache;
import com.sea_monster.cache.BitmapCacheWrapper;
import com.sea_monster.cache.CacheableBitmapDrawable;
import com.sea_monster.cache.DiskCacheWrapper;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;
import com.sea_monster.network.NetworkManager;
import com.sea_monster.network.StoreStatusCallback;


/**
 * Created by DragonJ on 15/2/2.
 */
public class ResourceHandler extends Observable {

    static ResourceHandler sS;

    NetworkManager mNetworkManager;
    DiskCacheWrapper mDiskCache;
    BitmapCacheWrapper mBitmapCache;
    BaseCache mBaseCache;
    Map<String, IResourceHandler> mHandlerMap;
    Map<Resource, AbstractHttpRequest<File>> mRequestQueue;
    Context mContext;


    public static ResourceHandler getInstance() {
        return sS;
    }

    private ResourceHandler(Context context, String type) {
        NetworkManager.init(context);
        mContext = context;
        if (type == null)
            mBaseCache = new BaseCache.Builder().build(context);
        else
            mBaseCache = new BaseCache.Builder().setType(type).build(context);

        mRequestQueue = new HashMap<>();
    }

    private ResourceHandler(Context context, NetworkManager networkManager) {
        init(context);
        mNetworkManager = networkManager;

        if(mNetworkManager == null)
            mNetworkManager.init(context);
    }

    public final static class Builder {

        private BaseCache cache;

        private boolean enableBitmapCache;

        private int sizeLimit;

        private String type;

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder() {

        }

        public Builder setOutputSizeLimit(int size){
            sizeLimit = size;
            return this;
        }

        public ResourceHandler build(Context context) {
            if (sS != null)
                return sS;

            ResourceHandler cache = new ResourceHandler(context, type);

            if (enableBitmapCache) {
                cache.enableBitmapCache();

                if(sizeLimit>0)
                    cache.mBitmapCache.setSizeLimit(sizeLimit);
            }

            cache.init(context);



            sS = cache;
            return cache;
        }

        public Builder enableBitmapCache() {
            this.enableBitmapCache = true;
            return this;
        }
    }

    private void init(Context context) {

        if (mDiskCache == null)
            mDiskCache = new DiskCacheWrapper.Builder().setCache(mBaseCache).build();

        if (mHandlerMap == null) {
            mHandlerMap = new HashMap<>();
            mHandlerMap.put("*", new CachedResourceHandler(context, mDiskCache));
            mHandlerMap.put("image", new CachedImageResourceHandler(context, mBitmapCache));
        }

        mNetworkManager = NetworkManager.getInstance();
    }

    private void enableBitmapCache() {
        if (mBitmapCache == null)
            mBitmapCache = new BitmapCacheWrapper.Builder(mContext).setCache(mBaseCache).build();
    }

    public AbstractHttpRequest<File> requestResource(final Resource resource, final ResCallback callback) throws URISyntaxException {

        if (mRequestQueue.containsKey(resource))
            return mRequestQueue.get(resource);

        ResRequest resRequest = new ResRequest(this, resource) {
            @Override
            public void onComplete(AbstractHttpRequest<File> request, File obj) {
                if (callback != null)
                    callback.onComplete(request, obj);
                setChanged();
                mRequestQueue.remove(resource);
                ResourceHandler.this.notifyObservers(new RequestCallback(resource, true));
                Log.d("requestResource", obj.getPath());


            }

            @Override
            public void onFailure(AbstractHttpRequest<File> request, BaseException e) {
                if (callback != null)
                    callback.onFailure(request, e);
                setChanged();
                mRequestQueue.remove(resource);
                ResourceHandler.this.notifyObservers(new RequestCallback(resource, false));
                Log.d("requestResource", e.getMessage());
            }
        };
        AbstractHttpRequest<File> request = resRequest.obtainRequest();

        mRequestQueue.put(resource, request);
        mNetworkManager.requestAsync(request);
        return request;
    }

    public AbstractHttpRequest<File> requestResource(final Resource resource, final ResCallback callback, final StoreStatusCallback statusCallback) throws URISyntaxException {

        if (mRequestQueue.containsKey(resource))
            return mRequestQueue.get(resource);

        ResRequest resRequest = new ResRequest(this, resource, statusCallback) {
            @Override
            public void onComplete(AbstractHttpRequest<File> request, File obj) {
                if (callback != null)
                    callback.onComplete(request, obj);
                setChanged();
                mRequestQueue.remove(resource);
                ResourceHandler.this.notifyObservers(new RequestCallback(resource, true));
                Log.d("requestResource", obj.getPath());
            }

            @Override
            public void onFailure(AbstractHttpRequest<File> request, BaseException e) {
                if (callback != null)
                    callback.onFailure(request, e);
                setChanged();
                mRequestQueue.remove(resource);
                ResourceHandler.this.notifyObservers(new RequestCallback(resource, false));
                Log.d("requestResource", e.getMessage());
            }
        };
        AbstractHttpRequest<File> request = resRequest.obtainRequest();

        mRequestQueue.put(resource, request);
        mNetworkManager.requestAsync(request);
        return request;
    }

    public void cancel(Resource resource) {

        if (mRequestQueue.containsKey(resource)) {
            mNetworkManager.cancelRequest(mRequestQueue.get(resource));
        }
    }

    public AbstractHttpRequest<File> requestResource(Resource resource) throws URISyntaxException {
        return requestResource(resource, null);
    }

    public boolean contains(Resource resource) {
        if (resource == null || resource.getUri() == null)
            return false;

        if (mBitmapCache == null)
            return mDiskCache.contains(resource.getUri());

        return mBitmapCache.contains(resource.getUri());
    }

    public boolean containsInDiskCache(Resource resource) {
        if (resource == null || resource.getUri() == null)
            return false;
        return mDiskCache.contains(resource.getUri());
    }

    public boolean containsInMemoryCache(Resource resource) {
        if (resource == null || resource.getUri() == null)
            return false;

        if (mBitmapCache == null)
            return false;

        return mBitmapCache.containsInMemoryCache(resource.getUri());
    }

    public File getFile(Resource resource) {
        if (resource == null || resource.getUri() == null)
            return null;
        return mDiskCache.getFile(resource.getUri());
    }

    public CacheableBitmapDrawable getDrawable(Resource resource) {
        if (resource == null || resource.getUri() == null)
            return null;

        if (mBitmapCache == null)
            return null;

        return mBitmapCache.get(resource.getUri());
    }

    IResourceHandler getResourceHandler(HttpEntity entity) {
        if (entity.getContentType() == null) {
            return mHandlerMap.get("*");
        }

        String type = entity.getContentType().getValue();

        if (TextUtils.isEmpty(type)) {
            return mHandlerMap.get("*");
        }

        for (Map.Entry<String, IResourceHandler> entry : mHandlerMap.entrySet()) {
            if (type.startsWith(entry.getKey()))
                return entry.getValue();
        }

        return mHandlerMap.get("*");
    }

    public void put(Resource resource, InputStream stream) {
        mDiskCache.put(resource.getUri(), stream);
    }

    public void put(Resource resource, Bitmap bitmap) {
        mBitmapCache.put(resource.getUri(), bitmap);
    }

    public class RequestCallback {
        Resource resource;
        boolean result;

        public RequestCallback(Resource resource, boolean result) {
            this.resource = resource;
            this.result = result;
        }

        public Resource getResource() {
            return resource;
        }

        public boolean isSuccess() {
            return result;
        }
    }
}
