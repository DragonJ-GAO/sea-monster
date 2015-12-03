package me.add1.resource;

import android.net.Uri;
import android.os.Parcel;
import android.text.TextUtils;

import me.add1.common.ParcelUtils;
import me.add1.common.UriUtils;

import java.util.Map;

/**
 * Created by dragonj on 15/11/26.
 */
public class ImageResource extends Resource {

    public ImageResource(Uri uri) {
        super(uri);
    }

    public ImageResource(Parcel in) {
        super(in);
    }

    public ImageResource(Resource resource) {
        super(resource);
    }

    public ImageResource(String uriPath) {
        super(uriPath);
    }

    public final static class Builder {
        Uri uri;
        Uri originalUri;
        boolean thumb;
        int width;
        int height;
        boolean corp;

        public Builder() {

        }

        public Builder uri(Uri uri) {
            this.uri = uri;
            return this;
        }

        public Builder supportThumb(boolean thumb) {
            this.thumb = thumb;
            return this;
        }

        public Builder limitWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder limitHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder supportCorp(boolean corp) {
            this.corp = corp;
            return this;
        }

        public ImageResource build() {
            if (thumb) {
                Uri.Builder builder = Uri.parse("thumb://me.add1").buildUpon();

                builder.appendQueryParameter("add1_thumb", "1");
                if (corp)
                    builder.appendQueryParameter("add1_corp", "1");
                builder.appendQueryParameter("add1_limit_width", String.valueOf(width));
                builder.appendQueryParameter("add1_limit_height", String.valueOf(height));
                builder.appendQueryParameter("add1_original", uri.toString());
                uri = builder.build();
            }


            ImageResource resource = new ImageResource(uri);
            return resource;
        }
    }

    public boolean isThumb() {
        return !TextUtils.isEmpty(uri.getQueryParameter("add1_thumb"));
    }

    public boolean isCorp() {
        return !TextUtils.isEmpty(uri.getQueryParameter("add1_corp"));

    }

    public int getLimitWidth() {
        if (TextUtils.isEmpty(uri.getQueryParameter("add1_limit_width")))
            return 0;

        return Integer.parseInt(uri.getQueryParameter("add1_limit_width"));
    }

    public int getLimitHeight() {
        if (TextUtils.isEmpty(uri.getQueryParameter("add1_limit_height")))
            return 0;

        return Integer.parseInt(uri.getQueryParameter("add1_limit_height"));
    }

    public Uri getOriginalUri() {
        if (!TextUtils.isEmpty(uri.getQueryParameter("add1_original"))) {
            return Uri.parse(uri.getQueryParameter("add1_original"));
        }
        return uri;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, uri);
    }

    public static final Creator<ImageResource> CREATOR = new Creator<ImageResource>() {
        public ImageResource createFromParcel(Parcel in) {
            return new ImageResource(in);
        }

        @Override
        public ImageResource[] newArray(int size) {
            return new ImageResource[size];
        }
    };
}
