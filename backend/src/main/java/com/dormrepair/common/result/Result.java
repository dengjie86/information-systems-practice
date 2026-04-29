package com.dormrepair.common.result;
public record Result<T>(Integer code, String msg, T data) {

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> fail(ResultCode code, String msg) {
        return new Result<>(code.getCode(), msg, null);
    }
}
