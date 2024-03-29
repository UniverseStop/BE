package be.busstop.global.exception;

import be.busstop.global.stringCode.ErrorCodeEnum;

public class UploadException extends RuntimeException{
    ErrorCodeEnum errorCodeEnum;

    public UploadException(ErrorCodeEnum errorCodeEnum) {
        this.errorCodeEnum = errorCodeEnum;
    }

    public UploadException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(cause);
        this.errorCodeEnum = errorCodeEnum;
    }

}
