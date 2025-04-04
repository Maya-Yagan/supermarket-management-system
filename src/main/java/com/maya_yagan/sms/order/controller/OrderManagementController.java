package com.maya_yagan.sms.order.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.order.dao.OrderDAO;
import com.maya_yagan.sms.warehouse.dao.WarehouseDAO;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.order.model.OrderProduct;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.util.ViewUtil;
import com.maya_yagan.sms.util.AlertUtil;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Maya Yagan
 */
public class OrderManagementController implements Initializable {

    @FXML
    private TableView<Order> ordersTableView;
    @FXML
    private TableColumn<Order, String> orderColumn, supplierColumn, orderDateColumn, deliveryDateColumn;
    @FXML
    private TableColumn<Order, Double> priceColumn;
    @FXML
    private Button addOrderButton;
    @FXML
    private StackPane stackPane;
    
    private ModalPane modalPane;
    private final OrderDAO orderDAO = new OrderDAO();
    private final ObservableList<Order> orderObservableList = FXCollections.observableArrayList();
    private Order selectedOrder;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeModalPane();
        configureTableColumns();
        populateTable();
        setupRowInteractions();
        addOrderButton.setOnAction(event -> ViewUtil.displayView(
                "/view/order/AddOrder.fxml",
                (AddOrderController controller) -> {
                }, modalPane));
    } 
    
    private void initializeModalPane() {
        StackPane root = stackPane;
        modalPane = new ModalPane();
        modalPane.setId("modalPane");
        root.getChildren().add(modalPane);
    }
    
    private void configureTableColumns(){
        ordersTableView.setItems(orderObservableList);
        ordersTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ordersTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        orderColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        deliveryDateColumn.setCellValueFactory(cellData -> {
            return cellData.getValue().getDeliveryDate() == null 
                    ? new SimpleStringProperty("Awaiting Delivery") 
                    : new SimpleStringProperty(String.valueOf(cellData.getValue().getDeliveryDate()));
        });
        priceColumn.setCellValueFactory(cellData -> {
            double price = getPrice(cellData.getValue());
            return new SimpleDoubleProperty(price).asObject();
        });
        
        priceColumn.setCellFactory(column -> new TableCell<Order, Double>(){
            @Override
            protected void updateItem(Double price, boolean empty){
                super.updateItem(price, empty);
                if(empty || price == null)
                    setText(null);
                else
                    setText(String.format("%.2f", price));
            }
        });
    }
    
    private float getPrice(Order order){
        float totalPrice = 0.0f;
        Supplier supplier = order.getSupplier();
        if(supplier != null && supplier.getSupplierProducts() != null){
            for(OrderProduct orderProduct : order.getOrderProducts()){
                Product product = orderProduct.getProduct();
                for(SupplierProduct supplierProduct : supplier.getSupplierProducts()){
                    if(supplierProduct.getProduct().equals(product)){
                        totalPrice += supplierProduct.getPrice() * orderProduct.getAmount();
                        break;
                    }
                }
            }
        }
        return totalPrice;
    }
    
    private void populateTable(){
        Set<Order> orders = orderDAO.getOrders();
        orderObservableList.clear();
        orderObservableList.addAll(orders);
    }
    
    private void setupRowInteractions(){
        ordersTableView.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldOrder, currentOrder) -> {
                if(currentOrder == null || currentOrder.getDeliveryDate() != null)
                    row.setContextMenu(createContextMenu(row, true));
                else
                    row.setContextMenu(createContextMenu(row, false));
            });
            row.setOnMouseClicked(event -> handleRowClick(event, row));
            return row;
        });
    }
    
    private ContextMenu createContextMenu(TableRow<Order> row, boolean isDelivered){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem completeOrder = new MenuItem("Mark as Delivered");
        MenuItem deleteOrder = new MenuItem("Delete Order");
        MenuItem orderDetails = new MenuItem("Order Details");
        MenuItem editOrder = new MenuItem("Edit Order");
        
        contextMenu.getItems().add(orderDetails);
        orderDetails.setOnAction(event -> {
            selectedOrder = row.getItem();
            if(selectedOrder != null)
                ViewUtil.displayView("/view/order/OrderProducts.fxml",
                        (OrderProductsController controller) -> {
                            controller.setModalPane(modalPane);
                            controller.setOrder(selectedOrder);
                        }, modalPane);
        });
        
        contextMenu.getItems().add(deleteOrder);
        deleteOrder.setOnAction(event -> {
            selectedOrder = row.getItem();
            AlertUtil.showDeleteConfirmation(selectedOrder,
                    "Delete Order", 
                    "Are you sure you want to delete this order?",
                    "This action cannot be undone", 
                    (Order o) -> {
                        orderDAO.deleteOrder(o.getId());
                        orderObservableList.remove(o);
                    });
        });
        
        if(!isDelivered){
            contextMenu.getItems().add(completeOrder);
            completeOrder.setOnAction(event -> {
            selectedOrder = row.getItem();
            if(selectedOrder != null)
                showWarehouseChoiceDialog(selectedOrder);
            });
            
            contextMenu.getItems().add(editOrder);
            editOrder.setOnAction(event -> {
                selectedOrder = row.getItem();
                if(selectedOrder != null)
                    ViewUtil.displayView("/view/order/EditOrder.fxml",
                            (EditOrderController controller) -> {
                                controller.setModalPane(modalPane);
                                controller.setOrder(selectedOrder);
                                controller.setOnCloseAction(() -> populateTable());
                            }, modalPane);
            });
            
        }
        return contextMenu;
    }
    
    private void handleRowClick(MouseEvent event, TableRow<Order> row){
        if(row.isEmpty()) return;
        
        if(event.getButton() == MouseButton.SECONDARY)
            row.getContextMenu().show(row, event.getScreenX(), event.getScreenY());
        else
            row.getContextMenu().hide();
    }
    
    private  void showWarehouseChoiceDialog(Order order){
        WarehouseDAO warehouseDAO = new WarehouseDAO();
        List<Warehouse> warehouses = warehouseDAO.getWarehouses();
        if(warehouses != null && !warehouses.isEmpty()){
            ChoiceDialog<Warehouse> dialog = new ChoiceDialog<>(warehouses.get(0), warehouses);
            dialog.setTitle("Select Warehouse");
            dialog.setHeaderText("Choose a Warehouse");
            dialog.setContentText("Select a warehouse where the order's products will be saved:");

            Optional<Warehouse> result = dialog.showAndWait();
            result.ifPresent(selectedWarehose -> {
                if(tryToAddOrderToWarehouse(order, selectedWarehose)){
                    order.setDeliveryDate(LocalDate.now());
                    orderDAO.updateOrder(order);
                    ordersTableView.refresh();
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, 
                            "Success", 
                           "Order's products have been added to warehouse:" +
                            selectedWarehose.getName());
                }
                else{
                    AlertUtil.showAlert(Alert.AlertType.WARNING, 
                            "No Enough Space in The Warehouse",
                            "This warehouse doesn't have enough capacity for this order. Please choose another warehouse.");
                    showWarehouseChoiceDialog(order);
                }
            });
        }
    }
    
    private boolean tryToAddOrderToWarehouse(Order order, Warehouse warehouse){
        WarehouseDAO warehouseDAO = new WarehouseDAO();
        Warehouse freshWarehouse = warehouseDAO.getWarehouseById(warehouse.getId());
        
        if(freshWarehouse != null){
            int currentUsage = freshWarehouse.getProductWarehouses()
                    .stream()
                    .mapToInt(ProductWarehouse::getAmount)
                    .sum();
            int orderTotal = order.getOrderProducts()
                    .stream()
                    .mapToInt(OrderProduct::getAmount)
                    .sum();
            
            if(currentUsage + orderTotal <= freshWarehouse.getCapacity()){
                for(OrderProduct orderProduct : order.getOrderProducts()){
                    Product product = orderProduct.getProduct();
                    boolean found = false;
                    for(ProductWarehouse pw : freshWarehouse.getProductWarehouses()){
                        if(pw.getProduct().equals(product)){
                            pw.setAmount(pw.getAmount() + orderProduct.getAmount());
                            found = true;
                            break;
                        }
                    }
                    if(!found)
                        freshWarehouse.getProductWarehouses().add(new ProductWarehouse(freshWarehouse, product, orderProduct.getAmount()));
                }
                warehouseDAO.updateWarehouse(freshWarehouse);
                return true;
            }
        }
        return false;
    }
}
