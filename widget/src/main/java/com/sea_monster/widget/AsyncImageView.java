/*
 * 文件名：	AsyncImageView.java
 * 创建日期：	2012-6-20
 */
package com.sea_monster.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;

import com.sea_monster.core.common.BackgroundThread;
import com.sea_monster.core.resource.ResourceManager;
import com.sea_monster.core.resource.model.CompressedResource;
import com.sea_monster.core.resource.model.Resource;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import uk.co.senab.bitmapcache.CacheableImageView;

/**
 * 版权所有 (c) 2012 北京新媒传信科技有限公司。 保留所有权利。<br>
 * 项目名： 飞信 - Android客户端<br>
 * 描述： <br>
 *
 * @author dragonj
 * @version 1.0
 * @since JDK1.5
 */
public class AsyncImageView extends CacheableImageView implements Observer {

    static final ExecutorService mMultiThreadExecutor;
    static final boolean DEBUG = true;
    static final String TAG = "AsyncImageView";

    static {
        int coreNum = Math.round(Runtime.getRuntime().availableProcessors());
        int threadNum = 1;
        switch (coreNum) {
            case 1:
                threadNum = coreNum;
                break;
            case 2:
                threadNum = coreNum;
                break;

            default:
                threadNum = 3;
                break;
        }
        mMultiThreadExecutor = Executors.newFixedThreadPool(threadNum, new PhotoThreadFactory());
    }

    @Override
    public void update(Observable observable, Object data) {

        if (mResource instanceof Resource) {
            Log.d(TAG, "update Resource");
            Resource resource = (Resource) data;
            if (data.equals(resource) || data.equals(resource.getOrientaion())) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        refreshResouce();
                    }
                });
            }
        } else if (mResource instanceof CompressedResource) {
            Log.d(TAG, "update CompressedResource");
            Resource resource = ((CompressedResource) data).getOriResource();
            if (data.equals(resource) || data.equals(resource.getOrientaion())) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        refreshResouce();
                    }
                });
            }
        }

    }

    final static class PhotoThreadFactory implements ThreadFactory {

        private final String mThreadName;

        public PhotoThreadFactory(String threadName) {
            mThreadName = threadName;
        }

        public PhotoThreadFactory() {
            this("Photo");
        }

        public Thread newThread(final Runnable r) {
            if (null != mThreadName) {
                return new Thread(r, mThreadName);
            } else {
                return new Thread(r);
            }
        }
    }

    /**
     * @param context
     */
    Resource mResource;
    Future<?> mCurrentRunnable;
    Drawable mDefaultDrawable;
    Runnable mAttachedRunnable;
    final static int STATUS_DISPLAY = 1;
    final static int STATUS_EMPTY = 0;
    boolean isAttached;

    private int status;

    public AsyncImageView(Context context) {
        super(context);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public Resource getResource() {
        return mResource;
    }

    public void setDefaultDrawable(Drawable defaultDrawable) {
        this.mDefaultDrawable = defaultDrawable;
    }

    public void setResource(Resource resource) {
        Log.d(TAG, "setResource:" + resource.toString());
        final Resource previous = getResource();
        this.mResource = resource;

        if (mResource == null) {
            status = STATUS_EMPTY;
            setImageDrawable(mDefaultDrawable);
            Log.d(TAG, "setDefaultDrawable");
            return;
        }

        if (!mResource.equals(previous)) {
            status = STATUS_EMPTY;
        }

        if (status == STATUS_EMPTY) {
            mAttachedRunnable = null;
            resetForRequest(true);
            if (ResourceManager.getInstance().containsInMemoryCache(mResource)) {
                Log.d(TAG, "containsInMemoryCache");
                final BitmapDrawable drawable = ResourceManager.getInstance().getDrawable(mResource);
                if (drawable != null) {
                    setImageDrawable(drawable);
                    status = STATUS_DISPLAY;
                } else {
                    setImageDrawable(mDefaultDrawable);
                }
            } else {
                mCurrentRunnable = mMultiThreadExecutor.submit(new PhotoLoadRunnable(this, ResourceManager.getInstance(), mResource));
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        ResourceManager.getInstance().deleteObserver(this);
        cancelRequest();
        super.onDetachedFromWindow();
        isAttached = false;
        mAttachedRunnable = null;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    protected void onAttachedToWindow() {
        ResourceManager.getInstance().addObserver(this);
        super.onAttachedToWindow();
        isAttached = true;
        if (mAttachedRunnable != null) {
            mAttachedRunnable.run();
            mAttachedRunnable = null;
        }
    }

    private void resetForRequest(final boolean clearDrawable) {
        cancelRequest();

        if (clearDrawable) {
            setImageDrawable(null);
        }
    }

    public void cancelRequest() {
        if (null != mCurrentRunnable) {
            mCurrentRunnable.cancel(true);
            mCurrentRunnable = null;
        }
    }

    public void refreshResouce() {
        setResource(mResource);
    }

    static final class PhotoLoadRunnable extends BackgroundThread {

        private final WeakReference<AsyncImageView> mImageView;
        private final ResourceManager mManager;
        private final Resource mResource;

        public PhotoLoadRunnable(AsyncImageView imageView, ResourceManager manager,
                                 Resource resource) {
            mImageView = new WeakReference<AsyncImageView>(imageView);
            mManager = manager;
            mResource = resource;
        }

        public void runImpl() {
            final AsyncImageView imageView = mImageView.get();
            if (null == imageView) {
                return;
            }
            Log.d(TAG, "runImpl");


            final BitmapDrawable diskDrawable = mManager.getDrawable(mResource);
            Log.d(TAG, "BitmapDrawable");
            if (null != diskDrawable) {
                Log.d(TAG, "BitmapDrawable not null");
                if (imageView.status == STATUS_EMPTY && imageView.getResource().equals(mResource)) {

                    imageView.post(new Runnable() {
                        public void run() {
                            Log.d(TAG, "post setBitmapDrawable");
                            imageView.setImageDrawable(diskDrawable);
                            imageView.status = STATUS_DISPLAY;
                        }
                    });
                } else {
                    imageView.mAttachedRunnable = new Runnable() {
                        public void run() {
                            Log.d(TAG, "mAttachedRunnable setBitmapDrawable");
                            imageView.setImageDrawable(diskDrawable);
                            imageView.status = STATUS_DISPLAY;
                        }
                    };
                }
            } else {
                Log.d(TAG, "BitmapDrawable null");
                if (imageView.isAttached) {
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "post setBitmapDefault");

                            imageView.setImageDrawable(imageView.mDefaultDrawable);
                        }
                    });
                } else {
                    imageView.mAttachedRunnable = new Runnable() {
                        public void run() {
                            Log.d(TAG, "mAttachedRunnable setBitmapDefault");
                            imageView.setImageDrawable(imageView.mDefaultDrawable);
                        }
                    };
                }
            }
        }
    }

}
