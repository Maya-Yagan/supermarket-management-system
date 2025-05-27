package com.maya_yagan.sms.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

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

    public static void showConfirmation(String title, String headerText, String contentText, Runnable onConfirmAction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onConfirmAction.run();
        }
    }

    public static void showNotification(StackPane root,
                                        String message,
                                        Node graphic,
                                        Iterable<String> styleClasses,
                                        Duration stayTime) {

        var notification = new Notification(message,
                graphic != null ? graphic : new FontIcon()); // fallback icon

        // ---------- styling ----------
        if (styleClasses != null) {
            styleClasses.forEach(notification.getStyleClass()::add);
        }
        notification.setPrefHeight(Region.USE_PREF_SIZE);
        notification.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(notification, Pos.TOP_RIGHT);
        StackPane.setMargin(notification, new Insets(10, 10, 0, 0));

        // ---------- add to scene once ----------
        if (!root.getChildren().contains(notification)) {
            root.getChildren().add(notification);
        }

        // ---------- slide-in ----------
        var slideIn = Animations.slideInDown(notification, Duration.millis(250));
        slideIn.play();

        // ---------- auto-dismiss ----------
        slideIn.setOnFinished(ev -> {
            var wait = new PauseTransition(stayTime);
            wait.setOnFinished(e -> slideOutAndRemove(root, notification));
            wait.play();
        });

        // ---------- manual close ----------
        notification.setOnClose(e -> slideOutAndRemove(root, notification));
    }

    /* Convenience overload with sensible defaults (3 s stay, success style). */
    public static void showSuccess(StackPane root, String message) {
        showNotification(root,
                message,
                new FontIcon(Feather.CHECK_CIRCLE),
                List.of(Styles.SUCCESS, Styles.ELEVATED_1),
                Duration.seconds(3));
    }

    // helper
    private static void slideOutAndRemove(StackPane root, Notification n) {
        var slideOut = Animations.slideOutUp(n, Duration.millis(250));
        slideOut.setOnFinished(f -> root.getChildren().remove(n));
        slideOut.playFromStart();
    }

}
