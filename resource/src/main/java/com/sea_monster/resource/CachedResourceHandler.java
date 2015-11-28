package com.sea_monster.resource;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.sea_monster.cache.DiskCacheWrapper;
import com.sea_monster.cache.DiskLruCache;
import com.sea_monster.cache.IoUtils;
import com.sea_monster.common.Md5;
import com.sea_monster.network.StoreStatusCallback;

/**
 * Created by DragonJ on 15/1/27.
 */
public class CachedResourceHandler implements IResourceHandler<File, Resource> {

    public DiskCacheWrapper mCache;


    public CachedResourceHandler(Context context, DiskCacheWrapper cache) {
        mCache = cache;
    }

    @Override
    public boolean exists(Resource resource) {
        if (mCache == null || resource == null || resource.getUri() == null)
            return false;

        return mCache.contains(resource.getUri());
    }

    @Override
    public File get(Resource resource) {
        if (mCache == null || resource == null || resource.getUri() == null)
            return null;

        return mCache.getFile(resource.getUri());
    }

    @Override
    public File getFile(Resource resource) {
        if (mCache == null || resource == null || resource.getUri() == null)
            return null;

        return mCache.getFile(resource.getUri());
    }

    @Override
    public InputStream getInputStream(Resource resource) throws IOException {
        if (mCache == null || resource == null || resource.getUri() == null)
            return null;

        return mCache.getInputStream(resource.getUri());
    }

    @Override
    public void store(Resource resource, InputStream is) throws IOException {
        if (mCache == null || resource == null || resource.getUri() == null)
            return;

        mCache.put(resource.getUri(),is);
    }

    //TODO StoreStatusCallback
    @Override
    public void store(Resource resource, InputStream is, long total, StoreStatusCallback statusCallback) throws IOException {
        if (mCache == null || resource == null || resource.getUri() == null)
            return;

        store(resource, new ProgressInputStreamWrapper(is, total, statusCallback));
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void remove(Resource resource) {
        if (mCache == null || resource == null || resource.getUri() == null)
            return;

        mCache.remove(resource.getUri());
    }



}
