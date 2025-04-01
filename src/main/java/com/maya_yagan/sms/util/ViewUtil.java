package com.maya_yagan.sms.util;

import atlantafx.base.controls.ModalPane;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.StackPane;

/**
 *
 * @author maya2
 */
public class ViewUtil {
    public static <T> void displayView(String path, Consumer<T> controllerConsumer, ModalPane modalPane) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewUtil.class.getResource(path));
            Parent root = loader.load();
            T controller = loader.getController();
            controllerConsumer.accept(controller);
            modalPane.setContent(root);
            modalPane.show(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Displays a custom dialog with a given title, header message, and custom content.
     * The dialog will include OK and Cancel buttons.
     *
     * @param title   The title of the dialog window.
     * @param header  The header text or message displayed in the dialog.
     * @param content A JavaFX Node containing custom content (e.g., TableView, TextField).
     * @return An Optional containing the ButtonType that was pressed, or empty if the dialog was closed.
     */
    public static Optional<ButtonType> showCustomDialog(String title, String header, Node content){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(content);
        return dialog.showAndWait();
    }
    
    public static ModalPane initializeModalPane(StackPane stackPane) {
        ModalPane modalPane = new ModalPane();
        modalPane.setId("modalPane");
        stackPane.getChildren().add(modalPane);
        return modalPane;
    }
}
