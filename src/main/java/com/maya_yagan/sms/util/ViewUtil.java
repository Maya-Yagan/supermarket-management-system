package com.maya_yagan.sms.util;

import atlantafx.base.controls.ModalPane;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;

import com.maya_yagan.sms.common.ValidationService;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author Maya Yagan
 */
public class ViewUtil {
    private static final ValidationService validationService = new ValidationService();
    public static <T> void displayModalPaneView(String path, Consumer<T> controllerConsumer, ModalPane modalPane) {
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

    public static <T> void switchView(String path, Consumer<T> controllerConsumer, StackPane targetPane, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewUtil.class.getResource(path));
            Parent root = loader.load();
            T controller = loader.getController();
            controllerConsumer.accept(controller);

            Stage stage = (Stage) targetPane.getScene().getWindow();
            if (title != null) {
                stage.setTitle(title);
            }

            targetPane.getChildren().clear();
            targetPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<ButtonType> showCustomDialog(String title, String header, Node content){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(content);
        return dialog.showAndWait();
    }

    public static Optional<ButtonType> showOrderSummaryDialog(
            String title,
            String headerText,
            List<SupplierProduct> items,
            Map<SupplierProduct, Integer> amounts) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Product"), 0, 0);
        grid.add(new Label("Amount"), 1, 0);
        grid.add(new Label("Price"), 2, 0);
        grid.add(new Label("Total"), 3, 0);

        float grandTotal = 0f;
        int grandQuantity = 0;
        int row = 1;

        for (SupplierProduct sp : items) {
            int qty = amounts.getOrDefault(sp, 1);
            float price = sp.getPrice();
            float lineTotal = price * qty;

            grandQuantity += qty;
            grandTotal   += lineTotal;

            grid.add(new Label(sp.getProduct().getName()),       0, row);
            grid.add(new Label(String.valueOf(qty)),             1, row);
            grid.add(new Label(String.format("%.2f", price)),    2, row);
            grid.add(new Label(String.format("%.2f", lineTotal)),3, row);
            row++;
        }

        Label summary = new Label(
                "Total Quantity: " + grandQuantity +
                        "    Grand Total: " + String.format("%.2f", grandTotal));
        summary.setStyle("-fx-font-weight: bold;");

        grid.add(summary, 0, row, 4, 1);

        return ViewUtil.showCustomDialog(title, headerText, grid);
    }

    public static void showIntegerInputDialog(
            String title,
            String header,
            String content,
            int defaultValue,
            String fieldName,
            IntPredicate validator,
            Consumer<Integer> onSuccess) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(defaultValue));
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            int value = validationService.parseAndValidateInt(result.get(), fieldName);
            if (!validator.test(value)) {
                throw new CustomException(
                        String.format("%s must satisfy business rules", fieldName),
                        "INVALID_NUMBER"
                );
            }
            onSuccess.accept(value);
        } catch (CustomException ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    public static void showIntegerInputDialog(
            String title,
            String header,
            String content,
            int defaultValue,
            String fieldName,
            Consumer<Integer> onSuccess) {
        showIntegerInputDialog(
                title, header, content, defaultValue, fieldName,
                val -> true, onSuccess);
    }

    public static void showFloatInputDialog(
            String title,
            String header,
            String content,
            float defaultValue,
            String fieldName,
            DoublePredicate validator,
            Consumer<Double> onSuccess) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(defaultValue));
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            double value = validationService.parseAndValidateFloat(result.get(), fieldName);
            if (!validator.test(value)) {
                throw new CustomException(
                        String.format("%s must satisfy business rules", fieldName),
                        "INVALID_NUMBER"
                );
            }
            onSuccess.accept(value);
        } catch (CustomException ex) {
            ExceptionHandler.handleException(ex);
        }
    }

    public static void showFloatInputDialog(
            String title,
            String header,
            String content,
            float defaultValue,
            String fieldName,
            Consumer<Double> onSuccess) {
        showFloatInputDialog(
                title, header, content, defaultValue, fieldName,
                val -> true, onSuccess);
    }

    public static void showStringInputDialog(
            String title,
            String header,
            String content,
            String defaultValue,
            String fieldName,
            Consumer<String> onSuccess) {

        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            String value = result.get().trim();
            if (value.isEmpty()) {
                throw new CustomException(fieldName + " cannot be empty", "EMPTY_STRING");
            }
            onSuccess.accept(value);
        } catch (CustomException ex) {
            ExceptionHandler.handleException(ex);
        }
    }


    public static ModalPane initializeModalPane(StackPane stackPane) {
        ModalPane modalPane = new ModalPane();
        modalPane.setId("modalPane");
        stackPane.getChildren().add(modalPane);
        return modalPane;
    }

    public static void setupDynamicLayoutAdjustment(MenuButton menuButton, List<Node> targetNodes, List<Double> offsets){
        if(targetNodes.size() != offsets.size())
            throw new IllegalArgumentException("The number of target nodes must match the number of offsets.");

        menuButton.widthProperty().addListener((obs, oldVal, newVal) -> {
            for (int i = 0; i < targetNodes.size(); i++) {
                Node target = targetNodes.get(i);
                double offset = offsets.get(i);
                target.setLayoutX(menuButton.getLayoutX() + newVal.doubleValue() + offset);
            }
        });
    }

    public static <T> boolean showDialog(
            String fxmlPath,
            String title,
            Consumer<T> initController,
            Consumer<T> onSave) throws IOException {

        FXMLLoader loader = new FXMLLoader(ViewUtil.class.getResource(fxmlPath));
        Parent root = loader.load();
        T controller = loader.getController();
        initController.accept(controller);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(root);

        ButtonType ok = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(ok, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ok) {
            onSave.accept(controller);
            return true;
        }
        return false;
    }

    public static void changeScene(String path, String title,StackPane targetPane){
        try {
            FXMLLoader loader = new FXMLLoader(ViewUtil.class.getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) targetPane.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}