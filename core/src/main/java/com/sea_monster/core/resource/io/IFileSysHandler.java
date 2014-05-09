package com.sea_monster.core.resource.io;

import android.net.Uri;

import com.sea_monster.core.network.StoreStatusCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface IFileSysHandler {
	public boolean exists(Uri uri);

	public File getFile(Uri uri);

	public String getPath(Uri uri);

	public InputStream getInputStream(Uri uri) throws IOException;

	public File store(Uri uri, InputStream is) throws IOException;

	public File store(Uri uri, InputStream is, long contentLength, StoreStatusCallback statusCallback)
			throws IOException;

	public void invalidate(Uri uri);

	public void cleanup();

	public void clear();

	public void clear(byte type);

	public void delFile(Uri uri);
	
	public String saveTo(File file, String name);

	public File getWorldWideFile() throws IOException;

	public void create(Uri uri) throws IOException;
}
