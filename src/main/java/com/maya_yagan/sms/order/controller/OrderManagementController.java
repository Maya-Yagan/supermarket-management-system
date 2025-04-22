package com.maya_yagan.sms.order.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.order.service.OrderService;
import com.maya_yagan.sms.util.*;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.warehouse.model.Warehouse;

import java.util.*;
import java.util.function.Predicate;

import com.maya_yagan.sms.warehouse.service.WarehouseService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Maya Yagan
 */
public class OrderManagementController extends AbstractTableController<Order> {

    @FXML private TableColumn<Order, String> orderColumn, supplierColumn, orderDateColumn, deliveryDateColumn;
    @FXML private TableColumn<Order, Double> priceColumn;
    @FXML private Button addOrderButton;
    @FXML private StackPane stackPane;
    
    private ModalPane modalPane;
    private final OrderService orderService = new OrderService();
    private final WarehouseService warehouseService = new WarehouseService();
    private Order selectedOrder;

    @Override
    protected void configureColumns() {
        orderColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        deliveryDateColumn.setCellValueFactory(cellData -> cellData.getValue().getDeliveryDate() == null
                ? new SimpleStringProperty("Awaiting Delivery")
                : new SimpleStringProperty(String.valueOf(cellData.getValue().getDeliveryDate())));

        priceColumn.setCellValueFactory(cellData -> {
            double price = orderService.getPrice(cellData.getValue());
            return new SimpleDoubleProperty(price).asObject();
        });

        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null)
                    setText(null);
                else
                    setText(String.format("%.2f", price));
            }
        });
    }

    @Override
    protected Collection<Order> fetchData() {
        return orderService.getOrders();
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<Order>> menuItemsFor(Order order) {
        Predicate<Order> notDelivered = o -> o.getDeliveryDate() == null;
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Order Details", (item, row) ->
                        handleDetailsAction(row)),
                new ContextMenuUtil.MenuItemConfig<>("Delete Order", (item, row) ->
                        handleDeleteAction(row)),
                new ContextMenuUtil.MenuItemConfig<>("Edit Order", (item,  row) ->
                        handleEditAction(row), notDelivered),
                new ContextMenuUtil.MenuItemConfig<>("Mark as Delivered", (item, row) ->
                        handleDeliveryAction(row), notDelivered)
        );
    }

    @Override
    protected void postInit(){
        modalPane = ViewUtil.initializeModalPane(stackPane);
        addOrderButton.setOnAction(event -> ViewUtil.displayModalPaneView(
                "/view/order/AddOrder.fxml",
                (AddOrderController controller) -> {
                }, modalPane));
    }

    private void handleDeliveryAction(TableRow<Order> row) {
        selectedOrder = row.getItem();
        showWarehouseChoiceDialog(selectedOrder);
    }

    private void handleEditAction(TableRow<Order> row) {
        selectedOrder = row.getItem();
        if(selectedOrder != null)
            ViewUtil.displayModalPaneView("/view/order/EditOrder.fxml",
                    (EditOrderController controller) -> {
                        controller.setModalPane(modalPane);
                        controller.setOrder(selectedOrder);
                        controller.setOnCloseAction(this::refresh);
                    }, modalPane);
    }

    private void handleDetailsAction(TableRow<Order> row) {
        selectedOrder = row.getItem();
        if(selectedOrder != null)
            ViewUtil.displayModalPaneView("/view/order/OrderProducts.fxml",
                    (OrderProductsController controller) -> {
                        controller.setModalPane(modalPane);
                        controller.setOrder(selectedOrder);
                    }, modalPane);
    }

    private void handleDeleteAction(TableRow<Order> row) {
        selectedOrder = row.getItem();
        AlertUtil.showDeleteConfirmation(selectedOrder,
                "Delete Order",
                "Are you sure you want to delete this order?",
                "This action cannot be undone",
                (Order o) -> {
                    orderService.deleteOrder(o.getId());
                    refresh();
                }
        );
    }
    
    private  void showWarehouseChoiceDialog(Order order){
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        if(warehouses.isEmpty()){
            AlertUtil.showAlert(Alert.AlertType.WARNING,
                    "No Warehouses",
                    "There are no warehouses configured yet");
            return;
        }

        ChoiceDialog<Warehouse> dialog = new ChoiceDialog<>(warehouses.get(0), warehouses);
        dialog.setTitle("Select Warehouse");
        dialog.setHeaderText("Choose a Warehouse");
        dialog.setContentText("Select a warehouse where the order's products will be saved:");
        dialog.showAndWait().ifPresent(selectedWarehouse -> {
            try{
                orderService.deliverOrder(order, selectedWarehouse);
                refresh();
                AlertUtil.showAlert(Alert.AlertType.INFORMATION,
                        "Success",
                        "Order's products have been added to warehouse: " + selectedWarehouse.getName());
            } catch (CustomException e){
                ExceptionHandler.handleException(e);
                if("INSUFFICIENT_CAPACITY".equals(e.getErrorCode()))
                    showWarehouseChoiceDialog(order);
            }
        });
    }
}
