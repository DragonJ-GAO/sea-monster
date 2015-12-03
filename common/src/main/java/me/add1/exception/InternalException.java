package me.add1.exception;

import java.util.HashMap;

public class InternalException extends BaseException {


	public static final int DEF_NETWORK_CODE = 0xFFFF0000;
	public static final int DEF_LOGIC_CODE = 0xFFFF1000;
	public static final int DEF_OTHER_CODE = 0xFFFF2000;
	public static final int DEF_SERVICE_CODE = 0xFFFF3000;

    public static final int DISCARD_TASK = 0xFFFF1001;
    public static final int NETWORK_DISABLED = 0xFFFF0001;
    public static final int NETWORK_PACKER_ERROR = 0xFFFF0002;

	public static final String DEFAULT_NETWORK_EXP_MESSAGE = "网络请求异常";
	public static final String DEFAULT_LOGIC_EXP_MESSAGE = "操作失败";
	public static final String DEFAULT_OTHER_EXP_MESSAGE = "操作失败";
	public static final String DEFAULT_SERVICE_EXP_MESSAGE = "服务器异常";
	public static final String DEFAULT_EXP_MESSAGE = "操作失败";

	private static HashMap<Integer, String> codeMapping;

	private int code;

	static {

		codeMapping = new HashMap<Integer, String>();
		// 1K网络错误 2K业务错误 3K其他错误 4K服务返回错误
		codeMapping.put(400, "请求无效");
		codeMapping.put(401, "服务器异常，请注销帐号后重新登录");
		codeMapping.put(403, "服务器异常，请注销帐号后重新登录");
        codeMapping.put(404, "请求失败");
        codeMapping.put(500, "服务器异常，请稍后再试");
        codeMapping.put(501, "服务器异常，请稍后再试");
        codeMapping.put(502, "服务器异常，请稍后再试");
        codeMapping.put(503, "服务器正忙，请稍后再试");
        codeMapping.put(504, "服务器请求超时，请稍后再试");

        codeMapping.put(4001, "请求的参数错误");
        codeMapping.put(4002, "发布//更新内容错误");
        codeMapping.put(4003, "XML解析错误");
        codeMapping.put(4004, "传图片错误");
        codeMapping.put(4005, "上传图片大小错误");
        codeMapping.put(4007, "图片压缩错误");
        codeMapping.put(4008, "用户不存在");
        codeMapping.put(4010, "请求的数据不存在");
        codeMapping.put(4011, "URL错误");
        codeMapping.put(4012, "内容包含非法词");
        codeMapping.put(4013, "不能重复绑定服务");

        codeMapping.put(40002, "请求无效");
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public InternalException(int code, String message) {
		super(message);
		this.code = code;
	}

	public InternalException(int code, Throwable throwable) {
		super(throwable);
		this.code = code;
	}

	public InternalException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		if (codeMapping.containsKey(code)) {
			return codeMapping.get(code);
		} else {
			if (code >= 0 && code < 1024) {
				return DEFAULT_NETWORK_EXP_MESSAGE;
			} else if (code >= 1024 && code < 2048) {
				return DEFAULT_LOGIC_EXP_MESSAGE;
			} else if (code >= 2048 && code < 3072) {
				return DEFAULT_OTHER_EXP_MESSAGE;
			} else if (code >= 3072 && code < 4096) {
				return DEFAULT_SERVICE_EXP_MESSAGE;
			}
		}
		return DEFAULT_EXP_MESSAGE;
	}

	public int getGeneralCode() {
		if (codeMapping.containsKey(code)) {
			return code;
		} else {
			if (code >= 0 && code < 1024) {
				return DEF_NETWORK_CODE;
			} else if (code >= 1024 && code < 2048) {
				return DEF_LOGIC_CODE;
			} else if (code >= 2048 && code < 3072) {
				return DEF_OTHER_CODE;
			} else if (code >= 3072 && code < 4096) {
				return DEF_SERVICE_CODE;
			}
		}
		return 0;
	}

	public boolean isSpecial() {
		return !codeMapping.containsKey(code);
	}

	public String getCustomErrorMessage(int specialCode, String specialMessage) {
		if (specialCode == code)
			return specialMessage;
		else
			return toString();
	}

}
