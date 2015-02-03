package com.sea_monster.core.resource.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.sea_monster.core.utils.ParcelUtils;

public class Resource implements Parcelable {

    protected Uri resUri;
    private int mOrientaion = -1;

    public Resource() {
    }


    public Resource(Uri uri) {
        this.resUri = uri;
    }

    public Resource(Parcel in) {
        this(ParcelUtils.readFromParcel(in, Uri.class));
    }

    public Resource(Resource resource) {
        this.resUri = resource.getUri();
    }


    public Resource(String uriPath) {
        this(Uri.parse(uriPath));
    }


    public Uri getUri() {
        return this.resUri;
    }

    @Override
    public int hashCode() {
        return this.resUri.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getOrientaion() {
        return mOrientaion;
    }

    public void setOrientaion(int orientaion) {
        this.mOrientaion = orientaion;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, resUri);
    }

    public static final Creator<Resource> CREATOR = new Creator<Resource>() {
        public Resource createFromParcel(Parcel in) {
            return new Resource(in);
        }

        @Override
        public Resource[] newArray(int size) {
            return new Resource[size];
        }
    };

    public boolean equals(Resource o) {
        if (o == null || (o.getUri() == null && getUri() != null)) {
            return false;
        }
        return o.getUri().equals(this.getUri());
    }

    ;

}