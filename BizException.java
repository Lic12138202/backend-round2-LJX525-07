package com.studio.jwt.exception;

/** 业务异常，携带 HTTP 状态码 */
public class BizException extends RuntimeException {

    private final int status;

    public BizException(int status, String message) {
        super(message);
        this.status = status;
    }

    public static BizException badRequest(String message) {
        return new BizException(400, message);
    }

    public static BizException unauthorized(String message) {
        return new BizException(401, message);
    }

    /** 无权限（登录了但不够格） */
    public static BizException forbidden(String message) {
        return new BizException(403, message);
    }

    public static BizException conflict(String message) {
        return new BizException(409, message);
    }

    public static BizException notFound(String message) {
        return new BizException(404, message);
    }

    public int getStatus() {
        return status;
    }
}
