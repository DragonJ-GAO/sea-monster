package me.add1.cache;

import android.net.Uri;

import java.io.File;
import java.io.InputStream;

/**
 * Created by DragonJ on 15/1/31.
 */
public class DiskCacheWrapper extends BaseCache {

    BaseCache mCache;

    public DiskCacheWrapper(BaseCache mCache) {
        super();
        this.mCache = mCache;
    }

    public boolean contains(Uri uri) {
        if (null == mCache|| uri == null)
            return false;

        return mCache.contains(uri);
    }

    public File getFile(Uri uri) {
        if (null == mCache || uri == null)
            return null;

        return mCache.getFile(uri);
    }

    public String getPath(Uri uri) {
        File file = getFile(uri);
        if (file == null)
            return null;
        return file.getPath();
    }

    public InputStream getInputStream(Uri uri) {
        if (null == mCache || uri == null)
            return null;

        return mCache.getInputStream(uri);
    }

    public void put(final Uri uri, final InputStream inputStream) {
        if (null == mCache || uri == null)
            return;

        mCache.put(uri, inputStream);
    }

    public void remove(Uri uri){
        if(null == mCache || uri == null)
            return;

        mCache.remove(uri);
    }


    public final static class Builder {

        private BaseCache mCache;

        public Builder() {

        }

        public DiskCacheWrapper build() {
            DiskCacheWrapper cache = new DiskCacheWrapper(mCache);
            return cache;
        }

        public Builder setCache(BaseCache cache) {
            mCache = cache;
            return this;
        }
    }



}
