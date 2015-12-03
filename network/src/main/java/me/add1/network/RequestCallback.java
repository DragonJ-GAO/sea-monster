package me.add1.network;

import me.add1.common.RequestProcess;
import me.add1.exception.BaseException;

public interface RequestCallback<T> {
	public void onComplete(RequestProcess<T> request, T obj);

	public void onFailure(RequestProcess<T> request, BaseException e);
}
