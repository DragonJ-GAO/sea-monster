package com.sea_monster.core.network;

import com.sea_monster.core.common.Const;
import com.sea_monster.core.exception.BaseException;
import com.sea_monster.core.exception.InternalException;
import com.sea_monster.core.exception.PackException;
import com.sea_monster.core.network.entity.GzipEntity;
import com.sea_monster.core.network.entity.MultipartEntity;
import com.sea_monster.core.network.packer.AbsEntityPacker;
import com.sea_monster.core.network.parser.IEntityParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class AbstractHttpRequest<T> implements HttpRequestProcess<T> {
	public static final int NORMAL = 0;
	public static final int LOW = -1;
	public static final int HIGH = 1;

	private int callId;
	private int priority;
	private URI uri;
	private List<NameValuePair> params;
	private InputStream resStream;
	private boolean isMultiPart;
	private String resName;
	private String fileName;
	private int method;
	private IEntityParser<?> parser;
	private AbsEntityPacker<?> packer;
	private boolean supportGzip = false;;

	private Map<String, Object> attrs;

	private StatusCallback<?> statusCallback;

	public StatusCallback<?> getStatusCallback() {
		return statusCallback;
	}

	public void setStatusCallback(StatusCallback<?> statusCallback) {
		this.statusCallback = statusCallback;
	}

	public boolean isSupportGzip() {
		return supportGzip;
	}

	public void setSupportGzip(boolean supportGzip) {
		this.supportGzip = supportGzip;
	}

	public final static int GET_METHOD = 1;
	public final static int POST_METHOD = 2;
	public final static int PUT_METHOD = 3;

	public AbstractHttpRequest(int method, URI uri, List<NameValuePair> params, int priority, boolean isMultiPart) {
		this.method = method;
		this.uri = uri;
		this.params = params;
		this.callId = new Random().nextInt();
		this.priority = priority;
		this.isMultiPart = isMultiPart;
	}

	public AbstractHttpRequest(int method, URI uri, List<NameValuePair> params, IEntityParser<?> parser) {
		this.method = method;
		this.uri = uri;
		this.params = params;
		this.parser = parser;
		this.callId = new Random().nextInt();
		this.priority = NORMAL;
		isMultiPart = false;

	}

	public AbstractHttpRequest(int method, URI uri, List<NameValuePair> params) {
		this.method = method;
		this.uri = uri;
		this.params = params;
		this.callId = new Random().nextInt();
		this.priority = NORMAL;
		isMultiPart = false;
	}

	public AbstractHttpRequest(int method, URI uri, List<NameValuePair> params, IEntityParser<?> parser, int priority) {
		this.method = method;
		this.uri = uri;
		this.params = params;
		this.parser = parser;
		this.callId = new Random().nextInt();
		this.priority = priority;
		isMultiPart = false;
	}

	public AbstractHttpRequest(int method, URI uri, List<NameValuePair> params, int priority) {
		this.method = method;
		this.uri = uri;
		this.params = params;
		this.callId = new Random().nextInt();
		this.priority = priority;
		isMultiPart = false;
	}

	public void cancelRequest(BaseException e) {
		this.onFailure(e);
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public int getCallId() {
		return callId;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public List<NameValuePair> getParamsMap() {
		return params;
	}

	public void setParamsMap(List<NameValuePair> params) {
		this.params = params;
	}

	public IEntityParser<?> getParser() {
		return parser;
	}

	public void setParser(IEntityParser<?> parser) {
		this.parser = parser;
	}

	public AbsEntityPacker<?> getPacker() {
		return packer;
	}

	public void setPacker(AbsEntityPacker<?> packer) {
		this.packer = packer;
	}

	public void setResStream(InputStream inputStream) {
		this.resStream = inputStream;
	}

	public void setResName(String resName) {
		this.resName = resName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public InputStream getResStream() {
		return this.resStream;
	}

	public void putAttr(String name, Object obj) {
		if (attrs == null) {
			attrs = new HashMap<String, Object>();
		}
		attrs.put(name, obj);
	}

	public boolean containsAttr(String name) {
		if (attrs == null)
			return false;
		return attrs.containsKey(name);
	}

	@SuppressWarnings("unchecked")
	public <K extends Object> K getAttr(String name) {
		if (attrs == null)
			return null;
		if (!attrs.containsKey(name))
			return null;
		return (K) attrs.get(name);
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("CallId:%1$d\n", callId));
		stringBuilder.append(String.format("URI:%1$s\n", uri.toString()));

		int i = 0;

		for (NameValuePair pair : params) {
			stringBuilder.append(String.format("Params($1$d):%2$s\n", i++, pair.getValue().toString()));
		}

		return super.toString();
	}

	public HttpUriRequest obtainRequest() throws InternalException, PackException {

		if (this.method == GET_METHOD) {
			HttpGet request = new HttpGet(this.uri);
			processReadyRequest(request);
			return request;
		} else {
			HttpPost request = new HttpPost(this.uri);

			processReadyRequest(request);

			if (this.params != null && this.params.size() > 0 || resStream != null || packer != null)

				try {
					HttpEntity entity = null;
					if (resStream != null) {
						if (resName != null) {
							if (fileName != null)
								entity = new MultipartEntity(this.params, this.resStream, this.resName, this.fileName, HTTP.UTF_8);
							else
								entity = new MultipartEntity(this.params, this.resStream, this.resName, Const.DEF.POST_FILE_NAME, HTTP.UTF_8);
						} else {
							entity = new MultipartEntity(this.params, this.resStream, Const.DEF.POST_NAME, Const.DEF.POST_FILE_NAME, HTTP.UTF_8);
						}
					} else if (this.packer != null) {
						try {
							entity = packer.pack();
						} catch (IOException e) {
							throw new PackException(e);
						} catch (JSONException e) {
							throw new PackException(e);
						}
					} else if (this.params != null && this.params.size() > 0) {
						if (isMultiPart)
							entity = new MultipartEntity(params, HTTP.UTF_8);
						// request.setEntity(new MultipartEntity(params,
						// this.resStream, "", HTTP.UTF_8));
						else
							entity = new UrlEncodedFormEntity(this.params, HTTP.UTF_8);
					}

					if (supportGzip) {
						request.setEntity(new GzipEntity(entity));
					} else {
						request.setEntity(entity);
					}

				} catch (UnsupportedEncodingException e) {
					throw new PackException(e);

				} catch (InternalException e) {
					throw e;
				}
			request.getParams().toString();
			return request;
		}
	}

	public int getPriority() {
		return this.priority;
	}

	@Override
	public void processReadyRequest(HttpRequest request) {

	}
}
