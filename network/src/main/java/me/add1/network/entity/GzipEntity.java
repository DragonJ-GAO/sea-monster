package me.add1.network.entity;

import org.apache.http.HttpEntity;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GzipEntity extends AbstractHttpEntity {

	HttpEntity mEntity;

	public GzipEntity(HttpEntity entity) {
		mEntity = entity;
		setContentType("gzip");
	}

	@Override
	public boolean isRepeatable() {
		return mEntity.isRepeatable();
	}

	@Override
	public long getContentLength() {
		return mEntity.getContentLength();
	}

	@Override
	public boolean isChunked() {
		return true;
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return mEntity.getContent();
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outstream);
		mEntity.writeTo(gzipOutputStream);
	}

	@Override
	public boolean isStreaming() {
		return mEntity.isStreaming();
	}

}
