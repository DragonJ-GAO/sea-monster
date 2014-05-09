package com.sea_monster.core.common;


import com.sea_monster.core.exception.BaseException;

public interface RequestProcess<T> {
	public void onComplete(T obj);

	public void onFailure(BaseException e);
}
