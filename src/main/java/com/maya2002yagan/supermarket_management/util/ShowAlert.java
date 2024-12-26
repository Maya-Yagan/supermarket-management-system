package com.maya2002yagan.supermarket_management.util;

import javafx.scene.control.Alert;

/**
 *
 * This class is a utility class designed to simplify the process of displaying alert dialogs.
 * It provides a static method to show an alert of a specific type with a customizable title and message.
 * 
 * @author Maya Yagan
 */
public class ShowAlert {
    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
