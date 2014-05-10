package com.sea_monster.core.resource.compress;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.sea_monster.core.exception.BaseException;
import com.sea_monster.core.resource.io.IFileSysHandler;
import com.sea_monster.core.resource.model.LocalResource;

/**
 * Created by DragonJ on 13-7-3.
 */
public class ResourceCompressHandler implements IResourceCompressHandler {

	private Context context;
	private IFileSysHandler fileSysHandler;

	public ResourceCompressHandler(Context context, IFileSysHandler fileSysHandler) {
		this.context = context;
		this.fileSysHandler = fileSysHandler;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
	@Override
	public Bitmap compressResourceWithCrop(AbstractCompressRequest request) {

		Bitmap result = null;
		if (request.getResource() instanceof LocalResource) {
			BitmapFactory.Options options = new BitmapFactory.Options();

			options.inPreferredConfig = Bitmap.Config.RGB_565;
			Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), ((LocalResource) request.getResource()).getImageId(),
					MediaStore.Images.Thumbnails.MINI_KIND, options);
			Matrix matrix = new Matrix();

			int w = bitmap.getWidth();
			int h = bitmap.getHeight();

			if (request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_90
					|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_270
					|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSPOSE
					|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSVERSE) {
				int tmp = w;
				w = h;
				h = tmp;

			}
			switch (request.getOptions().getOrientation()) {
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

			if (request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_90
					|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_270
					|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSPOSE
					|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSVERSE) {
				int tmp = request.getOptions().getThumbWidth();
				request.getOptions().setThumbWidth(request.getOptions().getThumbHeight());
				;
				request.getOptions().setThumbHeight(tmp);
			}

			int x = 0, y = 0, width, height;

			if (request.getOptions().getThumbWidth() > bitmap.getWidth() && request.getOptions().getThumbHeight() > bitmap.getHeight()) {
				height = bitmap.getHeight();
				width = bitmap.getWidth();
			} else if (request.getOptions().getThumbWidth() > bitmap.getWidth()) {
				height = (bitmap.getWidth() * request.getOptions().getThumbHeight()) / request.getOptions().getThumbWidth();
				width = bitmap.getWidth();
				x = 0;
				y = bitmap.getHeight() - request.getOptions().getThumbHeight();
				y >>= 1;
			} else if (request.getOptions().getThumbHeight() > bitmap.getHeight()) {
				width = (bitmap.getHeight() * request.getOptions().getThumbWidth()) / request.getOptions().getThumbHeight();
				height = bitmap.getHeight();
				y = 0;
				x = bitmap.getWidth() - request.getOptions().getThumbWidth();
				x >>= 1;

			} else {

				int widthMix = request.getOptions().getThumbWidth() * bitmap.getHeight();
				int heightMix = request.getOptions().getThumbHeight() * bitmap.getWidth();
				if (widthMix == heightMix) {
					width = request.getOptions().getThumbWidth();
					height = request.getOptions().getThumbHeight();

				} else if (widthMix > heightMix) {
					height = heightMix / request.getOptions().getThumbWidth();
					width = request.getOptions().getThumbWidth();
					y = bitmap.getHeight() - height;
					y >>= 1;
				} else {
					width = widthMix / request.getOptions().getThumbHeight();
					height = request.getOptions().getThumbHeight();
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
			if (request.getOptions().isCrop())
				result = Bitmap.createBitmap(bitmap, x, y, bitmap.getWidth() - (x << 1), bitmap.getHeight() - (y << 1), matrix, true);
			else
				result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

		} else {

			if (request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_90
					|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_270
					|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSPOSE
					|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSVERSE) {
				int tmp = request.getOptions().getThumbWidth();
				request.getOptions().setThumbWidth(request.getOptions().getThumbHeight());
				;
				request.getOptions().setThumbHeight(tmp);
			}

			int width = request.getOptions().getSrcWidth();
			int height = request.getOptions().getSrcHeight();
			int sampleW = 1, sampleH = 1;
			while (width / 2 > request.getOptions().getThumbWidth()) {
				width /= 2;
				sampleW <<= 1;
			}

			while (height / 2 > request.getOptions().getThumbHeight()) {
				height /= 2;
				sampleH <<= 1;
			}

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			int sampleSize = Math.min(sampleW, sampleH);

			options.inSampleSize = sampleSize;

			try {
				String path = null;
				if (request.getResource().getUri().getScheme().equals("file")) {
					path = request.getResource().getUri().getPath();
				} else {
					path = fileSysHandler.getPath(request.getResource().getUri());
				}

				Bitmap bitmap = null;

				bitmap = BitmapFactory.decodeFile(path, options);
				Log.d("path", path);
				Matrix matrix = new Matrix();

				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				if (request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_90
						|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_270
						|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSPOSE
						|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSVERSE) {
					int tmp = w;
					w = h;
					h = tmp;
				}
				switch (request.getOptions().getOrientation()) {
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

                if (request.getOptions().getThumbWidth() > bitmap.getWidth() && request.getOptions().getThumbHeight() > bitmap.getHeight()) {
                    height = bitmap.getHeight();
                    width = bitmap.getWidth();
                } else if (request.getOptions().getThumbWidth() > bitmap.getWidth()) {
                    height = (bitmap.getWidth() * request.getOptions().getThumbHeight()) / request.getOptions().getThumbWidth();
                    width = bitmap.getWidth();
                    x = 0;
                    y = bitmap.getHeight() - request.getOptions().getThumbHeight();
                    y >>= 1;
                } else if (request.getOptions().getThumbHeight() > bitmap.getHeight()) {
                    width = (bitmap.getHeight() * request.getOptions().getThumbWidth()) / request.getOptions().getThumbHeight();
                    height = bitmap.getHeight();
                    y = 0;
                    x = bitmap.getWidth() - request.getOptions().getThumbWidth();
                    x >>= 1;

                } else {

                    int widthMix = request.getOptions().getThumbWidth() * bitmap.getHeight();
                    int heightMix = request.getOptions().getThumbHeight() * bitmap.getWidth();
                    if (widthMix == heightMix) {
                        width = request.getOptions().getThumbWidth();
                        height = request.getOptions().getThumbHeight();

                    } else if (widthMix > heightMix) {
                        height = heightMix / request.getOptions().getThumbWidth();
                        width = request.getOptions().getThumbWidth();
                        y = bitmap.getHeight() - height;
                        y >>= 1;
                    } else {
                        width = widthMix / request.getOptions().getThumbHeight();
                        height = request.getOptions().getThumbHeight();
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

			} catch (Exception e) {
				request.onFailure(new BaseException(e));
			}
		}
		request.onComplete(result);

		return result;
	}

	@Override
	public Bitmap compressResource(AbstractCompressRequest request) {
		Bitmap result = null;
		if (request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_90
				|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_270
				|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSPOSE
				|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSVERSE) {
			int tmp = request.getOptions().getThumbWidth();
			request.getOptions().setThumbWidth(request.getOptions().getThumbHeight());

			request.getOptions().setThumbHeight(tmp);
		}

		int width = request.getOptions().getSrcWidth();
		int height = request.getOptions().getSrcHeight();
		int sampleW = 1, sampleH = 1;
		while (width / 2 > request.getOptions().getThumbWidth()) {
			width /= 2;
			sampleW <<= 1;

		}

		while (height / 2 > request.getOptions().getThumbHeight()) {
			height /= 2;
			sampleH <<= 1;
		}
		int sampleSize = 1;
		BitmapFactory.Options options = new BitmapFactory.Options();
		if (request.getOptions().getThumbHeight() == Integer.MAX_VALUE || request.getOptions().getThumbWidth() == Integer.MAX_VALUE) {
			sampleSize = Math.max(sampleW, sampleH);
		} else {
			sampleSize = Math.max(sampleW, sampleH);
		}
		options.inSampleSize = sampleSize;
		Log.d("request.getPath()", request.getPath());
		Bitmap bitmap;
		try {
			bitmap = BitmapFactory.decodeFile(request.getPath(), options);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			options.inSampleSize = options.inSampleSize << 1;
			bitmap = BitmapFactory.decodeFile(request.getPath(), options);
		}

		Matrix matrix = new Matrix();
		if (bitmap == null) {
			return bitmap;
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		if (request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_90
				|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_ROTATE_270
				|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSPOSE
				|| request.getOptions().getOrientation() == ExifInterface.ORIENTATION_TRANSVERSE) {
			int tmp = w;
			w = h;
			h = tmp;
		}
		switch (request.getOptions().getOrientation()) {
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
		float xS = (float) request.getOptions().getThumbWidth() / bitmap.getWidth();
		float yS = (float) request.getOptions().getThumbHeight() / bitmap.getHeight();

		matrix.postScale(Math.min(xS, yS), Math.min(xS, yS));
		try {
			result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			Log.e("ResourceCompressHandler", "OOM" + "Height:" + bitmap.getHeight() + "Width:" + bitmap.getHeight() + "matrix:" + xS + " " + yS);
			return null;
		}

		request.onComplete(result);

		return result;
	}
}
