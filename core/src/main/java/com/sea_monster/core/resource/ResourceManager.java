package com.sea_monster.core.resource;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import com.sea_monster.core.exception.BaseException;
import com.sea_monster.core.network.AbstractHttpRequest;
import com.sea_monster.core.network.StoreStatusCallback;
import com.sea_monster.core.resource.cache.ResourceCacheWrapper;
import com.sea_monster.core.resource.io.ResourceRemoteWrapper;
import com.sea_monster.core.resource.model.CompressedResource;
import com.sea_monster.core.resource.model.LocalMicroResource;
import com.sea_monster.core.resource.model.LocalResource;
import com.sea_monster.core.resource.model.RequestResource;
import com.sea_monster.core.resource.model.Resource;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Observer;

/**
 * Created by dragonj on 14-4-2.
 */
public class ResourceManager {
    private static final String TAG = "RemoteResourceManager";
    private final boolean DEBUG = false;
    private Context mContext;
    private ResourceRemoteWrapper mRemoteWrapper;
    private ResourceCacheWrapper mCacheWrapper;

    private static ResourceManager sS;

    public static void init(Context context, ResourceRemoteWrapper remoteWrapper, ResourceCacheWrapper cacheWrapper) {
        sS = new ResourceManager(context, remoteWrapper, cacheWrapper);
    }

    public static ResourceManager getInstance() {
        return sS;
    }

    private ResourceManager(Context context, ResourceRemoteWrapper remoteWrapper, ResourceCacheWrapper cacheWrapper) {
        mContext = context;
        mRemoteWrapper = remoteWrapper;
        mCacheWrapper = cacheWrapper;
    }

    public boolean containsInMemoryCache(Resource resource) {
        return mCacheWrapper.containsInMemoryCache(resource);
    }

    public boolean containsInCache(Resource resource) {
        return mCacheWrapper.contains(resource);
    }

    public boolean containsInDisk(Resource resource) {
        return mRemoteWrapper.exists(resource);
    }

    public void removeFromDisk(Resource resource) {
        mRemoteWrapper.remove(resource);
    }

    public Uri getFileUri(Resource res) {
        return mRemoteWrapper.getFileUri(res);
    }

    public File getFile(Resource res) {
        return mRemoteWrapper.getFile(res.getUri());
    }

    public void addObserver(Observer observer) {
        mRemoteWrapper.addObserver(observer);
    }

    public void deleteObserver(Observer observer) {
        mRemoteWrapper.deleteObserver(observer);
    }


    public BitmapDrawable getDrawable(final Resource resource) {
        BitmapDrawable drawable = null;
        drawable = mCacheWrapper.get(resource);

        if (mCacheWrapper.contains(resource)) {
            drawable = mCacheWrapper.get(resource);
            return drawable;
        }


        if (resource instanceof LocalMicroResource) {
            drawable = mCacheWrapper.buildCompareBitmap((LocalMicroResource) resource);
        } else if (resource instanceof LocalResource) {
            throw new RuntimeException("NOT support use compress item");
        } else if (resource instanceof RequestResource) {
            if (mRemoteWrapper.exists(resource)) {
                try {
                    return new BitmapDrawable(mContext.getResources(), mRemoteWrapper.getBitmap(resource));
                } catch (BaseException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    mRemoteWrapper.request(resource);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        } else if (resource instanceof CompressedResource) {
            CompressedResource compressedResource = (CompressedResource) resource;
            if (compressedResource.getOriResource() instanceof LocalResource) {
                drawable = mCacheWrapper.buildCompareBitmap(compressedResource);
            } else {
                if (mRemoteWrapper.exists(compressedResource.getOriResource())) {
                    drawable = mCacheWrapper.buildCompareBitmap(compressedResource);
                } else {
                    try {
                        mRemoteWrapper.request(compressedResource.getOriResource());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (mRemoteWrapper.exists(resource)) {
                try {
                    return new BitmapDrawable(mContext.getResources(), mRemoteWrapper.getBitmap(resource));
                } catch (BaseException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    mRemoteWrapper.request(resource);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return drawable;
    }

    public AbstractHttpRequest<File> requestResource(final Resource resource) throws URISyntaxException {
        return mRemoteWrapper.request(resource);
    }

    public AbstractHttpRequest<File> requestResource(final Resource resource, StoreStatusCallback callback) throws URISyntaxException {
        return mRemoteWrapper.request(resource,callback);
    }

    public void cancelRequest(Resource resource) {
        mRemoteWrapper.cancel(resource);
    }

    public BitmapDrawable getCompressBitmap(CompressedResource resource) {
        return mCacheWrapper.buildCompareBitmap(resource);
    }

    public BitmapDrawable getCompressBitmap(LocalMicroResource resource) {
        return mCacheWrapper.buildCompareBitmap(resource);
    }

    public void compressImageToFile(final CompressedResource resource, File file) throws BaseException {
        mCacheWrapper.getCompressFile(resource, file.getPath());
    }
}
