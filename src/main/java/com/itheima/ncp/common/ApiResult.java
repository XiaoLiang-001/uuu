package com.itheima.ncp.common;

import lombok.Data;

/**
 * 与前端约定的统一 JSON 消息体，便于 axios 与 Jackson 反序列化。
 * <p>约定：{@code code == 200} 为业务成功；非 200 时看 {@code message}，{@code data} 可为空。</p>
 */
@Data
public class ApiResult<T> {

    /** 业务/协议码：200 成功，其它见各接口说明 */
    private int code;
    /** 响应消息：成功通常为 ok，失败时为可读错误信息。 */
    private String message;
    /** 响应载荷：失败时通常为空。 */
    private T data;

    public static <T> ApiResult<T> ok(T data) {
        // 构建成功响应对象。
        ApiResult<T> r = new ApiResult<T>();
        // 约定成功码为 200。
        r.setCode(200);
        // 默认成功消息。
        r.setMessage("ok");
        // 设置业务数据。
        r.setData(data);
        return r;
    }

    /**
     * 无业务载荷时（如仅表示操作成功）。
     */
    public static <T> ApiResult<T> ok() {
        return ok(null);
    }

    public static <T> ApiResult<T> fail(int code, String message) {
        // 构建失败响应对象。
        ApiResult<T> r = new ApiResult<T>();
        // 写入业务失败码。
        r.setCode(code);
        // 写入可读失败消息。
        r.setMessage(message);
        // 失败时 data 统一置空。
        r.setData(null);
        return r;
    }
}
