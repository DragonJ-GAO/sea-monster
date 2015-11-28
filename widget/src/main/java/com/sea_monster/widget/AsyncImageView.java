package com.sea_monster.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.sea_monster.cache.CacheableImageView;
import com.sea_monster.common.BackgroundThread;
import com.sea_monster.resource.ImageResource;
import com.sea_monster.resource.Resource;
import com.sea_monster.resource.ResourceHandler;

/**
 * Created by DragonJ on 15/2/4.
 */
public class AsyncImageView extends CacheableImageView implements Observer {

    static final ExecutorService mMultiThreadExecutor;
    static final boolean DEBUG = true;

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
        if (mResource == null)
            return;

        if (data instanceof ResourceHandler.RequestCallback) {
            ResourceHandler.RequestCallback callback = (ResourceHandler.RequestCallback) data;
            if (callback != null && callback.isSuccess())
                if (callback.getResource().getUri().equals(mResource.getOriginalUri())) {
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
    ImageResource mResource;
    Future<?> mCurrentRunnable;
    Drawable mDefaultDrawable;
    Runnable mAttachedRunnable;
    final static int STATUS_DISPLAY = 1;
    final static int STATUS_EMPTY = 0;
    boolean isAttached;
    private boolean isCircle;
    private int status;

    public AsyncImageView(Context context) {
        super(context);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);


        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AsyncImageView);
        int resId = a.getResourceId(R.styleable.AsyncImageView_defDrawable, 0);
        int shape = a.getInt(R.styleable.AsyncImageView_shape, 0);

        isCircle = shape == 1;

        if (resId != 0)
            mDefaultDrawable = getResources().getDrawable(resId);

        a.recycle();
    }

    public ImageResource getResource() {
        return mResource;
    }

    public void setDefaultDrawable(Drawable defaultDrawable) {
        if (defaultDrawable == null) {
            this.mDefaultDrawable = null;
            return;
        }

        if (isCircle && defaultDrawable instanceof BitmapDrawable) {
            this.mDefaultDrawable = new CircleBitmapDrawable(getResources(), ((BitmapDrawable) defaultDrawable).getBitmap());
        } else {
            this.mDefaultDrawable = defaultDrawable;
        }
    }


    public void setImageDrawable(Drawable drawable) {

        if (isCircle && drawable instanceof BitmapDrawable) {
            super.setImageDrawable(new CircleBitmapDrawable(getResources(), ((BitmapDrawable) drawable).getBitmap()));
        } else {
            super.setImageDrawable(drawable);
        }

    }


    public void clean() {
        this.mResource = null;
        status = STATUS_EMPTY;
        setImageDrawable(mDefaultDrawable);
    }

    public void setResource(ImageResource resource) {

        final Resource previous = getResource();
        this.mResource = resource;

        if (mResource == null) {
            status = STATUS_EMPTY;
            setImageDrawable(mDefaultDrawable);
            return;
        }

        if (!mResource.equals(previous)) {
            setImageDrawable(mDefaultDrawable);
            status = STATUS_EMPTY;
        }

        if (status == STATUS_EMPTY) {
            mAttachedRunnable = null;
            cancelRequest();
            if (mResource != null && mResource.getUri() != null && ResourceHandler.getInstance().containsInMemoryCache(mResource)) {
                final BitmapDrawable drawable = ResourceHandler.getInstance().getDrawable(mResource);
                if (drawable != null && drawable.getBitmap() != null) {
                    if (isCircle) {
                        setImageDrawable(new CircleBitmapDrawable(getResources(), drawable.getBitmap()));
                    } else {
                        setImageDrawable(drawable);
                        invalidate();
                    }
                    status = STATUS_DISPLAY;

                } else {
                    setImageDrawable(mDefaultDrawable);
                }
            } else {
                mCurrentRunnable = mMultiThreadExecutor.submit(new PhotoLoadRunnable(this, ResourceHandler.getInstance(), mResource));
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        ResourceHandler.getInstance().deleteObserver(this);
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
        ResourceHandler.getInstance().addObserver(this);
        super.onAttachedToWindow();
        isAttached = true;
        if (mAttachedRunnable != null) {
            mAttachedRunnable.run();
            mAttachedRunnable = null;
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
        private final ResourceHandler mHandler;
        private final ImageResource mResource;

        public PhotoLoadRunnable(AsyncImageView imageView, ResourceHandler handler, final ImageResource resource) {
            mImageView = new WeakReference<AsyncImageView>(imageView);
            mHandler = handler;
            mResource = resource;
        }

        public void runImpl() {

            final AsyncImageView imageView = mImageView.get();

            if (null == imageView) {
                return;
            }
            BitmapDrawable diskDrawable = null;
            synchronized (mResource) {
                diskDrawable = mHandler.getDrawable(mResource);
            }
            if (diskDrawable != null && diskDrawable.getBitmap() != null) {


                if (imageView.status == STATUS_EMPTY && imageView.getResource().equals(mResource) && imageView.isAttached) {
                    final BitmapDrawable drawable = diskDrawable;
                    imageView.post(new Runnable() {
                        public void run() {
                            if (imageView.getResource() == null || !imageView.getResource().equals(mResource))
                                return;

                            if (imageView.isCircle)
                                imageView.setImageDrawable(new CircleBitmapDrawable(imageView.getResources(), drawable.getBitmap()));
                            else
                                imageView.setImageDrawable(drawable);

                            imageView.status = STATUS_DISPLAY;
                        }
                    });
                } else {
                    final BitmapDrawable drawable = diskDrawable;
                    imageView.mAttachedRunnable = new Runnable() {
                        public void run() {

                            if (imageView.getResource() == null || !imageView.getResource().equals(mResource))
                                return;

                            if (imageView.isCircle)
                                imageView.setImageDrawable(new CircleBitmapDrawable(imageView.getResources(), drawable.getBitmap()));
                            else
                                imageView.setImageDrawable(drawable);

                            imageView.status = STATUS_DISPLAY;
                        }
                    };
                }
            } else {
                if (imageView.isAttached) {
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageDrawable(imageView.mDefaultDrawable);
                        }
                    });
                } else {
                    imageView.mAttachedRunnable = new Runnable() {
                        public void run() {
                            imageView.setImageDrawable(imageView.mDefaultDrawable);
                        }
                    };
                }

                if (mResource.isThumb()) {
                    if (mResource.getOriginalUri().getScheme().equals("http") || mResource.getOriginalUri().getScheme().equals("https")) {
                        try {
                            mHandler.requestResource(new ImageResource(mResource.getOriginalUri()));
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    if (mResource.getUri().getScheme().equals("http") || mResource.getUri().getScheme().equals("https")) {
                        try {
                            mHandler.requestResource(mResource);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
        }
    }


}
