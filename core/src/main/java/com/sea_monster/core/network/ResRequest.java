package com.sea_monster.core.network;

import com.sea_monster.core.exception.BaseException;
import com.sea_monster.core.network.parser.ResParser;
import com.sea_monster.core.resource.io.IFileSysHandler;
import com.sea_monster.core.resource.model.Resource;

import org.apache.http.HttpRequest;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class ResRequest implements ResCallback {
	private URI uri;
	private Resource res;
	private IFileSysHandler fileSysHandler;
	private StoreStatusCallback storeStatusCallback;

	public ResRequest(Resource res, IFileSysHandler fileSysHandler) throws URISyntaxException {

		String resScheme = res.getUri().getScheme();

		if (!resScheme.equals("http") && !resScheme.equals("https"))
			throw new URISyntaxException(res.getUri().toString(), "scheme invilidate fail");

		this.res = res;
		this.uri = URI.create(res.getUri().toString());
		this.fileSysHandler = fileSysHandler;
	}

	public ResRequest(Resource res, IFileSysHandler fileSysHandler, StoreStatusCallback callback) throws URISyntaxException {

		this.res = res;

		String resScheme = res.getUri().getScheme();

		if (!resScheme.equals("http") && !resScheme.equals("https"))
			throw new URISyntaxException(res.getUri().toString(), "scheme invilidate fail");

		this.uri = URI.create(res.getUri().toString());
		this.fileSysHandler = fileSysHandler;
		this.storeStatusCallback = callback;

	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public StoreStatusCallback getStoreStatusCallback() {
		return storeStatusCallback;
	}

	public AbstractHttpRequest<File> obtainRequest() {

		final AbstractHttpRequest<File> request = new AbstractHttpRequest<File>(AbstractHttpRequest.GET_METHOD, uri, null) {

			@Override
			public void processReadyRequest(HttpRequest request) {

			}

			@Override
			public void onFailure(BaseException e) {
				ResRequest.this.onFailure(this, e);
				if (storeStatusCallback != null) {
					storeStatusCallback.onFailure(this, e);
				}
			}

			@Override
			public void onComplete(File obj) {
				ResRequest.this.onComplete(this, obj);
				if (storeStatusCallback != null) {
					storeStatusCallback.onComplete(this, obj);
				}
			}
		};

		if (this.storeStatusCallback != null) {
			request.setStatusCallback(this.storeStatusCallback);
		}

		request.setParser(new ResParser(res, fileSysHandler));

		return request;
	}
}
