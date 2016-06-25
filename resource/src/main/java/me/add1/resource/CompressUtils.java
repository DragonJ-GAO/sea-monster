package me.add1.resource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

/**
 * Created by dragonj on 15/11/27.
 */
public class CompressUtils {
    public static Bitmap compressResource(Context context, Uri source, int limitWidth, int limitHeight, boolean isCorp) throws IOException {
        Bitmap result = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(source.getPath(), options);
        options.inJustDecodeBounds = false;

        ExifInterface exifInterface = new ExifInterface(source.getPath());
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);


        if (source.getScheme().equals("content")) {

            long imageId;

            try {
                imageId = Long.parseLong(source.getLastPathSegment());
            } catch (NumberFormatException nfe) {
                throw new IOException("Source Not Found");
            }

            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), imageId,
                    MediaStore.Images.Thumbnails.MINI_KIND, options);
            Matrix matrix = new Matrix();

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90
                    || orientation == ExifInterface.ORIENTATION_ROTATE_270
                    || orientation == ExifInterface.ORIENTATION_TRANSPOSE
                    || orientation == ExifInterface.ORIENTATION_TRANSVERSE) {
                int tmp = w;
                w = h;
                h = tmp;

            }
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90, w / 2f, h / 2f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180, w / 2f, h / 2f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(270, w / 2f, h / 2f);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.preScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.preScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90, w / 2f, h / 2f);
                    matrix.preScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(270, w / 2f, h / 2f);
                    matrix.preScale(1, -1);
                    break;
            }

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90
                    || orientation == ExifInterface.ORIENTATION_ROTATE_270
                    || orientation == ExifInterface.ORIENTATION_TRANSPOSE
                    || orientation == ExifInterface.ORIENTATION_TRANSVERSE) {
                int tmp = limitWidth;
                limitWidth = limitHeight;
                limitHeight = tmp;
            }

            int x = 0, y = 0, width, height;

            if (limitWidth > bitmap.getWidth() && limitHeight > bitmap.getHeight()) {
                height = bitmap.getHeight();
                width = bitmap.getWidth();
            } else if (limitWidth > bitmap.getWidth()) {
                height = (bitmap.getWidth() * limitHeight) / limitWidth;
                width = bitmap.getWidth();
                x = 0;
                y = bitmap.getHeight() - limitHeight;
                y >>= 1;
            } else if (limitHeight > bitmap.getHeight()) {
                width = (bitmap.getHeight() * limitWidth) / limitHeight;
                height = bitmap.getHeight();
                y = 0;
                x = bitmap.getWidth() - limitWidth;
                x >>= 1;

            } else {

                int widthMix = limitWidth * bitmap.getHeight();
                int heightMix = limitHeight * bitmap.getWidth();
                if (widthMix == heightMix) {
                    width = limitWidth;
                    height = limitHeight;

                } else if (widthMix > heightMix) {
                    height = heightMix / limitWidth;
                    width = limitWidth;
                    y = bitmap.getHeight() - height;
                    y >>= 1;
                } else {
                    width = widthMix / limitHeight;
                    height = limitHeight;
                    x = bitmap.getWidth() - width;
                    x >>= 1;
                }
            }

            float scale = 0;
            if (width > height)
                scale = (float) width / bitmap.getWidth();
            else
                scale = (float) height / bitmap.getHeight();

            matrix.postScale(scale, scale);
            if (isCorp)
                result = Bitmap.createBitmap(bitmap, x, y, bitmap.getWidth() - (x << 1), bitmap.getHeight() - (y << 1), matrix, true);
            else
                result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        } else {

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90
                    || orientation == ExifInterface.ORIENTATION_ROTATE_270
                    || orientation == ExifInterface.ORIENTATION_TRANSPOSE
                    || orientation == ExifInterface.ORIENTATION_TRANSVERSE) {
                int tmp = limitWidth;
                limitWidth = limitHeight;
                limitHeight = tmp;
            }

            int width = options.outWidth;
            int height = options.outHeight;
            int sampleW = 1, sampleH = 1;

            while (width / 2 > limitWidth && limitWidth > 0) {
                width /= 2;
                sampleW <<= 1;
            }

            while (height / 2 > limitHeight && limitHeight > 0) {
                height /= 2;
                sampleH <<= 1;
            }

            options.inPreferredConfig = Bitmap.Config.RGB_565;
            int sampleSize = Math.min(sampleW, sampleH);

            options.inSampleSize = sampleSize;


            Bitmap bitmap = null;

            bitmap = BitmapFactory.decodeFile(source.getPath(), options);

            Matrix matrix = new Matrix();

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90
                    || orientation == ExifInterface.ORIENTATION_ROTATE_270
                    || orientation == ExifInterface.ORIENTATION_TRANSPOSE
                    || orientation == ExifInterface.ORIENTATION_TRANSVERSE) {
                int tmp = w;
                w = h;
                h = tmp;
            }
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90, w / 2f, h / 2f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180, w / 2f, h / 2f);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(270, w / 2f, h / 2f);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.preScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.preScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90, w / 2f, h / 2f);
                    matrix.preScale(1, -1);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(270, w / 2f, h / 2f);
                    matrix.preScale(1, -1);
                    break;
            }

            int x = 0, y = 0;

            if (limitWidth > bitmap.getWidth() && limitHeight > bitmap.getHeight()) {
                height = bitmap.getHeight();
                width = bitmap.getWidth();
            } else if (limitWidth > bitmap.getWidth()) {
                height = (bitmap.getWidth() * limitHeight) / limitWidth;
                width = bitmap.getWidth();
                x = 0;
                y = bitmap.getHeight() - limitHeight;
                y >>= 1;
            } else if (limitHeight > bitmap.getHeight()) {
                width = (bitmap.getHeight() * limitWidth) / limitHeight;
                height = bitmap.getHeight();
                y = 0;
                x = bitmap.getWidth() - limitWidth;
                x >>= 1;

            } else {

                int widthMix = limitWidth * bitmap.getHeight();
                int heightMix = limitHeight * bitmap.getWidth();
                if (widthMix == heightMix) {
                    width = limitWidth;
                    height = limitHeight;

                } else if (widthMix > heightMix) {
                    height = heightMix / limitWidth;
                    width = limitWidth;
                    y = bitmap.getHeight() - height;
                    y >>= 1;
                } else {
                    width = widthMix / limitHeight;
                    height = limitHeight;
                    x = bitmap.getWidth() - width;
                    x >>= 1;
                }
            }


            float scale = 0;
            if (width > height)
                scale = (float) width / bitmap.getWidth();
            else
                scale = (float) height / bitmap.getHeight();

            matrix.postScale(scale, scale);
            result = Bitmap.createBitmap(bitmap, x, y, bitmap.getWidth() - (x << 1), bitmap.getHeight() - (y << 1), matrix, true);
        }


        return result;
    }
}
