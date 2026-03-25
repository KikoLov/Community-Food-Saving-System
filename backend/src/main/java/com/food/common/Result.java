package com.food.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    /**
     * 业务错误码（前端可用于细粒度处理）
     */
    private String bizCode;

    /**
     * 成功
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        result.setBizCode(BizCode.SUCCESS.code());
        return result;
    }

    /**
     * 失败
     */
    public static <T> Result<T> error(String msg) {
        return error(500, BizCode.BUSINESS_ERROR, msg);
    }

    /**
     * 失败
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return error(code, BizCode.BUSINESS_ERROR, msg);
    }

    /**
     * 失败（带统一业务码）
     */
    public static <T> Result<T> error(Integer code, BizCode bizCode, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setBizCode(bizCode.code());
        return result;
    }

    /**
     * 参数错误
     */
    public static <T> Result<T> paramError(String msg) {
        return error(400, BizCode.PARAM_INVALID, msg);
    }

    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String msg) {
        return error(401, BizCode.UNAUTHORIZED, msg);
    }

    /**
     * 禁止访问
     */
    public static <T> Result<T> forbidden(String msg) {
        return error(403, BizCode.FORBIDDEN, msg);
    }

    /**
     * 资源不存在
     */
    public static <T> Result<T> notFound(String msg) {
        return error(404, BizCode.BUSINESS_ERROR, msg);
    }
}
