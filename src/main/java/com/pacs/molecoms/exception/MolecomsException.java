package com.pacs.molecoms.exception;

public class MolecomsException extends RuntimeException {
    private final ErrorCode errorCode;

    public MolecomsException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }
    public MolecomsException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    public MolecomsException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    public ErrorCode getErrorCode() { return errorCode; }
}
