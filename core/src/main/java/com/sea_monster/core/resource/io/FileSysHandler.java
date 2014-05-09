package com.sea_monster.core.resource.io;

import android.net.Uri;
import android.util.Log;

import com.sea_monster.core.network.StoreStatusCallback;
import com.sea_monster.core.resource.model.StoreStatus;
import com.sea_monster.core.utils.FileUtils;
import com.sea_monster.core.utils.Md5;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ThreadPoolExecutor;

public class FileSysHandler implements IFileSysHandler {
	private static final String TAG = "FileSysHandler";
	public static final String SETTING = "settting";
	private static final int MIN_FILE_SIZE_IN_BYTES = 100;
	private File mStorageDirectory;
	ThreadPoolExecutor executor;
    private File baseDirectory;
	public FileSysHandler(ThreadPoolExecutor executor, File environmentPath, String dirPath, String name) {
		this.executor = executor;
		baseDirectory = new File(environmentPath, dirPath);
		File storageDirectory = new File(baseDirectory, name);
		FileUtils.createDirectory(storageDirectory, true);
		mStorageDirectory = storageDirectory;

		cleanupSimple();
	}

	@Override
	public boolean exists(Uri uri) {
		return getFile(uri).exists();
	}

	public File getFile(Uri uri) {
		if(uri.getScheme().equalsIgnoreCase("file"))
		{			
			return new File(uri.getPath());
		}else {
			return new File(mStorageDirectory.toString() + File.separator + Md5.encode(uri.toString()));

		}
	}

	public File getTempFile(Uri uri) {
		return new File(mStorageDirectory.toString() + File.separator + Md5.encode(uri.toString()) + ".tmp");
	}

	public String getPath(Uri uri) {
		if(uri.getScheme().equalsIgnoreCase("file"))
			return uri.getPath();
		else
		return mStorageDirectory.toString() + File.separator + Md5.encode(uri.toString());
	}

	public InputStream getInputStream(Uri uri) throws IOException {
		return (InputStream) new FileInputStream(getFile(uri));
	}

	public File store(Uri uri, InputStream is) throws IOException {

		is = new BufferedInputStream(is);
		File result = getTempFile(uri);
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(result));

			byte[] b = new byte[2048];
			int count;

			while ((count = is.read(b)) > 0) {
				os.write(b, 0, count);
			}
			os.close();
			File f = getFile(uri);
			result.renameTo(f);
			result = f;
		} catch (IOException e) {
			delFile(result);
			throw e;
		}

		return result;
	}

	@Override
	public File store(Uri uri, InputStream is, long contentLength, StoreStatusCallback statusCallback) throws IOException {

		is = new BufferedInputStream(is);
		File result = getTempFile(uri);
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(result));

			StoreStatus status = new StoreStatus((int) contentLength);

			byte[] b = new byte[2048];
			int count;

			while ((count = is.read(b)) > 0) {
				os.write(b, 0, count);
				status.addReceivedSize(count);

				statusCallback.statusCallback(status);
			}
			os.close();

			// result.renameTo(getFile(uri));

			File f = getFile(uri);
			result.renameTo(f);
			result = f;

		} catch (IOException e) {
			delFile(result);
			throw e;
		}

		return result;
	}

	public void invalidate(Uri uri) {
		getFile(uri).delete();
	}

	public void cleanup() {
		String[] children = mStorageDirectory.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				File child = new File(mStorageDirectory, children[i]);
				if (!child.equals(new File(mStorageDirectory, FileUtils.NOMEDIA)) && child.length() <= MIN_FILE_SIZE_IN_BYTES) {
					child.delete();
				}
			}
		}
	}

	public void cleanupSimple() {
		final int maxNumFiles = 1000;
		final int numFilesToDelete = 50;

		String[] children = mStorageDirectory.list();
		if (children != null) {

			if (children.length > maxNumFiles) {

				for (int i = children.length - 1, m = i - numFilesToDelete; i > m; i--) {
					File child = new File(mStorageDirectory, children[i]);

					child.delete();
				}
			}
		}
	}

	public void clear() {
		delFile(mStorageDirectory);
	}

	public void clear(byte type) {
		String dirName = null;
		switch (type) {
		case 0:
			dirName = "img";
			break;
		case 1:
			dirName = "cache";
			break;
		}
		if (dirName == null)
			return;
		File delDir = new File(mStorageDirectory, dirName);
		delFileOfDir(delDir);
	}

	private void delFile(File path) {
		if (path.isFile()) {
			Log.d(TAG, "Deleting: " + path.getName());
			path.delete();
		} else if (path.isDirectory()) {
			String[] children = path.list();
			for (String string : children) {
				File child = new File(path, string);
				delFile(child);
			}
			path.delete();
		}
	}

	public void delFile(Uri uri) {
		File checkFile = getFile(uri);

		if (checkFile.exists()) {
			delFile(checkFile);
		}
	}

	private void delFileOfDir(File path) {
		if (path.isDirectory()) {
			String[] children = path.list();
			for (String string : children) {
				File child = new File(path, string);
				delFile(child);
			}
		}
	}

	public String saveTo(File file, String name) {
		File newFile = new File(mStorageDirectory, name);
		file.renameTo(newFile);

		return newFile.getPath();
	}

	public File getWorldWideFile() throws IOException {

		File file = new File(baseDirectory.toString() + File.separator + System.currentTimeMillis() + ".jpg");

		file.createNewFile();

		return file;
	}

	@Override
	public void create(Uri uri) throws IOException {
		File newFile = new File(mStorageDirectory, Md5.encode(uri.toString()));
		if (!newFile.exists())
			newFile.createNewFile();
	}

}
