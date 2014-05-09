package com.sea_monster.core.resource.compress;

import com.sea_monster.core.resource.model.CompressOptions;
import com.sea_monster.core.resource.model.Resource;

public abstract class AbstractCompressRequest implements CompressRequestProcess {

	private Resource resource;
	private String path;
	private CompressOptions options;

	public AbstractCompressRequest(Resource resource, String path, CompressOptions options) {
		this.resource = resource;
		this.path = path;
		this.options = options;
	}

	public CompressOptions getOptions() {
		return options;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
