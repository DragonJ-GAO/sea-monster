package me.add1.resource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import me.add1.cache.BitmapCacheWrapper;
import me.add1.cache.CacheableBitmapDrawable;
import me.add1.network.StoreStatusCallback;

/**
 * Created by DragonJ on 15/1/29.
 */
public class CachedImageResourceHandler implements IResourceHandler<CacheableBitmapDrawable, ImageResource> {

    BitmapCacheWrapper cache;
    Context mContext;

    public CachedImageResourceHandler(Context context, BitmapCacheWrapper cache) {
        this.cache = cache;
        mContext = context;
    }

    @Override
    public boolean exists(ImageResource resource) {
        return cache.contains(resource.getUri());
    }

    @Override
    public CacheableBitmapDrawable get(ImageResource resource) {
        if (cache.contains(resource.getUri()))
            return cache.get(resource.getUri(), null);

        if (resource.isThumb()) {
            Uri originalUri = resource.getOriginalUri();
            if (originalUri.getScheme().equals("http") || originalUri.getScheme().equals("https")) {
                File file = cache.getFile(resource.getOriginalUri());
                if (file != null && file.exists()) {
                    try {
                        Bitmap thumbBitmap = CompressUtils.compressResource(mContext, Uri.fromFile(file), resource.getLimitWidth(), resource.getLimitHeight(), resource.isCorp());
                        if (thumbBitmap != null) {
                            return cache.put(resource.getUri(), thumbBitmap);
                        }
                    } catch (IOException e) {
                        Log.e("Cache_Handler_Compress", e.getMessage(), e);
                    }
                }
            } else if (originalUri.getScheme().equals("file")) {
                File file = new File(originalUri.getPath());
                if (file.exists()) {
                    try {
                        Bitmap thumbBitmap = CompressUtils.compressResource(mContext, originalUri, resource.getLimitWidth(), resource.getLimitWidth(), resource.isCorp());

                        if (thumbBitmap != null) {
                            return cache.put(resource.getUri(), thumbBitmap);
                        }
                    } catch (IOException e) {
                        Log.e("Cache_Handler_Compress", e.getMessage(), e);
                    }
                }
            } else if (resource.getUri().getScheme().equals("content")) {
                try {
                    Bitmap thumbBitmap = CompressUtils.compressResource(mContext, originalUri, resource.getLimitWidth(), resource.getLimitWidth(), resource.isCorp());
                    if (thumbBitmap != null) {
                        return cache.put(resource.getUri(), thumbBitmap);
                    }
                } catch (IOException e) {
                    Log.e("Cache_Handler_Compress", e.getMessage(), e);
                }
            } else {
                throw new RuntimeException("Unknown schema:" + resource.getUri().getScheme());
            }

        }

        return null;
    }

    @Override
    public File getFile(ImageResource resource) {

        File file = cache.getFileFromDiskCache(resource.getUri());
        if (file != null)
            return file;
        return null;
    }

    @Override
    public InputStream getInputStream(ImageResource resource) throws IOException {
        File file = cache.getFileFromDiskCache(resource.getUri());
        if (file != null)
            return new FileInputStream(file);
        return null;
    }

    @Override
    public void store(ImageResource resource, InputStream is) throws IOException {
        cache.put(resource.getUri(), is);
    }

    @Override
    public void store(ImageResource resource, InputStream is, long total, StoreStatusCallback statusCallback) throws IOException {
        store(resource, new ProgressInputStreamWrapper(is, total, statusCallback));
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void remove(ImageResource resource) {
        cache.remove(resource.getUri());
    }
}
