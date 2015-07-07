package com.sea_monster.resource;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sea_monster.cache.BitmapCacheWrapper;
import com.sea_monster.network.StoreStatusCallback;

/**
 * Created by DragonJ on 15/1/29.
 */
public class CachedImageResourceHandler implements IResourceHandler<BitmapDrawable> {

    BitmapCacheWrapper cache;

    public CachedImageResourceHandler(Context context, BitmapCacheWrapper cache) {
        this.cache = cache;
    }

    @Override
    public boolean exists(Resource resource) {
        return cache.contains(resource.getUri());
    }

    @Override
    public BitmapDrawable get(Resource resource) {
        return cache.get(resource.getUri());
    }

    @Override
    public File getFile(Resource resource) {

        File file = cache.getFileFromDiskCache(resource.getUri());
        if (file != null)
            return file;
        return null;
    }

    @Override
    public InputStream getInputStream(Resource resource) throws IOException {
        File file = cache.getFileFromDiskCache(resource.getUri());
        if (file != null)
            return new FileInputStream(file);
        return null;
    }

    @Override
    public void store(Resource resource, InputStream is) throws IOException {
        cache.put(resource.getUri(), is);
    }

    @Override
    public void store(Resource resource, InputStream is, long total, StoreStatusCallback statusCallback) throws IOException {
        store(resource, new ProgressInputStreamWrapper(is,total, statusCallback));
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void remove(Resource resource) {
        cache.remove(resource.getUri());
    }
}
