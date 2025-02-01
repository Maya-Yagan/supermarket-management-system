package com.maya2002yagan.supermarket_management.util;

import atlantafx.base.controls.ModalPane;
import java.io.IOException;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 *
 * @author maya2
 */
public class FormHelper {
    public static <T> void openForm(String path, Consumer<T> controllerConsumer, ModalPane modalPane) {
        try {
            FXMLLoader loader = new FXMLLoader(FormHelper.class.getResource(path));
            Parent root = loader.load();
            T controller = loader.getController();
            controllerConsumer.accept(controller);
            modalPane.setContent(root);
            modalPane.show(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
