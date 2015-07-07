package com.sea_monster.network;

import com.sea_monster.exception.BaseException;

public interface HttpHandler {
	public <T> int executeRequest(AbstractHttpRequest<T> request);

	public <T> T executeRequestSync(AbstractHttpRequest<T> request) throws BaseException;

	public void cancelRequest(AbstractHttpRequest<?> request);

	public void cancelRequest();
}
