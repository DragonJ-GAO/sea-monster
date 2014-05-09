package com.sea_monster.core.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Build;
import android.util.FloatMath;
import android.util.Log;


import com.sea_monster.core.resource.model.ImageInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

public class ImageUtils {

	private static final String TAG = "BitmapUtils";
	private static final int DEFAULT_JPEG_QUALITY = 90;
	public static final int UNCONSTRAINED = -1;

	public static int getOrientation(String path) {
		int orientation = -1;
		try {
			ExifInterface EXIF = new ExifInterface(path);
			orientation = EXIF.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return orientation;
	}

	public static int getOrientation(File file) {
		int orientation = -1;
		try {
			ExifInterface EXIF = new ExifInterface(file.getPath());
			orientation = EXIF.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return orientation;
	}

	public static void repairOrientation(File file) {
		int orientation = 0;
		try {
			ExifInterface EXIF = new ExifInterface(file.getPath());
			orientation = EXIF.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
			if (orientation == 0)
				EXIF.setAttribute(ExifInterface.TAG_ORIENTATION, "1");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void repairOrientation(String path) {
		int orientation = 0;
		try {
			ExifInterface EXIF = new ExifInterface(path);
			orientation = EXIF.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
			if (orientation == 0)
				EXIF.setAttribute(ExifInterface.TAG_ORIENTATION, "1");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Bitmap rotateToPortrait(Bitmap bitmap, int ori) {
		Matrix matrix = new Matrix();
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if (ori == ExifInterface.ORIENTATION_ROTATE_90 || ori == ExifInterface.ORIENTATION_ROTATE_270
				|| ori == ExifInterface.ORIENTATION_TRANSPOSE || ori == ExifInterface.ORIENTATION_TRANSVERSE) {
			int tmp = w;
			w = h;
			h = tmp;
		}
		switch (ori) {
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
		case ExifInterface.ORIENTATION_NORMAL:
		default:
			return bitmap;
		}

		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}


	public static Matrix getRotateMatrix(Bitmap bitmap, int ori) {
		Matrix matrix = new Matrix();
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if (ori == ExifInterface.ORIENTATION_ROTATE_90 || ori == ExifInterface.ORIENTATION_ROTATE_270
				|| ori == ExifInterface.ORIENTATION_TRANSPOSE || ori == ExifInterface.ORIENTATION_TRANSVERSE) {
			int tmp = w;
			w = h;
			h = tmp;
		}
		switch (ori) {
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
		return matrix;
	}

	public static int computeSampleSize(int width, int height, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(width, height, minSideLength, maxNumOfPixels);

		return initialSize <= 8 ? Utils.nextPowerOf2(initialSize) : (initialSize + 7) / 8 * 8;
	}

	private static int computeInitialSampleSize(int w, int h, int minSideLength, int maxNumOfPixels) {
		if (maxNumOfPixels == UNCONSTRAINED && minSideLength == UNCONSTRAINED)
			return 1;

		int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) FloatMath.ceil(FloatMath.sqrt((float) (w * h)
				/ maxNumOfPixels));

		if (minSideLength == UNCONSTRAINED) {
			return lowerBound;
		} else {
			int sampleSize = Math.min(w / minSideLength, h / minSideLength);
			return Math.max(sampleSize, lowerBound);
		}
	}

	// This computes a sample size which makes the longer side at least
	// minSideLength long. If that's not possible, return 1.
	public static int computeSampleSizeLarger(int w, int h, int minSideLength) {
		int initialSize = Math.max(w / minSideLength, h / minSideLength);
		if (initialSize <= 1)
			return 1;

		return initialSize <= 8 ? Utils.prevPowerOf2(initialSize) : initialSize / 8 * 8;
	}

	// Find the min x that 1 / x >= scale
	public static int computeSampleSizeLarger(float scale) {
		int initialSize = (int) FloatMath.floor(1f / scale);
		if (initialSize <= 1)
			return 1;

		return initialSize <= 8 ? Utils.prevPowerOf2(initialSize) : initialSize / 8 * 8;
	}

	// Find the max x that 1 / x <= scale.
	public static int computeSampleSize(float scale) {
		Utils.assertTrue(scale > 0);
		int initialSize = Math.max(1, (int) FloatMath.ceil(1 / scale));
		return initialSize <= 8 ? Utils.nextPowerOf2(initialSize) : (initialSize + 7) / 8 * 8;
	}

	public static Bitmap resizeBitmapByScale(Bitmap bitmap, float scale, boolean recycle) {
		int width = Math.round(bitmap.getWidth() * scale);
		int height = Math.round(bitmap.getHeight() * scale);
		if (width == bitmap.getWidth() && height == bitmap.getHeight())
			return bitmap;
		Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
		Canvas canvas = new Canvas(target);
		canvas.scale(scale, scale);
		Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		if (recycle)
			bitmap.recycle();
		return target;
	}

	private static Bitmap.Config getConfig(Bitmap bitmap) {
		Bitmap.Config config = bitmap.getConfig();
		if (config == null) {
			config = Bitmap.Config.ARGB_8888;
		}
		return config;
	}

	public static Bitmap resizeDownBySideLength(Bitmap bitmap, int maxLength, boolean recycle) {
		int srcWidth = bitmap.getWidth();
		int srcHeight = bitmap.getHeight();
		float scale = Math.min((float) maxLength / srcWidth, (float) maxLength / srcHeight);
		if (scale >= 1.0f)
			return bitmap;
		return resizeBitmapByScale(bitmap, scale, recycle);
	}

	public static Bitmap resizeAndCropCenter(Bitmap bitmap, int size, boolean recycle) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if (w == size && h == size)
			return bitmap;

		// scale the image so that the shorter side equals to the target;
		// the longer side will be center-cropped.
		float scale = (float) size / Math.min(w, h);

		Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
		int width = Math.round(scale * bitmap.getWidth());
		int height = Math.round(scale * bitmap.getHeight());
		Canvas canvas = new Canvas(target);
		canvas.translate((size - width) / 2f, (size - height) / 2f);
		canvas.scale(scale, scale);
		Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		if (recycle)
			bitmap.recycle();
		return target;
	}

    public static ImageInfo getImageInfo(String path) {
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setOrientation(getOrientation(path));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        imageInfo.setHeight(options.outHeight);
        imageInfo.setWidth(options.outWidth);

        return imageInfo;
    }

    public static ImageInfo getImageInfo(File file) {
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setOrientation(getOrientation(file));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(file.getPath(), options);

        imageInfo.setHeight(options.outHeight);
        imageInfo.setWidth(options.outWidth);

        return imageInfo;
    }

	public static void recycleSilently(Bitmap bitmap) {
		if (bitmap == null)
			return;
		try {
			bitmap.recycle();
		} catch (Throwable t) {
			Log.w(TAG, "unable recycle bitmap", t);
		}
	}

	public static Bitmap rotateBitmap(Bitmap source, int rotation, boolean recycle) {
		if (rotation == 0)
			return source;
		int w = source.getWidth();
		int h = source.getHeight();
		Matrix m = new Matrix();
		m.postRotate(rotation);
		Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, w, h, m, true);
		if (recycle)
			source.recycle();
		return bitmap;
	}

	public static Bitmap createVideoThumbnail(String filePath) {
		// MediaMetadataRetriever is available on API Level 8
		// but is hidden until API Level 10
		Class<?> clazz = null;
		Object instance = null;
		try {
			clazz = Class.forName("android.media.MediaMetadataRetriever");
			instance = clazz.newInstance();

			Method method = clazz.getMethod("setDataSource", String.class);
			method.invoke(instance, filePath);

			// The method name changes between API Level 9 and 10.
			if (Build.VERSION.SDK_INT <= 9) {
				return (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
			} else {
				byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
				if (data != null) {
					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					if (bitmap != null)
						return bitmap;
				}
				return (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
			}
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} catch (InstantiationException e) {
			Log.e(TAG, "createVideoThumbnail", e);
		} catch (InvocationTargetException e) {
			Log.e(TAG, "createVideoThumbnail", e);
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "createVideoThumbnail", e);
		} catch (NoSuchMethodException e) {
			Log.e(TAG, "createVideoThumbnail", e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "createVideoThumbnail", e);
		} finally {
			try {
				if (instance != null) {
					clazz.getMethod("release").invoke(instance);
				}
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	public static byte[] compressToBytes(Bitmap bitmap) {
		return compressToBytes(bitmap, DEFAULT_JPEG_QUALITY);
	}

	public static byte[] compressToBytes(Bitmap bitmap, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
		bitmap.compress(CompressFormat.JPEG, quality, baos);
		return baos.toByteArray();
	}

	public static boolean isSupportedByRegionDecoder(String mimeType) {
		if (mimeType == null)
			return false;
		mimeType = mimeType.toLowerCase(Locale.getDefault());
		return mimeType.startsWith("image/") && (!mimeType.equals("image/gif") && !mimeType.endsWith("bmp"));
	}

	public static boolean isRotationSupported(String mimeType) {
		if (mimeType == null)
			return false;
		mimeType = mimeType.toLowerCase(Locale.getDefault());
		return mimeType.equals("image/jpeg");
	}
}
