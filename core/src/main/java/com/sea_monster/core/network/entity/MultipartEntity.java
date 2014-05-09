package com.sea_monster.core.network.entity;

import com.sea_monster.core.exception.HttpException;
import com.sea_monster.core.exception.InternalException;
import com.sea_monster.core.network.MultipartUtils;

import org.apache.http.NameValuePair;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

public class MultipartEntity extends AbstractHttpEntity {

	private String mBoundary;
	private byte[] content;
	private byte[] fileSeparator;
	private byte[] endSeparator;
	private byte[] mStream;
	private InputStream inputStream;

	public MultipartEntity(final List<? extends NameValuePair> parameters, byte[] imgStream, String name, 
			String fileName, String charset) throws HttpException {
		mBoundary = UUID.randomUUID().toString();
		if (parameters != null) {
			String formDataString = null;
			try {
				formDataString = MultipartUtils.getFormDataContent(parameters, mBoundary);
			} catch (IllegalArgumentException e) {
				throw new HttpException(e);
			}
			try {
				content = formDataString.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				throw new HttpException(e);
			}
		}

		try {

			fileSeparator = MultipartUtils.getFileSeparator(mBoundary, name, fileName).getBytes(charset);
			endSeparator = MultipartUtils.getEndSeparator(mBoundary).getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new HttpException(e);
		}
		mStream = imgStream;

		setContentType(MultipartUtils.getContentType(mBoundary));
	}

	public MultipartEntity(final List<? extends NameValuePair> parameters, String charset) throws InternalException {
		mBoundary = UUID.randomUUID().toString();

		if (parameters != null)
			try {
				String formDataString = null;
				try {
					formDataString = MultipartUtils.getFormDataContent(parameters, mBoundary);
				} catch (IllegalArgumentException e) {
					throw new InternalException(InternalException.ENTITY_BUILD_EXP, e);
				}
				try {
					content = formDataString.getBytes(charset);

				} catch (UnsupportedEncodingException e) {
					throw new InternalException(InternalException.ENTITY_BUILD_EXP, e);
				}
				endSeparator = MultipartUtils.getEndSeparator(mBoundary).getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		setContentType(MultipartUtils.getContentType(mBoundary));
	}

	public MultipartEntity(final List<? extends NameValuePair> parameters, InputStream inputStream, String name, 
			String fileName, String charset)
			throws InternalException {
		mBoundary = UUID.randomUUID().toString();
		if (parameters != null) {
			String formDataString = null;
			try {
				formDataString = MultipartUtils.getFormDataContent(parameters, mBoundary);
			} catch (IllegalArgumentException e) {
				throw new InternalException(InternalException.ENTITY_BUILD_EXP, e);
			}
			try {
				content = formDataString.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				throw new InternalException(InternalException.ENTITY_BUILD_EXP, e);
			}
		}

		try {
			fileSeparator = MultipartUtils.getFileSeparator(mBoundary, name, fileName).getBytes(charset);
			endSeparator = MultipartUtils.getEndSeparator(mBoundary).getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new InternalException(InternalException.ENTITY_BUILD_EXP, e);
		}
		this.inputStream = inputStream;

		setContentType(MultipartUtils.getContentType(mBoundary));
	}

	@Override
	public boolean isRepeatable() {
		return false;
	}

	@Override
	public long getContentLength() {
		int length = 0;
		try {
			if (inputStream != null) {
				length = (content == null ? 0 : content.length) + fileSeparator.length + endSeparator.length + (mStream == null ? 0 : mStream.length)
						+ inputStream.available();

			} else {
				length = (content == null ? 0 : content.length) + endSeparator.length;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return length;
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {

		return null;
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		if (content != null)
			outstream.write(content);
		if (fileSeparator != null && fileSeparator.length > 0)
			outstream.write(fileSeparator);

		if (inputStream != null) {
			if (mStream == null || mStream.length == 0) {
				int bytesRead = 0;
				byte[] buf = new byte[4 * 1024]; // 4K buffer
				while ((bytesRead = inputStream.read(buf)) != -1) {
					outstream.write(buf, 0, bytesRead);
				}
			} else
				outstream.write(mStream);
		}

		outstream.write(endSeparator);
		outstream.flush();
	}

	@Override
	public boolean isStreaming() {
		// TODO Auto-generated method stub
		return false;
	}

}