package me.add1.cache;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by DragonJ on 15/1/31.
 */
public class BaseCache {

    static final int DISK_CACHE_FLUSH_DELAY_SECS = 5;

    private File mTempDir;

    /**
     * Disk Cache Variables
     */
    protected DiskLruCache mDiskCache;

    // Variables which are only used when the Disk Cache is enabled
    protected Map<String, ReentrantLock> mDiskCacheEditLocks;

    protected ScheduledThreadPoolExecutor mDiskCacheFlusherExecutor;

    protected DiskCacheFlushRunnable mDiskCacheFlusherRunnable;

    protected File mDiskCacheLocation;

    // Transient
    protected ScheduledFuture<?> mDiskCacheFuture;


    protected BaseCache() {

    }

    public final static class Builder {

        String type;


        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder() {
        }

        public BaseCache build(Context context) {

            BaseCache cache = new BaseCache();
            File dir = context.getExternalCacheDir();

            if (dir != null && cache.validateLocation(dir)) {
                cache.mDiskCacheLocation = TextUtils.isEmpty(type) ? dir : new File(dir, type);
                cache.mTempDir = new File(dir, "tmp");
            } else {
                cache.mDiskCacheLocation = TextUtils.isEmpty(type) ? context.getCacheDir() : new File(context.getCacheDir(), type);
                cache.mTempDir = new File(context.getCacheDir(), "tmp");
            }

            try {
                cache.mTempDir.mkdirs();
                cache.mDiskCache = DiskLruCache.open(cache.mDiskCacheLocation, 0, 1, Constants.DEFAULT_DISK_CACHE_MAX_SIZE_MB * Constants.MEGABYTE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            cache.mDiskCacheEditLocks = new HashMap<>();
            cache.mDiskCacheFlusherExecutor = new ScheduledThreadPoolExecutor(Constants.DEFAULT_DISK_CACHE_MAX_SIZE_MB);
            cache.mDiskCacheFlusherRunnable = new DiskCacheFlushRunnable(cache.mDiskCache);

            return cache;
        }

    }

    private boolean validateLocation(File dir) {

        if (!dir.exists() && dir.canWrite())
            dir.mkdirs();

        if (!dir.exists() || !dir.isDirectory() || !dir.canWrite())
            return false;

        return true;
    }

    /**
     * @throws IllegalStateException if the calling thread is the main/UI thread.
     */
    protected static void checkNotOnMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException(
                    "This method should not be called from the main/UI thread.");
        }
    }

    protected static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * The disk cache only accepts a reduced range of characters for the key values. This method
     * simply return a MD5 hash of the url.
     *
     * @param uri - Key to be transformed
     * @return key which can be used for the disk cache
     */
    protected static String transformUrlForDiskCacheKey(Uri uri) {
        return Md5.encode(uri.toString());
    }

    protected ReentrantLock getLockForDiskCacheEdit(Uri uri) {
        synchronized (mDiskCacheEditLocks) {
            ReentrantLock lock = mDiskCacheEditLocks.get(uri.toString());
            if (null == lock) {
                lock = new ReentrantLock();
                mDiskCacheEditLocks.put(uri.toString(), lock);
            }
            return lock;
        }
    }

    protected void setDiskLruCache(DiskLruCache diskLruCache) {
        mDiskCache = diskLruCache;
    }

    protected void scheduleDiskCacheFlush() {
        // If we already have a flush scheduled, cancel it
        if (null != mDiskCacheFuture) {
            mDiskCacheFuture.cancel(false);
        }

        // Schedule a flush
        mDiskCacheFuture = mDiskCacheFlusherExecutor
                .schedule(mDiskCacheFlusherRunnable, DISK_CACHE_FLUSH_DELAY_SECS,
                        TimeUnit.SECONDS);
    }

    static final class DiskCacheFlushRunnable implements Runnable {

        private final DiskLruCache mDiskCache;

        public DiskCacheFlushRunnable(DiskLruCache cache) {
            mDiskCache = cache;
        }

