package me.add1.common;


import me.add1.exception.BaseException;

public interface RequestProcess<T> {
	public void onComplete(T obj);

	public void onFailure(BaseException e);
}
