package com.ming.shortlink.admin.common.convention.exception;

import com.ming.shortlink.admin.common.convention.errorcode.BaseErrorCode;
import com.ming.shortlink.admin.common.convention.errorcode.IErrorCode;

/**
 * @author clownMing
 */
public class RemoteException extends AbstractException{

    public RemoteException(String errorMessage) {
        this(BaseErrorCode.REMOTE_ERROR, null, errorMessage);
    }

    public RemoteException(String errorMessage, IErrorCode errorCode) {
        this(errorCode, null, errorMessage);
    }

    public RemoteException(IErrorCode errorCode, Throwable throwable, String errorMessage) {
        super(errorCode, throwable, errorMessage);
    }

    @Override
    public String toString() {
        return "RemoteException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
