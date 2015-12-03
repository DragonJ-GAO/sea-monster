package me.add1.exception;

public class BaseException extends Exception {

	private static final long serialVersionUID = 1L;

	public BaseException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public BaseException(Throwable throwable) {
		super(throwable);

	}

	public BaseException(String message) {
		super(message);
	}
}
