package com.sea_monster.core.exception;

import java.util.HashMap;

public class InternalException extends BaseException {

	public static final int NETWORK_DISABLED = 3001;
	public static final int DISCARD_TASK = 2001;
	public static final int MODEL_INCOMPLETE = 2002;
	public static final int ENTITY_BUILD_EXP = 2003;
	public static final int IMAGE_GET_FAIL = 2004;
	public static final int UNLOGIN_EXP = 2005;

	public static final int VALID_EXCEPTION_CODE = 2100;

	public static final int DEF_NETWORK_CODE = 1024;
	public static final int DEF_LOGIC_CODE = 2048;
	public static final int DEF_OTHER_CODE = 3072;
	public static final int DEF_SERVICE_CODE = 4096;

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

		codeMapping.put(NETWORK_DISABLED, "当前网络不可用");
		codeMapping.put(DISCARD_TASK, "请求被舍弃");
		codeMapping.put(MODEL_INCOMPLETE, "保存对象信息不全");
		codeMapping.put(ENTITY_BUILD_EXP, "消息体创建失败");
		codeMapping.put(IMAGE_GET_FAIL, "图片获取失败");
		codeMapping.put(UNLOGIN_EXP, "用户尚未登陆");

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
