package me.add1.cache;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Created by DragonJ on 15/1/31.
 */
public class BitmapCacheWrapper extends BaseCache {


    protected BaseCache mCache;
    protected Resources mResources;
    protected ContentResolver mContentResolver;
    protected BitmapMemoryLruCache mMemoryCache;
    protected int mSizeLimit;

    protected RecyclePolicy mRecyclePolicy;
    protected final int CACHE_STORE_LIMIT = 1048576;

    BitmapCacheWrapper(Context context) {
        if (context == null)
            throw new IllegalArgumentException("context must not null.");
        mResources = context.getResources();
        mContentResolver = context.getContentResolver();
    }

    void setMemoryCache(BitmapMemoryLruCache memoryCache) {
        mMemoryCache = memoryCache;
        mRecyclePolicy = memoryCache.getRecyclePolicy();
    }

    public void setCache(BaseCache cache) {
        this.mCache = cache;
    }

    /**
     * Returns whether any of the enabled caches contain the specified URL. <p/> If you have the
     * disk cache enabled, you should not call this method from main/UI thread.
     *
     * @param uri the URI to search for.
     * @return {@code true} if any of the caches contain the specified URL, {@code false}
     * otherwise.
     */
    public boolean contains(Uri uri) {
        return containsInMemoryCache(uri) || containsInDiskCache(uri);
    }

    /**
     * Returns whether the Disk Cache contains the specified URL. You should not call this method
     * from main/UI thread.
     *
     * @param uri the URI to search for.
     * @return {@code true} if the Disk Cache is enabled and contains the specified URL, {@code
     * false} otherwise.
     */
    public boolean containsInDiskCache(Uri uri) {
        if (null == mCache || uri == null)
            return false;

        return mCache.contains(uri);
    }

    /**
     * Returns whether the Memory Cache contains the specified URL. This method is safe to be called
     * from the main thread.
     *
     * @param uri the URI to search for.
     * @return {@code true} if the Memory Cache is enabled and contains the specified URL, {@code
     * false} otherwise.
     */
    public boolean containsInMemoryCache(Uri uri) {

        return null != uri && null != mMemoryCache && null != mMemoryCache.get(uri.toString());
    }

    public void setSizeLimit(int sizeLimit) {
        this.mSizeLimit = sizeLimit;
    }

    /**
     * Returns the value for {@code url}. This will check all caches currently enabled. <p/> If you
     * have the disk cache enabled, you should not call this method from main/UI thread.
     *
     * @param url - String representing the URL of the image
     */
    public CacheableBitmapDrawable get(Uri url) {
        return get(url, null);
    }

    /**
     * Returns the value for {@code url}. This will check all caches currently enabled. <p/> If you
     * Returns the value for {@code url}. This will check all caches currently enabled. <p/> If you
     * have the disk cache enabled, you should not call this method from main/UI thread.
     *
     * @param uri        - String representing the URI of the image
     * @param decodeOpts - Options used for decoding the contents from the disk cache only.
     */
    public CacheableBitmapDrawable get(Uri uri, BitmapFactory.Options decodeOpts) {
        CacheableBitmapDrawable result;

        if (uri == null)
            return null;

        // First try Memory Cache
        result = getFromMemoryCache(uri);

        if (null == result || !isMainThread()) {
            if (uri.getScheme().equals("file")) {
                Log.d("BitmapCacheWrapper", "file:" + uri.toString());
                result = getFromDisk(uri, decodeOpts);
            } else if (uri.getScheme().equals("content")) {
                Log.d("BitmapCacheWrapper", "content:" + uri.toString());
                result = getFromContent(uri, decodeOpts);
            } else {
                Log.d("BitmapCacheWrapper", "disk:" + uri.toString());
                result = getFromDiskCache(uri, decodeOpts);
            }
        }

        return result;
    }

    /**
     * Returns the value for {@code url} in the disk cache only. You should not call this method
     * from main/UI thread. <p/> If enabled, the result of this method will be cached in the memory
     * cache. <p /> Unless you have a specific requirement to only query the disk cache, you should
     * call {@link #get(Uri)} instead.
     *
     * @param uri        - String representing the URI of the image
     * @param decodeOpts - Options used for decoding the contents from the disk cache.
     * @return Value for {@code url} from disk cache, or {@code null} if the disk cache is not
     * enabled.
     */
    public CacheableBitmapDrawable getFromDiskCache(final Uri uri,
                                                    final BitmapFactory.Options decodeOpts) {
        CacheableBitmapDrawable result = null;

        if (null != mCache && uri != null) {

            File file = mCache.getFile(uri);

            if (file == null)
                return null;

            result = decodeBitmap(new FileInputStreamProvider(file), uri.toString(), decodeOpts);

            if (null != result) {
                if (null != mMemoryCache) {
                    mMemoryCache.put(result);
                }
            } else {
                mCache.remove(uri);
            }
        }

        return result;
    }

