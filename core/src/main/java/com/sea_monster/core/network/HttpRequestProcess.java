package com.sea_monster.core.network;

import com.sea_monster.core.common.RequestProcess;

import org.apache.http.HttpRequest;

public interface HttpRequestProcess<T> extends RequestProcess<T> {

	public void processReadyRequest(HttpRequest request);
}
