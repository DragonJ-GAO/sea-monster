package me.add1.resource;

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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import me.add1.cache.BaseCache;
import me.add1.cache.BitmapCacheWrapper;
import me.add1.cache.CacheableBitmapDrawable;
import me.add1.cache.DiskCacheWrapper;
import me.add1.common.RequestProcess;
import me.add1.exception.BaseException;
import me.add1.network.*;
import me.add1.network.apache.ApacheHttpHandler;
import me.add1.network.ok.OkHttpHandler;


/**
 * Created by DragonJ on 15/2/2.
 */
public class ResourceHandler extends Observable {

    static ResourceHandler sS;

    HttpHandler mHandler;
    DiskCacheWrapper mDiskCache;
    BitmapCacheWrapper mBitmapCache;
    BaseCache mBaseCache;
    Map<String, IResourceHandler> mHandlerMap;
    Map<Resource, AbstractHttpRequest<File>> mRequestQueue;
    CachedImageResourceHandler mImageCacheHandler;
    Context mContext;


    public static ResourceHandler getInstance() {
        return sS;
    }

    private ResourceHandler(Context context, String type) {
        mContext = context;
        if (type == null)
            mBaseCache = new BaseCache.Builder().build(context);
        else
            mBaseCache = new BaseCache.Builder().setType(type).build(context);

        mRequestQueue = new HashMap<>();
    }

    private ResourceHandler(Context context, HttpHandler handler) {
        init(context);
        mHandler = handler;
    }

    public final static class Builder {

        public final static int HTTP_APACHE = 0;
        public final static int HTTP_OKHTTP = 1;

        private BaseCache cache;

        private boolean enableBitmapCache;
        private int httpType;
        private int sizeLimit;

        private String type;

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder() {

        }

        public Builder setHttpType(int type) {
            this.httpType = type;
            return this;
        }

        public Builder setOutputSizeLimit(int size) {
            sizeLimit = size;
            return this;
        }

        public ResourceHandler build(Context context) {
            if (sS != null)
                return sS;

            ResourceHandler cache = new ResourceHandler(context, type);

            if (enableBitmapCache) {
                cache.enableBitmapCache();

                if (sizeLimit > 0)
                    cache.mBitmapCache.setSizeLimit(sizeLimit);
            }

            if (httpType == 0) {

                BlockingQueue<Runnable> queue = new PriorityBlockingQueue<Runnable>(Const.WORK_QUEUE_MAX_COUNT);

                ThreadFactory mThreadFactory = new ThreadFactory() {
                    private final AtomicInteger mCount = new AtomicInteger(1);

                    public Thread newThread(Runnable r) {
                        return new Thread(r, "ConnectTask #" + mCount.getAndIncrement());
                    }
                };

                ThreadPoolExecutor executor = new ThreadPoolExecutor(Const.DEF_THREAD_WORDER_COUNT, Const.MAX_THREAD_WORKER_COUNT, Const.CREATE_THREAD_TIME_SPAN,
                        TimeUnit.SECONDS, queue, mThreadFactory);

                executor.setRejectedExecutionHandler(new me.add1.network.DiscardOldestPolicy());

                cache.mHandler = new ApacheHttpHandler(context, executor);
            } else {
                cache.mHandler = new OkHttpHandler(context);
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

        if (mBitmapCache != null)
            mImageCacheHandler = new CachedImageResourceHandler(context, mBitmapCache);

        if (mHandlerMap == null) {
            mHandlerMap = new HashMap<>();
            mHandlerMap.put("*", new CachedResourceHandler(context, mDiskCache));
            mHandlerMap.put("image", mImageCacheHandler);
        }

    }

    private void enableBitmapCache() {
        if (mBitmapCache == null)
            mBitmapCache = new BitmapCacheWrapper.Builder(mContext).setCache(mBaseCache).build();
    }

    public synchronized AbstractHttpRequest<File> requestResource(final Resource resource, final ResCallback callback) throws URISyntaxException {

        if (mRequestQueue.containsKey(resource))
            return mRequestQueue.get(resource);

        ResRequest resRequest = new ResRequest(this, resource) {
            @Override
            public void onComplete(RequestProcess<File> request, File obj) {
                if (callback != null)
                    callback.onComplete(request, obj);
                setChanged();
                mRequestQueue.remove(resource);
                ResourceHandler.this.notifyObservers(new RequestCallback(resource, true));

            }

            @Override
            public void onFailure(RequestProcess<File> request, BaseException e) {
                if (callback != null)
                    callback.onFailure(request, e);
                setChanged();
                mRequestQueue.remove(resource);
                ResourceHandler.this.notifyObservers(new RequestCallback(resource, false));
            }
        };
        AbstractHttpRequest<File> request = resRequest.obtainRequest();

        mRequestQueue.put(resource, request);
        mHandler.executeRequest(request);
        return request;
    }

    public AbstractHttpRequest<File> requestResource(final Resource resource, final ResCallback callback, final StoreStatusCallback statusCallback) throws URISyntaxException {

        if (mRequestQueue.containsKey(resource))
            return mRequestQueue.get(resource);

        ResRequest resRequest = new ResRequest(this, resource, statusCallback) {
            @Override
            public void onComplete(RequestProcess<File> request, File obj) {
                if (callback != null)
                    callback.onComplete(request, obj);
                setChanged();
                mRequestQueue.remove(resource);
                ResourceHandler.this.notifyObservers(new RequestCallback(resource, true));
            }

            @Override
            public void onFailure(RequestProcess<File> request, BaseException e) {
                if (callback != null)
                    callback.onFailure(request, e);
                setChanged();
                mRequestQueue.remove(resource);
                ResourceHandler.this.notifyObservers(new RequestCallback(resource, false));
            }
        };
        AbstractHttpRequest<File> request = resRequest.obtainRequest();

        mRequestQueue.put(resource, request);
        mHandler.executeRequest(request);
        return request;
    }

    public void cancel(Resource resource) {

        if (mRequestQueue.containsKey(resource)) {
            mHandler.cancelRequest(mRequestQueue.get(resource));
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

    public CacheableBitmapDrawable getDrawable(ImageResource resource) {
        if (resource == null || resource.getUri() == null)
            return null;

        if (mBitmapCache == null)
            return null;

        return mImageCacheHandler.get(resource);
    }

    IResourceHandler getResourceHandler(String contentType) {

        if (TextUtils.isEmpty(contentType)) {
            return mHandlerMap.get("*");
        }

        for (Map.Entry<String, IResourceHandler> entry : mHandlerMap.entrySet()) {
            if (contentType.startsWith(entry.getKey()))
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
