package me.add1.resource;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import me.add1.common.ParcelUtils;
import me.add1.common.UriUtils;

import java.util.Map;

public class Resource implements Parcelable {

    protected Uri uri;




    public Resource(Uri uri) {
        this.uri = uri;
    }

    public Resource(Parcel in) {
        this(ParcelUtils.readFromParcel(in, Uri.class));
    }

    public Resource(Resource resource) {
        this.uri = resource.getUri();
    }

    public Resource(String uriPath) {
        this(Uri.parse(uriPath));
    }




    public Uri getUri() {
        return this.uri;
    }

    @Override
    public int hashCode() {
        return this.uri.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }



    public boolean equals(Resource o) {
        if (o == null || (o.getUri() == null && getUri() != null)) {
            return false;
        }
        return o.getUri().equals(this.getUri());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, uri);
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
}