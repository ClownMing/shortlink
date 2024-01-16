package com.ming.shortlink.admin.common.convention.exception;

import com.ming.shortlink.admin.common.convention.errorcode.IErrorCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author clownMing
 * 抽象项目中有三类异常体系：客户端异常、服务端异常以及远程服务调用异常
 * @see ClientException
 * @see ServiceException
 * @see RemoteException
 */
@Getter
public class AbstractException extends RuntimeException{

    public final String errorCode;

    public final String errorMessage;

    public AbstractException(IErrorCode errorCode, Throwable throwable, String errorMessage) {
        super(errorMessage, throwable);
        this.errorCode = errorCode.code();
        this.errorMessage = Optional.ofNullable(StringUtils.hasLength(errorMessage) ? errorMessage : null).orElse(errorCode.message());
    }
}
