package com.maya_yagan.sms.util;

/**
 *
 * @author Maya Yagan
 */
public class ExceptionUtil extends RuntimeException{
     private final String errorCode;

    public ExceptionUtil(String message) {
        super(message);
        this.errorCode = null;
    }

    public ExceptionUtil(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
