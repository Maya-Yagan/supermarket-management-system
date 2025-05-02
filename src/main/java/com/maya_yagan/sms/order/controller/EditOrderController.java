package com.maya_yagan.sms.order.controller;

import com.maya_yagan.sms.common.AbstractTableController;
import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.order.model.OrderProduct;
import com.maya_yagan.sms.order.service.OrderService;
import com.maya_yagan.sms.util.*;

import java.io.IOException;
import java.util.*;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

/**
 * Controller class for editing an existing order.
 *
 * @author Maya Yagan
 */
public class EditOrderController extends AbstractTableController<OrderProduct> {

    @FXML private TextField orderNameField;
    @FXML private DatePicker orderDateField;
    @FXML private TableColumn<OrderProduct, String> productColumn, unitColumn;
    @FXML private TableColumn<OrderProduct, Float> priceColumn;
    @FXML private TableColumn<OrderProduct, Integer> amountColumn;
    @FXML private Button closeButton, addProductButton;
    @FXML private Label totalPriceLabel, totalProductsLabel;

    private Order order;
    private Runnable onCloseAction;
    private ModalPane modalPane;

    private final OrderService orderService = new OrderService();

    @Override
    protected void configureColumns() {
        productColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getName()));

        priceColumn.setCellValueFactory(cellData -> {
            float price = (float) orderService.getSupplierPrice(cellData.getValue().getOrder(), cellData.getValue());
            return new SimpleFloatProperty(price).asObject();
        });

        amountColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getAmount()).asObject());

        unitColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getUnit().getFullName()));
    }

    @Override
    protected Collection<OrderProduct> fetchData() {
        if (order == null) {
            return Collections.emptyList();
        }
        order = orderService.getOrderById(order.getId());
        return Optional.ofNullable(order.getOrderProducts())
                .orElse(Collections.emptySet());
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<OrderProduct>> menuItemsFor(OrderProduct op) {
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Edit Amount", (item, row) -> handleEditAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Delete Product", (item, row) -> handleDeleteAction(item))
        );
    }

    @Override
    protected void postInit(){
        setupEventHandlers();
        DateUtil.applyDateFormat(orderDateField);
    }

    private void setupEventHandlers(){
    closeButton.setOnAction(event -> close());
    addProductButton.setOnAction(event -> {
        try {
            boolean saved = ViewUtil.showDialog(
                    "/view/order/AddProductToOrder.fxml",
                    "Add Product to Order",
                    (AddProductToOrderController controller) -> {
                        controller.setOrder(order);
                        controller.setSupplier(order.getSupplier());
                        controller.setOnCloseAction(this::refreshUI);
                    },
                    AddProductToOrderController::saveProducts
            );
            if(saved) refreshUI();
            } catch (IOException e) {
                ExceptionHandler.handleException(
                        new CustomException("Error loading view: " + e.getMessage(), "IO_ERROR"));
            }
        });
    }

    private void populateFields(){
        if(order != null){
            order = orderService.getOrderById(order.getId());
            orderNameField.setText(order.getName());
            orderDateField.setValue(order.getOrderDate());
        }
    }

    private void updateTotals(){
        if (order == null) return;

        float totalPrice = orderService.calculateTotalPrice(order);
        int totalProducts = orderService.calculateTotalProducts(order);

        totalPriceLabel.setText(String.format("%.2f", totalPrice));
        totalProductsLabel.setText(String.valueOf(totalProducts));
    }

    private void handleEditAction(OrderProduct orderProduct){
        ViewUtil.showIntegerInputDialog(
                "Edit Amount",
                "Enter new amount for " + orderProduct.getProduct().getName(),
                "Price (" + orderProduct.getProduct().getUnit().getShortName() + ")",
                orderProduct.getAmount(),
                "Amount",
                newAmt -> {
                    try{
                        orderService.updateOrder(order, orderProduct, newAmt);
                        refreshUI();
                        AlertUtil.showAlert(
                                Alert.AlertType.INFORMATION,
                                "Success",
                                "The product amount was updated successfully."
                        );
                    } catch(CustomException e){
                        ExceptionHandler.handleException(e);
                        refresh();
                    }
                }
        );
    }

    private void handleDeleteAction(OrderProduct orderProduct){
        if (order.getOrderProducts().size() == 1) {
            AlertUtil.showDeleteConfirmation(
                    order,
                    "Delete Last Product",
                    "Deleting this product will also delete the order.",
                    "Are you sure you want to proceed?",
                    o -> {
                        orderService.deleteOrder(o.getId());
                        close();
                    });
        } else {
            AlertUtil.showDeleteConfirmation(
                    orderProduct,
                    "Delete Product From Order",
                    "Are you sure you want to delete this product from the order?",
                    "This action cannot be undone",
                    op -> {
                        orderService.deleteProductFromOrder(op.getOrder(), op.getProduct());
                        order.getOrderProducts().remove(op);
                        refresh();
                        updateTotals();
                    });
        }
    }

    private void close(){
        try{
            if(order != null){
                order.setName(orderNameField.getText());
                order.setOrderDate(orderDateField.getValue());
                orderService.updateOrder(order);
            }

            if(modalPane != null) modalPane.hide();
            if(onCloseAction != null) onCloseAction.run();
        } catch(CustomException e){
            ExceptionHandler.handleException(e);
        }

    }

    public void setOrder(Order order) {
        this.order = order;
        refresh();
        populateFields();
        updateTotals();
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    public void refreshUI(){
        order = orderService.getOrderById(order.getId());
        refresh();
        updateTotals();
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
}
