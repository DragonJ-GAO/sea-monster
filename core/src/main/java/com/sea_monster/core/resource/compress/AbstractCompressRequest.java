package com.sea_monster.core.resource.compress;

import com.sea_monster.core.resource.model.CompressOptions;
import com.sea_monster.core.resource.model.Resource;

import java.io.InputStream;

public abstract class AbstractCompressRequest implements CompressRequestProcess {

	private Resource resource;
	private InputStream stream;
	private CompressOptions options;

	public AbstractCompressRequest(Resource resource, InputStream stream, CompressOptions options) {
		this.resource = resource;
		this.stream = stream;
		this.options = options;
	}

	public CompressOptions getOptions() {
		return options;
	}

	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public void setOptions(CompressOptions options) {
		this.options = options;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
}
