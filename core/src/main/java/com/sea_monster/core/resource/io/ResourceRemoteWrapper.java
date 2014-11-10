package com.sea_monster.core.resource.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.sea_monster.core.exception.BaseException;
import com.sea_monster.core.exception.InternalException;
import com.sea_monster.core.network.AbstractHttpRequest;
import com.sea_monster.core.network.HttpHandler;
import com.sea_monster.core.network.ResRequest;
import com.sea_monster.core.network.StoreStatusCallback;
import com.sea_monster.core.resource.model.LocalResource;
import com.sea_monster.core.resource.model.RequestResource;
import com.sea_monster.core.resource.model.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dragonj on 14-4-3.
 */
public class ResourceRemoteWrapper extends Observable {
    private static final String TAG = "RemoteResourceManager";
    private final boolean DEBUG = false;
    private IFileSysHandler fileSysHandler;
    private HttpHandler httpHandler;
    private Map<Resource, AbstractHttpRequest> onRequestMapping;
    private Context context;

    public ResourceRemoteWrapper(Context context, IFileSysHandler fileHandler, HttpHandler httpHandler) {
        this.context = context;
        fileSysHandler = fileHandler;
        this.httpHandler = httpHandler;
        this.onRequestMapping = new ConcurrentHashMap<Resource, AbstractHttpRequest>();
    }

    public boolean exists(Resource resource) {

        if (onRequestMapping.containsKey(resource))
            return false;

        if (resource instanceof LocalResource) {
            return true;
        }

        return fileSysHandler.exists(resource.getUri());
    }


    public Uri getFileUri(Resource res) {
        return Uri.fromFile(getFile(res));
    }

    public boolean exists(Uri uri) {
        return fileSysHandler.exists(uri);
    }

    public File getFile(Uri uri) {
        if (DEBUG)
            Log.d(TAG, "getInputStream(): " + uri);
        return fileSysHandler.getFile(uri);
    }

    public File getFile(Resource res) {
        if (DEBUG)
            Log.d(TAG, "getInputStream(): " + res.getUri());
        return fileSysHandler.getFile(res.getUri());
    }

    public InputStream getInputStream(Resource res) {
        InputStream resultStream = null;
        resultStream = getInputStream(res.getUri());
        return resultStream;
    }

    public InputStream getInputStream(Uri uri) {
        try {
            return fileSysHandler.getInputStream(uri);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    public void put(Resource res, InputStream stream){
        try {
            fileSysHandler.store(res.getUri(),stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBitmap(Resource res) throws BaseException {
        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeStream(getInputStream(res));
        } catch (OutOfMemoryError e1) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inSampleSize = 2;
                bitmap = BitmapFactory.decodeStream(getInputStream(res), null, options);
            } catch (OutOfMemoryError e) {
                throw new InternalException(InternalException.IMAGE_GET_FAIL, e);
            }
        }

        return bitmap;
    }

    public AbstractHttpRequest<File> request(final Resource img) throws URISyntaxException {

        if (img instanceof RequestResource) {
            return request((RequestResource) img);
        }

        if (onRequestMapping.containsKey(img))
            return onRequestMapping.get(img);

        final String scheme = img.getUri().getScheme().toLowerCase();

        if (scheme.equals("http") || scheme.equals("https")) {
            ResRequest resRequest = new ResRequest(img, fileSysHandler) {

                @Override
                public void onComplete(AbstractHttpRequest<File> request, File obj) {
                    setChanged();
                    onRequestMapping.remove(img);
                    ResourceRemoteWrapper.this.notifyObservers(img);
                }

                @Override
                public void onFailure(AbstractHttpRequest<File> request, BaseException e) {
                    onRequestMapping.remove(img);
                    fileSysHandler.delFile(img.getUri());
                    setChanged();
                    ResourceRemoteWrapper.this.notifyObservers(img);
                    e.printStackTrace();
                }
            };
            AbstractHttpRequest<File> httpRequest = resRequest.obtainRequest();
            onRequestMapping.put(img, httpRequest);
            httpHandler.executeRequest(httpRequest);
            return httpRequest;
        } else {
            throw new RuntimeException("request only support http");
        }
    }

    public AbstractHttpRequest<File> request(final RequestResource img) throws URISyntaxException {
        return request(img, img);
    }

    public AbstractHttpRequest<File> request(final Resource img, final StoreStatusCallback callback) throws URISyntaxException {

        if (onRequestMapping.containsKey(img))
            return onRequestMapping.get(img);

        ResRequest resRequest = new ResRequest(img, fileSysHandler, callback) {
            @Override
            public void onComplete(AbstractHttpRequest<File> request, File obj) {
                Log.d("AbstractHttpRequest","onComplete");
                setChanged();
                onRequestMapping.remove(img);
                callback.onComplete(request, obj);
                ResourceRemoteWrapper.this.notifyObservers(img);
            }

            @Override
            public void onFailure(AbstractHttpRequest<File> request, BaseException e) {
                Log.d("AbstractHttpRequest","onFailure");
                onRequestMapping.remove(img);
                fileSysHandler.delFile(img.getUri());
                setChanged();
                callback.onFailure(request, e);
                e.printStackTrace();
            }
        };

        AbstractHttpRequest<File> httpRequest = resRequest.obtainRequest();

        onRequestMapping.put(img, httpRequest);
        httpHandler.executeRequest(httpRequest);

        return httpRequest;
    }

    public void invalidate(URI uri) {
        fileSysHandler.invalidate(Uri.parse(uri.toString()));
    }

    public void shutdown() {
        fileSysHandler.cleanup();
    }

    public void cancel(Resource resource) {

        if (onRequestMapping.containsKey(resource)) {
            httpHandler.cancelRequest(onRequestMapping.get(resource));
        }
    }

    public void remove(Uri uri) {
        fileSysHandler.delFile(uri);
    }

    public void remove(Resource res) {
        fileSysHandler.delFile(res.getUri());
    }

}
