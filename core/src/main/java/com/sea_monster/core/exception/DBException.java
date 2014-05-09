package com.sea_monster.core.exception;

public class DBException extends BaseException {
	private static final long serialVersionUID = 1L;

	public DBException(String message) {
		super(message);
	}

	public DBException(Throwable throwable) {
		super(throwable);
	}

	@Override
	public String toString() {
		return "数据库出错！";
	}

}
