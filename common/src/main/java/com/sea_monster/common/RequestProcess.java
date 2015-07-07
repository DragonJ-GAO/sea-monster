package com.sea_monster.common;


import com.sea_monster.exception.BaseException;

public interface RequestProcess<T> {
	public void onComplete(T obj);

	public void onFailure(BaseException e);
}
