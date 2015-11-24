package com.sea_monster.network;

import com.sea_monster.common.RequestProcess;
import com.sea_monster.exception.BaseException;

public interface RequestCallback<T> {
	public void onComplete(RequestProcess<T> request, T obj);

	public void onFailure(RequestProcess<T> request, BaseException e);
}
