package com.sea_monster.network;

import com.sea_monster.exception.BaseException;

public interface RequestCallback<T> {
	public void onComplete(AbstractHttpRequest<T> request, T obj);

	public void onFailure(AbstractHttpRequest<T> request, BaseException e);
}
