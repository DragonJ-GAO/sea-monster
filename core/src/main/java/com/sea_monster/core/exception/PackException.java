package com.sea_monster.core.exception;

public class PackException extends BaseException {
	private static final long serialVersionUID = 1L;

	public PackException(String message) {
		super(message);
	}

	public PackException(Throwable throwable) {
		super(throwable);
	}

	@Override
	public String toString() {
		return "包装出错！";
	}
}
