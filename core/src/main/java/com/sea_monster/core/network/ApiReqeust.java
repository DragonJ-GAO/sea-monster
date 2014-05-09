package com.sea_monster.core.network;

import android.util.Log;


import com.sea_monster.core.exception.BaseException;
import com.sea_monster.core.network.packer.AbsEntityPacker;
import com.sea_monster.core.network.parser.IEntityParser;

import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public abstract class ApiReqeust<T extends BaseModel> implements ApiCallback<T> {
	private URI uri;
	private List<NameValuePair> params;
	private InputStream resStream;
	private int method;
	private String resName;
	private String fileName;

	public final static int GET_METHOD = 1;
	public final static int POST_METHOD = 2;
	public final static int PUT_METHOD = 3;

	public ApiReqeust(int method, URI uri, List<NameValuePair> params) {
		if (method == GET_METHOD) {
			this.method = method;

			StringBuilder path = new StringBuilder(uri.toString());
			path.append("?");
			int i = 0, n = 0;

			if (params != null)
				n = params.size();

			for (NameValuePair nameValuePair : params) {
				path.append(String.format("%1$s=%2$s", nameValuePair.getName(),
						URLEncoder.encode(nameValuePair.getValue())));
				i++;
				if (n > i)
					path.append("&");
			}
			try {
				this.uri = new URI(path.toString());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			Log.d("url", path.toString());
			this.params = null;
		} else {
			this.method = method;
			this.uri = uri;
			this.params = params;
		}
	}

	public ApiReqeust(int method, URI uri) {
		this.method = method;
		this.uri = uri;
	}

	public ApiReqeust(int method, URI uri, List<NameValuePair> params, InputStream resStream) {
		this.method = method;
		this.uri = uri;
		this.params = params;
		this.resStream = resStream;
	}

	public ApiReqeust(int method, URI uri, List<NameValuePair> params, InputStream resStream, String resName) {
		this.method = method;
		this.uri = uri;
		this.params = params;
		this.resStream = resStream;
		this.resName = resName;
	}
	
	public ApiReqeust(int method, URI uri, List<NameValuePair> params, InputStream resStream, String name, String fileName) {
		this.method = method;
		this.uri = uri;
		this.params = params;
		this.resStream = resStream;
		this.resName = name;
		this.fileName = fileName;
	}

	public ApiReqeust(int method, URI uri, InputStream resStream) {
		this.method = method;
		this.uri = uri;
		this.resStream = resStream;
	}

	public ApiReqeust(int method, URI uri, InputStream resStream, String resName) {
		this.method = method;
		this.uri = uri;
		this.resStream = resStream;
		this.resName = resName;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public List<NameValuePair> getParams() {
		return params;
	}

	public void setParams(List<NameValuePair> params) {
		this.params = params;
	}

	public String getResName() {
		return resName;
	}

	public void setResName(String resName) {
		this.resName = resName;
	}

	public InputStream getResStream() {
		return resStream;
	}

	public void setResStream(InputStream resStream) {
		this.resStream = resStream;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("URI:%1$s\n", uri.toString()));

		int i = 0;

		for (NameValuePair pair : params) {
			stringBuilder.append(String.format("Params($1$d):%2$s\n", i++, pair.getValue().toString()));
		}

		return super.toString();
	}

	public AbstractHttpRequest<T> obtainRequest(IEntityParser<T> parser, final AuthType consumer) {
		return obtainRequest(parser, consumer, false);
	}

	public AbstractHttpRequest<T> obtainRequest(IEntityParser<T> parser, final AuthType consumer, boolean isMulitPart) {
		final ApiReqeust<T> request = this;

		AbstractHttpRequest<T> httpRequest = new AbstractHttpRequest<T>(method, uri, params, AbstractHttpRequest.HIGH,
				isMulitPart) {

			@Override
			public void processReadyRequest(HttpRequest request) {

				if (consumer != null)
					try {
						synchronized (consumer) {
							consumer.signRequest(request, params);
						}
					} catch (InvalidKeyException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
			}

			@Override
			public void onComplete(T obj) {
				request.onComplete(this, obj);
			}

			@Override
			public void onFailure(BaseException e) {
				request.onFailure(this, e);
			}
		};
		httpRequest.setParser(parser);
		if (request.resStream != null) {
			httpRequest.setResStream(resStream);
		}

		if (request.resName != null) {
			httpRequest.setResName(request.resName);
		}
		if (request.fileName != null) {
			httpRequest.setFileName(request.fileName);
		}
		return httpRequest;
	}

	public AbstractHttpRequest<T> obtainRequest(IEntityParser<T> parser, AbsEntityPacker<?> packer,
			final AuthType consumer) {
		final ApiReqeust<T> request = this;

		AbstractHttpRequest<T> httpRequest = new AbstractHttpRequest<T>(method, uri, params, AbstractHttpRequest.HIGH) {

			@Override
			public void processReadyRequest(HttpRequest request) {
				if (consumer != null)
					try {

						synchronized (consumer) {
							consumer.signRequest(request, params);
						}
					} catch (InvalidKeyException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
			}

			@Override
			public void onComplete(T obj) {
				request.onComplete(this, obj);

			}

			@Override
			public void onFailure( BaseException e) {
				request.onFailure(this,  e);
			}
		};
		httpRequest.setParser(parser);
		httpRequest.setPacker(packer);
		if (request.resStream != null) {
			httpRequest.setResStream(resStream);
		}

		if (request.resName != null) {
			httpRequest.setResName(request.resName);
		}
		if (request.fileName != null) {
			httpRequest.setFileName(request.fileName);
		}
		return httpRequest;
	}
}
