package com.lily.parcelhubcore.parcel.api.response;

import lombok.Getter;

@Getter
public class BaseResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
    private final ErrorInfo error;

    private BaseResponse(boolean success, String code, String message, T data, ErrorInfo error) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.error = error;
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "SUCCESS", "OK", data, null);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, "SUCCESS", message, data, null);
    }

    public static <T> BaseResponse<T> fail(String code, String message) {
        return new BaseResponse<>(
                false,
                code,
                message,
                null,
                new ErrorInfo(code, message, null)
        );
    }

    @Getter
    public static class ErrorInfo {
        private final String code;
        private final String message;

        public ErrorInfo(String code, String message, String detail) {
            this.code = code;
            this.message = message;
        }
    }
}
