package com.maya_yagan.sms.order.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.order.model.OrderProduct;
import com.maya_yagan.sms.order.service.OrderService;
import java.util.Collection;
import java.util.List;

import com.maya_yagan.sms.util.ContextMenuUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 * FXML Controller class
 *
 * @author Maya Yagan
 */
public class OrderProductsController extends AbstractTableController<OrderProduct> {

    @FXML private TableColumn<OrderProduct, String> productColumn, unitColumn;
    @FXML private TableColumn<OrderProduct, Double> priceColumn;
    @FXML private TableColumn<OrderProduct, Integer> amountColumn;
    @FXML private Label totalPriceLabel, totalProductsLabel, orderLabel;
    @FXML private Button closeButton;

    private final OrderService orderService = new OrderService();
    private Order order;
    private ModalPane modalPane;

    @Override
    protected void configureColumns() {
        productColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getName())
        );

        priceColumn.setCellValueFactory(cellData -> {
            float price = (float) orderService.getSupplierPrice(order, cellData.getValue());
            return new SimpleDoubleProperty(price).asObject();
        });

        amountColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getAmount()).asObject()
        );

        unitColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getUnit().getFullName())
        );

        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("%.2f", price));
            }
        });
    }

    @Override
    protected Collection<OrderProduct> fetchData() {
        if (order == null) {
            totalPriceLabel.setText("0.00");
            totalProductsLabel.setText("0");
            return List.of();
        }
        updateTotals();
        return order.getOrderProducts();
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<OrderProduct>> menuItemsFor(OrderProduct item) {
        return List.of();
    }

    @Override
    protected void postInit(){
        closeButton.setOnAction(event -> closeModal());
    }

    private void updateTotals(){
        if (order == null) return;
        float totalPrice = orderService.calculateTotalPrice(order);
        int totalProducts = orderService.calculateTotalProducts(order);

        totalPriceLabel.setText(String.format("%.2f", totalPrice));
        totalProductsLabel.setText(String.valueOf(totalProducts));
    }
    
    private void closeModal(){
        if(modalPane != null) modalPane.hide();
    }

    public void setOrder(Order order) {
        this.order = order;
        orderLabel.setText(order.getName());
        refresh();
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }
}
