package com.sea_monster.core.exception;

public class HttpException extends BaseException {

	private static final long serialVersionUID = 1L;

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable throwable) {
		super(throwable);
	}

	@Override
	public String toString() {
		return "网络连接出现异常";
	}
}
