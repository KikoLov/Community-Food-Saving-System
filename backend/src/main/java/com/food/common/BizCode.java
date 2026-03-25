package com.food.common;

/**
 * 统一业务错误码（不替代HTTP状态码）
 */
public enum BizCode {
    SUCCESS("SUCCESS", "操作成功"),
    PARAM_INVALID("PARAM_INVALID", "参数错误"),
    UNAUTHORIZED("UNAUTHORIZED", "未授权"),
    FORBIDDEN("FORBIDDEN", "无权限"),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "请求过于频繁"),
    BUSINESS_ERROR("BUSINESS_ERROR", "业务处理失败"),
    SYSTEM_ERROR("SYSTEM_ERROR", "系统繁忙，请稍后重试");

    private final String code;
    private final String defaultMsg;

    BizCode(String code, String defaultMsg) {
        this.code = code;
        this.defaultMsg = defaultMsg;
    }

    public String code() {
        return code;
    }

    public String defaultMsg() {
        return defaultMsg;
    }
}
