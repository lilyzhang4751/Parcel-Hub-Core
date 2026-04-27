package com.lily.parcelhubcore.shared.exception;

import java.util.stream.Collectors;

import com.lily.parcelhubcore.shared.response.BaseResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 业务异常：自己主动抛出的异常
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<Void> handleBusinessException(BusinessException ex) {
        log.warn("BusinessException: code={}, message={}", ex.getCode(), ex.getMessage());
        return BaseResponse.fail(ex.getCode(), ex.getMessage());
    }

    /**
     * 2. @RequestBody + @Valid 校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        var message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("MethodArgumentNotValidException: {}", message);
        return BaseResponse.fail(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * 3. query/path/form 参数绑定或校验失败
     */
    @ExceptionHandler({BindException.class, ConstraintViolationException.class})
    public BaseResponse<Void> handleValidationException(Exception ex) {
        String message;

        if (ex instanceof BindException bindException) {
            message = bindException.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining("; "));
        } else if (ex instanceof ConstraintViolationException constraintViolationException) {
            message = constraintViolationException.getConstraintViolations()
                    .stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
        } else {
            message = ErrorCode.PARAM_INVALID.getMessage();
        }

        log.warn("ValidationException: {}", message);
        return BaseResponse.fail(ErrorCode.PARAM_INVALID.getCode(), message);
    }

    /**
     * 4. @PreAuthorize 权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public BaseResponse<Void> handleAccessDeniedException(Exception ex) {
        log.error("access denied", ex);
        return BaseResponse.fail(
                ErrorCode.ACCESS_DENIED.getCode(),
                ErrorCode.ACCESS_DENIED.getMessage()
        );
    }

    /**
     * 5. 用户不存在等鉴权异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public BaseResponse<Void> handleAuthenticationException(Exception ex) {
        log.error("access denied", ex);
        return BaseResponse.fail(
                ErrorCode.AUTHENTICATION_FAILED.getCode(),
                ErrorCode.AUTHENTICATION_FAILED.getMessage()
        );
    }

    /**
     * 6. 兜底异常：所有未预期异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return BaseResponse.fail(
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage()
        );
    }
}
