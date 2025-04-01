package com.maya_yagan.sms.util;

import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 *
 * This class is a utility class designed to simplify the process of displaying alert dialogs.
 * It provides a static method to show an alert of a specific type with a customizable title and message.
 * 
 * @author Maya Yagan
 */
public class AlertUtil {
    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public  static <T> void showDeleteConfirmation(T entity, String title, String headerText, String contentText, Consumer<T> onConfirmAction){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK)
            onConfirmAction.accept(entity);
    }
}
