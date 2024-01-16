package com.ming.shortlink.admin.common.convention.exception;

import com.ming.shortlink.admin.common.convention.errorcode.BaseErrorCode;
import com.ming.shortlink.admin.common.convention.errorcode.IErrorCode;

import java.util.Optional;

/**
 * @author clownMing
 */
public class ServiceException extends AbstractException{

    public ServiceException(String errorMessage) {
        this(BaseErrorCode.SERVICE_ERROR, null, errorMessage);
    }

    public ServiceException(IErrorCode errorCode) {
        this(null, errorCode);
    }

    public ServiceException(String errorMessage, IErrorCode errorCode) {
        this(errorCode, null, errorMessage);
    }

    public ServiceException(IErrorCode errorCode, Throwable throwable, String errorMessage) {
        super(errorCode, throwable, Optional.ofNullable(errorMessage).orElse(errorCode.message()));
    }

    @Override
    public String toString() {
        return "ServiceException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
