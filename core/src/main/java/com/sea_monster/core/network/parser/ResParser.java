package com.sea_monster.core.network.parser;


import com.sea_monster.core.exception.InternalException;
import com.sea_monster.core.exception.ParseException;
import com.sea_monster.core.network.StatusCallback;
import com.sea_monster.core.network.StoreStatusCallback;
import com.sea_monster.core.resource.io.IFileSysHandler;
import com.sea_monster.core.resource.model.Resource;

import org.apache.http.HttpEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class ResParser implements IEntityParser<File> {

	private Resource res;
	private IFileSysHandler fileHandler;

	@Override
	public File parse(HttpEntity entity) throws IOException, ParseException {

		return parseEntityStream(entity.getContent());
	}

	public ResParser(Resource res, IFileSysHandler fileSysHandler) {
		this.res = res;
		this.fileHandler = fileSysHandler;
	}

	private File parseEntityStream(InputStream inputStream, long cotentLength, StoreStatusCallback callback) throws IOException {
		File file = fileHandler.store(res.getUri(), inputStream, cotentLength, callback);
		return file;

	}

	private File parseEntityStream(InputStream inputStream) throws IOException {
		File file = fileHandler.store(res.getUri(), inputStream);
		return file;
	}

	@Override
	public File parse(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException {
		File file = null;
		if (callback instanceof StoreStatusCallback)
			file = parseEntityStream(entity.getContent(), entity.getContentLength(), (StoreStatusCallback) callback);
		else
			file = parseEntityStream(entity.getContent());
		entity.consumeContent();
		return file;
	}

	@Override
	public File parseGzip(HttpEntity entity) throws IOException, ParseException, InternalException {
		return parseEntityStream(new GZIPInputStream(entity.getContent()));
	}

	@Override
	public File parseGzip(HttpEntity entity, StatusCallback<?> callback) throws IOException, ParseException, InternalException {
		File file = null;
		if (callback instanceof StoreStatusCallback)
			file = parseEntityStream(new GZIPInputStream(entity.getContent()), entity.getContentLength(), (StoreStatusCallback) callback);
		else
			file = parseEntityStream(new GZIPInputStream(entity.getContent()));
		entity.consumeContent();
		return file;
	}

}
