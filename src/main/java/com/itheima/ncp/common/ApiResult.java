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
    private String message;
    private T data;

    public static <T> ApiResult<T> ok(T data) {
        ApiResult<T> r = new ApiResult<T>();
        r.setCode(200);
        r.setMessage("ok");
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
        ApiResult<T> r = new ApiResult<T>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(null);
        return r;
    }
}
