package com.sea_monster.core.resource.model;

import android.net.Uri;
import android.os.Parcel;

/**
 * Created by DragonJ on 13-7-3.
 */
public class LocalResource extends Resource {
	long imageId;

	public long getImageId() {
		return imageId;
	}

	public void setImageId(long imageId) {
		this.imageId = imageId;
	}

	public LocalResource(long imageId, Uri uri) {
		super(uri);
		this.imageId = imageId;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(imageId);
	}

	public LocalResource(Parcel in) {
		super(in);
		imageId = in.readLong();

	}

	public static final Creator<LocalResource> CREATOR = new Creator<LocalResource>() {
		public LocalResource createFromParcel(Parcel in) {
			return new LocalResource(in);
		}

		@Override
		public LocalResource[] newArray(int size) {
			return new LocalResource[size];
		}
	};
}
