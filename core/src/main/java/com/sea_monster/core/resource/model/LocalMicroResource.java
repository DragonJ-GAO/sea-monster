package com.sea_monster.core.resource.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.sea_monster.core.utils.ParcelUtils;

public class LocalMicroResource extends LocalResource {

	public LocalMicroResource(long imageId, Uri uri) {
		super(imageId, uri.buildUpon().appendQueryParameter("thum", "micro").build());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
        ParcelUtils.writeToParcel(dest, imageId);
		ParcelUtils.writeToParcel(dest, getUri());
	}

	public LocalMicroResource(Parcel in) {
		super(in);
		imageId =ParcelUtils.readLongFromParcel(in);
		resUri = ParcelUtils.readFromParcel(in, Uri.class);		
	}

	public static final Parcelable.Creator<LocalMicroResource> CREATOR = new Parcelable.Creator<LocalMicroResource>() {
		public LocalMicroResource createFromParcel(Parcel in) {
			return new LocalMicroResource(in);
		}

		@Override
		public LocalMicroResource[] newArray(int size) {
			return new LocalMicroResource[size];
		}
	};
}