    public CacheableBitmapDrawable getFromContent(final Uri uri, final BitmapFactory.Options decodeOpts) {
        CacheableBitmapDrawable result = null;

        if (uri != null) {

            result = decodeBitmap(new ContentStreamProvider(mContentResolver, uri), uri.toString(), decodeOpts, mSizeLimit);
/**Content数据不应该插入缓冲*/
//            if (null != result) {
////                if (null != mMemoryCache) {
////                    mMemoryCache.put(result);
////                }
//            } else {
//                mCache.remove(uri);
//            }
        }

        return result;
    }

    public CacheableBitmapDrawable getFromDisk(final Uri uri,
                                               final BitmapFactory.Options decodeOpts) {
        CacheableBitmapDrawable result = null;

        if (uri != null) {

            File file = new File(uri.getPath());

            if (!file.exists())
                return null;

            result = decodeBitmap(new FileInputStreamProvider(file), uri.toString(), decodeOpts);
            /**Disk数据不应该插入缓冲*/
//            if (null != result) {
//                if (null != mMemoryCache) {
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
//                        if (result.getBitmap().getRowBytes() * result.getBitmap().getHeight() < CACHE_STORE_LIMIT)
//                            mMemoryCache.put(result);
//                    } else {
//                        if (result.getBitmap().getByteCount() < CACHE_STORE_LIMIT)
//                            mMemoryCache.put(result);
//                    }
//
//                }
//
//            } else {
//                mCache.remove(uri);
//            }
        }

        return result;
    }

    public File getFileFromDiskCache(final Uri uri) {
        if (null == mCache || uri == null)
            return null;

        return mCache.getFile(uri);

    }

    /**
     * Returns the value for {@code url} in the memory cache only. This method is safe to be called
     * from the main thread. <p /> You should check the result of this method before starting a
     * threaded call.
     *
     * @param uri - String representing the URI of the image
     * @return Value for {@code url} from memory cache, or {@code null} if the disk cache is not
     * enabled.
     */
    public CacheableBitmapDrawable getFromMemoryCache(final Uri uri) {
        CacheableBitmapDrawable result = null;

        if (null != mMemoryCache && null != uri) {
            synchronized (mMemoryCache) {
                result = mMemoryCache.get(uri.toString());

                // If we get a value, but it has a invalid bitmap, remove it
                if (null != result && !result.isBitmapValid()) {
                    mMemoryCache.remove(uri.toString());
                    result = null;
                }
            }
        }

        return result;
    }

    /**
     * @return true if the Memory Cache is enabled.
     */
    public boolean isMemoryCacheEnabled() {
        return null != mMemoryCache;
    }

    /**
     * Caches {@code bitmap} for {@code url} into all enabled caches. If the disk cache is enabled,
     * the bitmap will be compressed losslessly. <p/> If you have the disk cache enabled, you should
     * not call this method from main/UI thread.
     *
     * @param uri    - String representing the URI of the image.
     * @param bitmap - Bitmap which has been decoded from {@code url}.
     * @return CacheableBitmapDrawable which can be used to display the bitmap.
     */
    public CacheableBitmapDrawable put(final Uri uri, final Bitmap bitmap) {
        return put(uri, bitmap, Bitmap.CompressFormat.JPEG, 80);
    }

    /**
     * Caches {@code bitmap} for {@code url} into all enabled caches. If the disk cache is enabled,
     * the bitmap will be compressed with the settings you provide.
     * <p/> If you have the disk cache enabled, you should not call this method from main/UI thread.
     *
     * @param uri             - String representing the URI of the image.
     * @param bitmap          - Bitmap which has been decoded from {@code url}.
     * @param compressFormat  - Compression Format to use
     * @param compressQuality - Level of compression to use
     * @return CacheableBitmapDrawable which can be used to display the bitmap.
     * @see android.graphics.Bitmap#compress(android.graphics.Bitmap.CompressFormat, int, java.io.OutputStream)
     */
    public CacheableBitmapDrawable put(final Uri uri, final Bitmap bitmap,
                                       Bitmap.CompressFormat compressFormat, int compressQuality) {

        if (uri == null)
            return null;

        CacheableBitmapDrawable d = new CacheableBitmapDrawable(uri.toString(), mResources, bitmap,
                mRecyclePolicy, CacheableBitmapDrawable.SOURCE_UNKNOWN);

        if (null != mMemoryCache) {
            mMemoryCache.put(d);
        }

        if (null != mCache) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(compressFormat, compressQuality, stream);
            mCache.put(uri, new ByteArrayInputStream(stream.toByteArray()));
        }

