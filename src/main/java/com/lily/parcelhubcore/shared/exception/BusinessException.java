package com.lily.parcelhubcore.shared.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final String message;
    private final transient CommonErrorCode commonErrorCode;

    public BusinessException(CommonErrorCode commonErrorCode) {
        super(commonErrorCode.getMessage());
        this.code = commonErrorCode.getCode();
        this.message = commonErrorCode.getMessage();
        this.commonErrorCode = commonErrorCode;
    }

    public BusinessException(CommonErrorCode commonErrorCode, String message) {
        super(message);
        this.code = commonErrorCode.getCode();
        this.message = message;
        this.commonErrorCode = commonErrorCode;
    }

    public BusinessException(CommonErrorCode commonErrorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = commonErrorCode.getCode();
        this.message = message;
        this.commonErrorCode = commonErrorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
