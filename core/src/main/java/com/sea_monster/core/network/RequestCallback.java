package com.sea_monster.core.network;

import com.sea_monster.core.exception.BaseException;

public interface RequestCallback<T> {
	public void onComplete(AbstractHttpRequest<T> request, T obj);

	public void onFailure(AbstractHttpRequest<T> request, BaseException e);
}
