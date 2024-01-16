package com.ming.shortlink.admin.common.convention.exception;

import com.ming.shortlink.admin.common.convention.errorcode.BaseErrorCode;
import com.ming.shortlink.admin.common.convention.errorcode.IErrorCode;

/**
 * @author clownMing
 * 客户端异常
 */
public class ClientException extends AbstractException{

    public ClientException(IErrorCode errorCode) {
        this(errorCode, null, null);
    }
    public ClientException(String errorMessage) {
        this(BaseErrorCode.CLIENT_ERROR, null, errorMessage);
    }

    public ClientException(String errorMessage, IErrorCode errorCode) {
        this(errorCode, null, errorMessage);
    }

    public ClientException(IErrorCode errorCode, Throwable throwable, String errorMessage) {
        super(errorCode, throwable, errorMessage);
    }

    @Override
    public String toString() {
        return "ClientException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
