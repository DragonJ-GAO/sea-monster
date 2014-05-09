package com.sea_monster.core.resource.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.util.Log;

import com.sea_monster.core.exception.BaseException;
import com.sea_monster.core.resource.compress.AbstractCompressRequest;
import com.sea_monster.core.resource.compress.IResourceCompressHandler;
import com.sea_monster.core.resource.io.IFileSysHandler;
import com.sea_monster.core.resource.model.CompressOptions;
import com.sea_monster.core.resource.model.CompressedResource;
import com.sea_monster.core.resource.model.LocalMicroResource;
import com.sea_monster.core.resource.model.Resource;
import com.sea_monster.core.utils.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class ResourceCacheWrapper {
	BitmapLruCache mCache;
	Context mContext;
	IFileSysHandler mFileSysHandler;
	IResourceCompressHandler mCompressHandler;
	private Map<CompressedResource, AbstractCompressRequest> mCompressMapping;

	private Options obtainOptions() {
		Options options;
		options = new Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		return options;
	}

	public ResourceCacheWrapper(Context context, BitmapLruCache cache, IFileSysHandler fileSysHandler, IResourceCompressHandler compressHandler) {
		this.mCache = cache;
		this.mContext = context;
		this.mFileSysHandler = fileSysHandler;
		this.mCompressHandler = compressHandler;
		mCompressMapping = new HashMap<CompressedResource, AbstractCompressRequest>();
	}

	public boolean contains(Resource resource) {
		return mCache.contains(resource.getUri().toString());
	}

	public boolean containsInDiskCache(Resource resource) {
		return mCache.containsInDiskCache(resource.getUri().toString());
	}

	public boolean containsInMemoryCache(Resource resource) {
		return mCache.containsInMemoryCache(resource.getUri().toString());
	}

	public CacheableBitmapDrawable get(final Resource res) {
		return mCache.get(res.getUri().toString(), obtainOptions());
	}

	public CacheableBitmapDrawable getFromDiskCache(final Resource res) {
		return mCache.getFromDiskCache(res.getUri().toString(), obtainOptions());
	}

	public CacheableBitmapDrawable getFromMemoryCache(final Resource res) {
		return mCache.getFromMemoryCache(res.getUri().toString());
	}

	public BitmapDrawable buildCompareBitmap(final CompressedResource res) {

		Log.d("buildCompareBitmap", res.getUri().toString());

		final CompressedResource resource = res;
		CompressOptions options = null;
		options = getCompressOptions(resource);

		String path = null;
		if (res.getUri().getScheme().equals("file")) {
			path = res.getOriResource().getUri().getPath();
		} else {
			path = mFileSysHandler.getPath(res.getOriResource().getUri());
		}
		if (!mCompressMapping.containsKey(resource)) {
			AbstractCompressRequest compressRequest = new AbstractCompressRequest(resource.getOriResource(), path, options) {

				@Override
				public void onFailure(BaseException e) {
					mCompressMapping.remove(resource);
					e.printStackTrace();
				}

				@Override
				public void onComplete(Bitmap obj) {
					mCompressMapping.remove(resource);

					synchronized (obj) {
						mCache.put(resource.getUri().toString(), obj);
					}
				}
			};
			mCompressMapping.put(resource, compressRequest);

			try {
				if (options.isCrop())
                    mCompressHandler.compressResourceWithCrop(compressRequest);
				else
					mCompressHandler.compressResource(compressRequest);
			} catch (Exception e) {
				compressRequest.onFailure(new BaseException(e));
			}
			return mCache.get(res.getUri().toString());
		} else {
			return null;
		}
	}

	public BitmapDrawable buildCompareBitmap(final LocalMicroResource res) {
		Log.d("buildCompareBitmap", res.getUri().toString());
		Bitmap result = null;
		Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(), res.getImageId(), MediaStore.Images.Thumbnails.MICRO_KIND,
				obtainOptions());

		ExifInterface exifInterface;
		try {
			exifInterface = new ExifInterface(res.getUri().getPath());

			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
			if (orientation > 1) {
				Matrix matrix = new Matrix();
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();
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

				result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			} else {
				result = bitmap;
			}
		} catch (IOException e) {
			result = bitmap;
			e.printStackTrace();
		}

        return mCache.put(res.getUri().toString(), result);
	}

	public CompressOptions getCompressOptions(CompressedResource resource) {
		Options options = new Options();
		options.inJustDecodeBounds = true;

		String path;
		if (resource.getOriResource().getUri().getScheme().equals("file")) {
			resource.getOriResource().setOrientaion(ImageUtils.getOrientation(resource.getOriResource().getUri().getPath()));
			path = resource.getOriResource().getUri().getPath();
		} else {
			resource.getOriResource().setOrientaion(ImageUtils.getOrientation(mFileSysHandler.getFile(resource.getOriResource().getUri())));
			path = mFileSysHandler.getPath(resource.getOriResource().getUri());
		}

		BitmapFactory.decodeFile(path, options);
		int oriHeight = options.outHeight;
		int oriWidth = options.outWidth;

		CompressOptions compressOption = new CompressOptions(resource.getWidth(), resource.getHeight(), oriWidth, oriHeight, resource.getOriResource()
				.getOrientaion(), resource.isCrop());

		return compressOption;
	}

	public File getCompressFile(CompressedResource res, String target) throws BaseException {
		Bitmap bitmap = null;
		final CompressedResource resource = res;
		CompressOptions options = getCompressOptions(resource);
		String path = null;
		if (res.getOriResource().getUri().getScheme().equals("file")) {
			path = res.getOriResource().getUri().getPath();
		} else {
			path = mFileSysHandler.getPath(res.getOriResource().getUri());
		}
		if (!mCompressMapping.containsKey(res)) {
			AbstractCompressRequest compressRequest = new AbstractCompressRequest(resource.getOriResource(), path, options) {

				@Override
				public void onFailure(BaseException e) {
					mCompressMapping.remove(resource);
					mFileSysHandler.delFile(resource.getUri());
					e.printStackTrace();
				}

				@Override
				public void onComplete(Bitmap obj) {
					mCompressMapping.remove(resource);
				}
			};
			mCompressMapping.put(resource, compressRequest);
			if (options.isCrop())
				bitmap = mCompressHandler.compressResourceWithCrop(compressRequest);
			else
				bitmap = mCompressHandler.compressResource(compressRequest);
			
			if(bitmap == null)
				return null;

			try {
				File file = new File(target);
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(file));
				bitmap.recycle();
				return file;
			} catch (IOException e) {

			}

			return null;
		}
		return null;
	}

}
