package com.sea_monster.network;


import com.sea_monster.common.RequestProcess;

public interface HttpRequestProcess<T> extends RequestProcess<T> {

	public void processReadyRequest(HttpRequest request);
}
