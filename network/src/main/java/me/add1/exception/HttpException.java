package me.add1.exception;

public class HttpException extends BaseException {

    private static final long serialVersionUID = 1L;

    private int code;

    public HttpException(String message) {
        super(message);
    }

    public HttpException(int code, String message) {
        super(message);
        this.code = code;
    }

    public HttpException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public String toString() {
        return "网络连接出现异常";
    }
}
