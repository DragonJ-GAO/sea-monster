package me.add1.network;


import me.add1.common.RequestProcess;

public interface HttpRequestProcess<T> extends RequestProcess<T> {

	public void processReadyRequest(HttpRequest request);
}
