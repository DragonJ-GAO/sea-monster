package com.sea_monster.exception;

public class ParseException extends BaseException {

	private static final long serialVersionUID = 1L;

	public ParseException(String message) {
		super(message);
	}

	public ParseException(Throwable throwable) {
		super(throwable);
	}

	@Override
	public String toString() {

		return "解析出错！";
	}
}
