package com.sea_monster.core.resource.model;

import android.net.Uri;
import android.os.Parcel;

import com.sea_monster.core.utils.ParcelUtils;

public class CompressedResource extends Resource {
	private int height;
	private int width;
	private boolean isCrop;
	Resource mResource;
	Uri mCompressUri;

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public CompressedResource(Resource res, int width, int height, boolean isCrop) {
		mResource = res;
		this.height = height;
		this.width = width;
		this.isCrop = isCrop;
		mCompressUri = res.getUri().buildUpon().appendQueryParameter("thum", width + "x" + height).appendQueryParameter("crop", String.valueOf(isCrop)).build();
	}

	public CompressedResource(Resource res, int width, int height) {
		this(res, width, height, true);
	}

	public CompressedResource(Resource res) {
		mResource = res;
	}

	public Uri getUri() {
		return this.mCompressUri;
	}

	public Resource getOriResource() {
		return mResource;
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}

	public boolean isCrop() {
		return isCrop;
	}

	public void setCrop(boolean crop) {
		isCrop = crop;
	}

	public CompressedResource(Parcel in) {
		mResource = ParcelUtils.readFromParcel(in, Resource.class);
        height = ParcelUtils.readIntFromParcel(in);
        width = ParcelUtils.readIntFromParcel(in);
        isCrop = ParcelUtils.readIntFromParcel(in) == 1 ? true : false;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest,mResource);
        ParcelUtils.writeToParcel(dest,height);
        ParcelUtils.writeToParcel(dest,width);
        ParcelUtils.writeToParcel(dest,isCrop?1:0);
	}

	public static final Creator<CompressedResource> CREATOR = new Creator<CompressedResource>() {
		public CompressedResource createFromParcel(Parcel in) {
			return new CompressedResource(in);
		}

		@Override
		public CompressedResource[] newArray(int size) {
			return new CompressedResource[size];
		}
	};
}