        public void run() {
            // Make sure we're running with a background priority
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            if (Constants.DEBUG) {
                Log.d(Constants.LOG_TAG, "Flushing Disk Cache");
            }
            try {
                mDiskCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    interface InputStreamProvider {
        InputStream getInputStream();
    }

    protected static class FileInputStreamProvider implements InputStreamProvider {
        final File mFile;

        public FileInputStreamProvider(File file) {
            mFile = file;
        }

        @Override
        public InputStream getInputStream() {
            try {
                return new FileInputStream(mFile);
            } catch (FileNotFoundException e) {
                Log.e(Constants.LOG_TAG, "Could not decode file: " + mFile.getAbsolutePath(), e);
            }
            return null;
        }
    }

    protected static class ContentStreamProvider implements InputStreamProvider {
        final Uri mContent;
        final ContentResolver mResolver;

        ContentStreamProvider(ContentResolver resolver, Uri uri) {
            mContent = uri;
            mResolver = resolver;
        }

        @Override
        public InputStream getInputStream() {

            String id = mContent.getLastPathSegment();

            Cursor cursor = mResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA},
                    MediaStore.Images.Media._ID + " = ?", new String[]{id}, null);

            String path;

            if (cursor.getCount() == 0)
                return null;

            cursor.moveToFirst();

            path = cursor.getString(1);
            cursor.close();

            if (TextUtils.isEmpty(path))
                return null;

            try {
                FileInputStream stream = new FileInputStream(path);
                return stream;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    protected final class SnapshotInputStreamProvider implements InputStreamProvider {
        final String mKey;

        SnapshotInputStreamProvider(String key) {
            mKey = key;
        }

        @Override
        public InputStream getInputStream() {
            try {
                DiskLruCache.Snapshot snapshot = mDiskCache.get(mKey);
                if (snapshot != null) {
                    return snapshot.getInputStream(0);
                }
            } catch (IOException e) {
                Log.e(Constants.LOG_TAG, "Could open disk cache for url: " + mKey, e);
            }
            return null;
        }

        public File getFile() {
            try {
                DiskLruCache.Snapshot snapshot = mDiskCache.get(mKey);
                if (snapshot != null) {
                    return snapshot.getFile(0);
                }
            } catch (IOException e) {
                Log.e(Constants.LOG_TAG, "Could open disk cache for url: " + mKey, e);
            }
            return null;
        }
    }

    public static enum RecyclePolicy {
        /**
         * The Bitmap is never recycled automatically.
         */
        DISABLED,

        /**
         * The Bitmap is only automatically recycled if running on a device API v10 or earlier.
         */
        PRE_HONEYCOMB_ONLY,

        /**
         * The Bitmap is always recycled when no longer being used. This is the default.
         */
        ALWAYS;

        boolean canCached() {
            switch (this) {
                case PRE_HONEYCOMB_ONLY:
                case DISABLED:
                    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
            }
            return false;
        }

        boolean canRecycle() {
            switch (this) {
                case DISABLED:
                    return false;
                case PRE_HONEYCOMB_ONLY:
                    return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
                case ALWAYS:
                    return true;
            }

            return false;
        }
    }

    public boolean contains(Uri uri) {
        if (null != mDiskCache) {
            checkNotOnMainThread();

            try {
                return null != mDiskCache.get(transformUrlForDiskCacheKey(uri));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public File getFile(Uri uri) {
        if (null == mDiskCache)
            return null;

        checkNotOnMainThread();

        final String key = transformUrlForDiskCacheKey(uri);

        try {
            SnapshotInputStreamProvider provider = new SnapshotInputStreamProvider(key);

            if (null != provider) {
                return provider.getFile();
            } else {
                mDiskCache.remove(key);
                scheduleDiskCacheFlush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InputStream getInputStream(Uri uri) {
        if (null == mDiskCache)
            return null;

        checkNotOnMainThread();

        final String key = transformUrlForDiskCacheKey(uri);

        try {
            SnapshotInputStreamProvider provider = new SnapshotInputStreamProvider(key);

            if (null != provider) {
                return provider.getInputStream();
            } else {
                mDiskCache.remove(key);
                scheduleDiskCacheFlush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void put(final Uri uri, final InputStream inputStream) {
        checkNotOnMainThread();

        // First we need to save the stream contents to a temporary file, so it
        // can be read multiple times
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("bitmapcache_", null, mTempDir);

            // Pipe InputStream to file
            IoUtils.copy(inputStream, tmpFile);
        } catch (IOException e) {
            Log.e(Constants.LOG_TAG, "Error writing to saving stream to temp file: " + uri.toString(), e);
        }


        if (null != tmpFile) {
            // Try and decode File
            FileInputStreamProvider provider = new FileInputStreamProvider(tmpFile);

            if (provider != null) {


                if (null != mDiskCache) {
                    final String key = transformUrlForDiskCacheKey(uri);
                    final ReentrantLock lock = getLockForDiskCacheEdit(uri);
                    lock.lock();

                    try {
                        DiskLruCache.Editor editor = mDiskCache.edit(key);

                        if (editor != null) {
                            IoUtils.copy(tmpFile, editor.newOutputStream(0));
                            editor.commit();
                        }

                    } catch (IOException e) {
                        Log.e(Constants.LOG_TAG, "Error writing to disk cache. URL: " + uri, e);
                    } finally {
                        lock.unlock();
                        scheduleDiskCacheFlush();
                    }
                }

                // Finally, delete the temporary file
                tmpFile.delete();
            }
        }
    }

    public void remove(Uri uri) {
        if (null != mDiskCache) {
            checkNotOnMainThread();

            try {
                mDiskCache.remove(transformUrlForDiskCacheKey(uri));
                scheduleDiskCacheFlush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
