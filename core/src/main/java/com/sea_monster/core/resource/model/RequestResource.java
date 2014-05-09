package com.sea_monster.core.resource.model;

import android.net.Uri;
import android.os.Parcel;


import com.sea_monster.core.exception.BaseException;
import com.sea_monster.core.network.AbstractHttpRequest;
import com.sea_monster.core.network.StoreStatusCallback;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by DragonJ on 13-5-22.
 */
public class RequestResource extends Resource implements StoreStatusCallback {

	private StoreStatus mStoreStatus;

	WeakReference<StoreStatusCallback> mListener;

	public RequestResource(String uriPath) {
		super(uriPath);
	}

	public RequestResource(Uri uri) {
		super(uri);
	}

	public RequestResource(Parcel in) {
		super(in);
	}

	public void registeredStatusChangeListener(StoreStatusCallback listener) {
		mListener = new WeakReference<StoreStatusCallback>(listener);
	}

	public void unRegisteredStatusChangeListener() {
		mListener = null;
	}


	public StoreStatus getStoreStatus() {
		return mStoreStatus;
	}

    @Override
    public void onComplete(AbstractHttpRequest<File> request, File obj) {
        if (mListener != null && mListener.get() != null) {
            mListener.get().onComplete(request, obj);
        }
    }

    @Override
    public void onFailure(AbstractHttpRequest<File> request, BaseException e) {
        if (mListener != null && mListener.get() != null) {
            mListener.get().onFailure(request, e);
        }
    }

    @Override
    public void statusCallback(StoreStatus obj) {
        if (mStoreStatus != obj)
            mStoreStatus = obj;
        if (mListener != null && mListener.get() != null) {
            mListener.get().statusCallback(obj);
        }
    }
}
