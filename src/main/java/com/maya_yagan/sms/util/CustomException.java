package com.maya_yagan.sms.util;

/**
 *
 * @author Maya Yagan
 */
public class CustomException extends RuntimeException{
     private final String errorCode;

    public CustomException(String message) {
        super(message);
        this.errorCode = null;
    }

    public CustomException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