        return d;
    }

    /**
     * Caches resulting bitmap from {@code inputStream} for {@code url} into all enabled caches.
     * This version of the method should be preferred as it allows the original image contents to be
     * cached, rather than a re-compressed version. <p /> The contents of the InputStream will be
     * copied to a temporary file, then the file will be decoded into a Bitmap. Providing the decode
     * worked: <ul> <li>If the memory cache is enabled, the decoded Bitmap will be cached to
     * memory.</li> <li>If the disk cache is enabled, the contents of the original stream will be
     * cached to disk.</li> </ul> <p/> You should not call this method from the main/UI thread.
     *
     * @param uri         - String representing the URI of the image
     * @param inputStream - InputStream opened from {@code url}
     * @return CacheableBitmapDrawable which can be used to display the bitmap.
     */
    public void put(final Uri uri, final InputStream inputStream) {
        put(uri, inputStream, null);
    }

    /**
     * Caches resulting bitmap from {@code inputStream} for {@code url} into all enabled caches.
     * This version of the method should be preferred as it allows the original image contents to be
     * cached, rather than a re-compressed version. <p /> The contents of the InputStream will be
     * copied to a temporary file, then the file will be decoded into a Bitmap, using the optional
     * <code>decodeOpts</code>. Providing the decode worked: <ul> <li>If the memory cache is
     * enabled, the decoded Bitmap will be cached to memory.</li> <li>If the disk cache is enabled,
     * the contents of the original stream will be cached to disk.</li> </ul> <p/> You should not
     * call this method from the main/UI thread.
     *
     * @param uri         - String representing the URL of the image
     * @param inputStream - InputStream opened from {@code url}
     * @param decodeOpts  - Options used for decoding. This does not affect what is cached in the
     *                    disk cache (if enabled).
     */
    public void put(final Uri uri, final InputStream inputStream,
                    final BitmapFactory.Options decodeOpts) {

        if (uri == null)
            return;

        mCache.put(uri, inputStream);

        CacheableBitmapDrawable d = null;

        File file = mCache.getFile(uri);

        if (null != file) {
            // Try and decode File
            d = decodeBitmap(new FileInputStreamProvider(file), uri.toString(), decodeOpts);

            if (d != null) {
                if (null != mMemoryCache) {
                    d.setCached(true);
                    mMemoryCache.put(d.getUrl(), d);
                }
            }
        }

    }

    /**
     * Removes the entry for {@code url} from all enabled caches, if it exists. <p/> If you have the
     * disk cache enabled, you should not call this method from main/UI thread.
     */
    public void remove(Uri uri) {
        if (null != mMemoryCache && null != uri) {
            mMemoryCache.remove(uri.toString());
        }

        if (null != mCache) {
            mCache.remove(uri);
        }
    }

    @Override
    public File getFile(Uri uri) {
        if (null != mCache) {
            return mCache.getFile(uri);
        }
        return null;
    }

    @Override
    public InputStream getInputStream(Uri uri) {
        if (null != mCache) {
            return mCache.getInputStream(uri);
        }
        return null;
    }

    /**
     * This method iterates through the memory cache (if enabled) and removes any entries which are
     * not currently being displayed. A good place to call this would be from {@link
     * android.app.Application#onLowMemory() Application.onLowMemory()}.
     */
    public void trimMemory() {
        if (null != mMemoryCache) {
            mMemoryCache.trimMemory();
        }
    }


    private CacheableBitmapDrawable decodeBitmap(InputStreamProvider ip, String url,
                                                 BitmapFactory.Options opts, int sizeLimit) {

        Bitmap bm = null;
        InputStream is = null;
        int source = CacheableBitmapDrawable.SOURCE_NEW;

        try {
            if (opts == null) {
                opts = new BitmapFactory.Options();
            }

            if (opts.inSampleSize <= 1) {
                opts.inSampleSize = 1;
            }

            if (sizeLimit > 0)
                if (addSimpleSizeBitmapOptions(ip, opts, sizeLimit))
                    Log.i(Constants.LOG_TAG, "compressed:" + url);

            // Get InputStream for actual decode
            is = ip.getInputStream();
            // Decode stream
            bm = BitmapFactory.decodeStream(is, null, opts);
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, "Unable to decode stream", e);
        } finally {
            IoUtils.closeStream(is);
        }

        if (bm != null) {
            return new CacheableBitmapDrawable(url, mResources, bm, mRecyclePolicy, source);
        }
        return null;
    }

    protected CacheableBitmapDrawable decodeBitmap(InputStreamProvider ip, String url,
                                                   BitmapFactory.Options opts) {
        return decodeBitmap(ip, url, opts, 0);
    }


    private boolean addSimpleSizeBitmapOptions(InputStreamProvider ip, BitmapFactory.Options opts, int sizeLimit) {
        final InputStream is = ip.getInputStream();
        // Decode the bounds so we know what size Bitmap to look for
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, opts);
        IoUtils.closeStream(is);

        int i = 0;

        while (opts.outHeight >> i > sizeLimit && opts.outWidth >> i > sizeLimit)
            i++;

        if (i > 0) {
            i--;
        }

        opts.inSampleSize = 1 << i;

        opts.inJustDecodeBounds = false;

        return i != 0;
    }


    public final static class Builder {

        static final int MEGABYTE = 1024 * 1024;

        static final float DEFAULT_MEMORY_CACHE_HEAP_RATIO = 1f / 8f;

        static final float MAX_MEMORY_CACHE_HEAP_RATIO = 0.75f;

        static final int DEFAULT_MEM_CACHE_MAX_SIZE_MB = 3;

        static final RecyclePolicy DEFAULT_RECYCLE_POLICY = RecyclePolicy.PRE_HONEYCOMB_ONLY;

        // Only used for Javadoc
        static final float DEFAULT_MEMORY_CACHE_HEAP_PERCENTAGE = DEFAULT_MEMORY_CACHE_HEAP_RATIO
                * 100;

        static final float MAX_MEMORY_CACHE_HEAP_PERCENTAGE = MAX_MEMORY_CACHE_HEAP_RATIO * 100;

        private static long getHeapSize() {
            return Runtime.getRuntime().maxMemory();
        }

        private Context mContext;

        private boolean mMemoryCacheEnabled;

        private int mMemoryCacheMaxSize;

        private RecyclePolicy mRecyclePolicy;

        private BaseCache mCache;

        public Builder(Context context) {
            mContext = context;

            // Memory Cache is enabled by default, with a small maximum size
            mMemoryCacheEnabled = true;
            mMemoryCacheMaxSize = DEFAULT_MEM_CACHE_MAX_SIZE_MB * MEGABYTE;
            mRecyclePolicy = DEFAULT_RECYCLE_POLICY;
        }

        /**
         * builder.
         */
        public BitmapCacheWrapper build() {
            final BitmapCacheWrapper cache = new BitmapCacheWrapper(mContext);

            if (isValidOptionsForMemoryCache()) {
                if (Constants.DEBUG) {
                    Log.d("BitmapLruCache.Builder", "Creating Memory Cache");
                }
                cache.setMemoryCache(new BitmapMemoryLruCache(mMemoryCacheMaxSize, mRecyclePolicy));
            }

            if (mCache != null) {
                cache.setCache(mCache);
            }

            return cache;
        }

        public Builder setCache(BaseCache cache) {
            mCache = cache;
            return this;
        }


        /**
         * Set whether the Memory Cache should be enabled. Defaults to {@code true}.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setMemoryCacheEnabled(boolean enabled) {
            mMemoryCacheEnabled = enabled;
            return this;
        }

        /**
         * Set the maximum number of bytes the Memory Cache should use to store values. Defaults to
         * {@value #DEFAULT_MEM_CACHE_MAX_SIZE_MB}MB.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setMemoryCacheMaxSize(int size) {
            mMemoryCacheMaxSize = size;
            return this;
        }

        /**
         * Sets the Memory Cache maximum size to be the default value of {@value
         * #DEFAULT_MEMORY_CACHE_HEAP_PERCENTAGE}% of heap size.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setMemoryCacheMaxSizeUsingHeapSize() {
            return setMemoryCacheMaxSizeUsingHeapSize(DEFAULT_MEMORY_CACHE_HEAP_RATIO);
        }

        /**
         * Sets the Memory Cache maximum size to be the given percentage of heap size. This is
         * capped at {@value #MAX_MEMORY_CACHE_HEAP_PERCENTAGE}% of the app heap size.
         *
         * @param percentageOfHeap - percentage of heap size. Valid values are 0.0 <= x <= {@value
         *                         #MAX_MEMORY_CACHE_HEAP_RATIO}.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setMemoryCacheMaxSizeUsingHeapSize(float percentageOfHeap) {
            int size = Math
                    .round(getHeapSize() * Math.min(percentageOfHeap, MAX_MEMORY_CACHE_HEAP_RATIO));
            return setMemoryCacheMaxSize(size);
        }

        /**
         * Sets the recycle policy. This controls if {@link android.graphics.Bitmap#recycle()} is
         * called.
         *
         * @param recyclePolicy - New recycle policy, can not be null.
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setRecyclePolicy(RecyclePolicy recyclePolicy) {
            if (null == recyclePolicy) {
                throw new IllegalArgumentException("The recycle policy can not be null");
            }

            mRecyclePolicy = recyclePolicy;
            return this;
        }

        private boolean isValidOptionsForMemoryCache() {
            return mMemoryCacheEnabled && mMemoryCacheMaxSize > 0;
        }
    }
}
