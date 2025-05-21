package com.maya_yagan.sms.util;

import javafx.scene.control.Alert;

/**
 *
 * @author Maya Yagan
 */
public class ExceptionHandler {
    public static void handleException(CustomException exception){
        String alertMessage, alertTitle;
        String errorCode = exception.getErrorCode() != null ?
                exception.getErrorCode() : "UNKNOWN_ERROR";

        switch(errorCode){
            case "EMPTY_FIELDS":
                alertTitle = "Empty Fields";
                alertMessage = exception.getMessage();
                break;
            case "INVALID_EMAIL":
                alertTitle = "Invalid Email Format";
                alertMessage = exception.getMessage();
                break;
            case "INVALID_NUMBER":
                alertTitle = "Invalid Number";
                alertMessage = exception.getMessage();
                break;
            case "INVALID_DATE":
                alertTitle = "Invalid Date Format";
                alertMessage = exception.getMessage();
                break;
            case "INVALID_CAPACITY":
                alertTitle = "Invalid Warehouse Capacity";
                alertMessage = exception.getMessage();
                break;
            case "INSUFFICIENT_CAPACITY":
                alertTitle = "Insufficient Warehouse Capacity";
                alertMessage = exception.getMessage();
                break;
            case "NOT_FOUND":
                alertTitle = "Missing";
                alertMessage = exception.getMessage();
                break;
            case "INVALID_FIELD":
                alertTitle = "Invalid Fields";
                alertMessage = exception.getMessage();
                break;
            case "CHECK_OUT":
                alertTitle = "Check-out Error";
                alertMessage = exception.getMessage();
                break;
            case "INVALID_FORMAT":
                alertTitle = "Invalid Format";
                alertMessage = exception.getMessage();
                break;
            default:
                alertTitle = "Unexpected Error";
                alertMessage = "An unexpected error occurred: " + exception.getMessage();
        }

        AlertUtil.showAlert(Alert.AlertType.ERROR,
                alertTitle, alertMessage);
    }
}