package com.maya_yagan.sms.order.controller;

import com.maya_yagan.sms.order.controller.AddProductToOrderController;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.order.dao.OrderDAO;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.order.model.OrderProduct;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.util.AlertUtil;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Controller class for editing an existing order.
 * Handles UI interactions and updates order details in the database.
 * 
 * @author Maya Yagan
 */
public class EditOrderController implements Initializable {

    @FXML
    private TextField orderNameField;
    @FXML
    private DatePicker orderDateField;
    @FXML
    private TableView orderDetailsTable;
    @FXML
    private TableColumn<OrderProduct, String> productColumn, unitColumn;
    @FXML
    private TableColumn<OrderProduct, Float> priceColumn;
    @FXML
    private TableColumn<OrderProduct, Integer> amountColumn;
    @FXML
    private Button closeButton, addProductButton;
    @FXML
    private Label totalPriceLabel, totalProductsLabel;
    
    private Order order;
    private Runnable onCloseAction;
    private OrderProduct orderProduct;
    private ModalPane modalPane;
    
    private final OrderDAO orderDAO = new OrderDAO();
    private final ObservableList<OrderProduct> ordersObservableList = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        loadData();
        setupRowInteractions();
        setupEventHandlers();
    }
    
    /**
     * Sets up event handlers for buttons in the UI.
     */
    private void setupEventHandlers(){
    closeButton.setOnAction(event -> closeModal());
    addProductButton.setOnAction(event -> {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/order/AddProductToOrder.fxml"));
            Parent root = loader.load();

            AddProductToOrderController controller = loader.getController();
            controller.setOrder(order);
            controller.setSupplier(order.getSupplier());
            controller.setOnCloseAction(() -> {
                order = orderDAO.getOrderById(order.getId());
                populateTable();
                updateTotals();
            });

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add Products to Order");
            dialog.getDialogPane().setContent(root);

            // Create a custom Save button and add it along with a Cancel button
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if(result.isPresent() && result.get() == saveButtonType)
                controller.saveProducts();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    });
}

    /**
     * Configures table columns to display product name, price, and quantity.
     */
    private void configureTableColumns(){
        orderDetailsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        orderDetailsTable.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        productColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProduct().getName())
        );
        
        priceColumn.setCellValueFactory(cellData -> {
            float price = getSupplierPrice(cellData.getValue());
            return new SimpleFloatProperty(price).asObject();
        });
        
        amountColumn.setCellValueFactory(cellData -> 
                new SimpleIntegerProperty(cellData.getValue().getAmount()).asObject()
        );
        
        unitColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getUnit().getFullName())
        );
    }
    
    /**
     * Loads order data and populates fields and table.
     */
    private void loadData(){
        if(order != null){
            order = orderDAO.getOrderById(order.getId());
            orderNameField.setText(order.getName());
            orderDateField.setValue(order.getOrderDate());
        }
        populateTable();
    }
    
    /**
     * Populates the table with order products.
     */
    private void populateTable(){
        if(order != null){
            ordersObservableList.clear();
            ordersObservableList.addAll(order.getOrderProducts());
            orderDetailsTable.setItems(ordersObservableList);
            updateTotals();
        }
    }
    
    /**
     * Retrieves the supplier price for a given product.
     * 
     * @param op The OrderProduct to get the price for.
     * @return The price of the product.
     */
    private float getSupplierPrice(OrderProduct op){
        if(order != null && order.getSupplier() != null){
            for(SupplierProduct sp : order.getSupplier().getSupplierProducts()){
                if(sp.getProduct().equals(op.getProduct()))
                    return sp.getPrice();
            }
        }
       return 0f; 
    }
    
    /**
     * Updates total price and total product count labels.
     */
    private void updateTotals(){
        float totalPrice = 0;
        int totalProducts = 0;

        for(OrderProduct op : order.getOrderProducts()){
            totalPrice += getSupplierPrice(op) * op.getAmount();
            totalProducts += op.getAmount();
        }
        totalPriceLabel.setText(String.format("%.2f", totalPrice));
        totalProductsLabel.setText(String.valueOf(totalProducts));
    }
    
    private void setupRowInteractions(){
        orderDetailsTable.setRowFactory(tv -> {
            TableRow<OrderProduct> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldVal, newVal) -> {
                row.setContextMenu(createContextMenu(row));
            });
            row.setOnMouseClicked(event -> handleRowClick(event, row));
            return row;
        });
    }
    
    private ContextMenu createContextMenu(TableRow<OrderProduct> row){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        MenuItem editMenuItem = new MenuItem("Edit Amount");
        
        contextMenu.getItems().add(editMenuItem);
        editMenuItem.setOnAction(event -> {
            orderProduct = row.getItem();
            if(orderProduct != null){
                TextInputDialog dialog = new TextInputDialog(String.valueOf(orderProduct.getAmount()));
                dialog.setTitle("Enter the Amount");
                dialog.setHeaderText("Enter the amount for " + orderProduct.getProduct().getName());
                dialog.setContentText("Amount(" + 
                                        orderProduct.getProduct()
                                        .getUnit().getShortName() + "):");
                
                Optional<String> result = dialog.showAndWait();
                if(result.isPresent()){
                    try{
                        int amount = Integer.parseInt(result.get());
                        if(amount <= 0)
                            AlertUtil.showAlert(Alert.AlertType.ERROR,
                                    "Invalid Input", "Amount must be greater than zero");
                        else{
                            orderProduct.setAmount(amount);
                            orderDAO.updateOrder(order);
                            orderDetailsTable.refresh();
                            updateTotals();
                        }
                    } catch(NumberFormatException e){
                        AlertUtil.showAlert(Alert.AlertType.ERROR,
                                "Invalid Input",
                                "Please enter a valid number");
                    }
                }
            }
        });
        
        contextMenu.getItems().add(deleteMenuItem);
        deleteMenuItem.setOnAction(event -> {
            orderProduct = row.getItem();
            if(order.getOrderProducts().size() == 1){
                AlertUtil.showDeleteConfirmation(order, 
                        "Delete Last Product", 
                        "Deleting this product will also delete the order.",
                        "Are you sure you want to proceed?",
                        (Order o) -> {
                            orderDAO.deleteOrder(order.getId());
                            ordersObservableList.clear();
                            closeModal();
                        });
            }
            else{
                AlertUtil.showDeleteConfirmation(orderProduct,
                    "Delete Product From Order",
                "Are you sure you want to delete this product from the order?",
                "This action cannot be undone",
                (OrderProduct op) -> {
                    orderDAO.deleteProductFromOrder(op.getOrder(), op.getProduct());
                    order.getOrderProducts().remove(op);
                    ordersObservableList.remove(op);
                    updateTotals();
                });
            }
        }); 
        return contextMenu;
    }
    
    private void handleRowClick(MouseEvent event, TableRow<OrderProduct> row){
        if(row.isEmpty()) return;
        
        if(event.getButton() == MouseButton.SECONDARY)
            row.getContextMenu().show(row, event.getScreenX(), event.getScreenY());
        else
            row.getContextMenu().hide();
    }
    
    /**
     * Handles the closing of the modal, saving order details if modified.
     */
    private void closeModal(){
        if(order != null){
            order.setName(orderNameField.getText());
            order.setOrderDate(orderDateField.getValue());
            orderDAO.updateOrder(order);
        }
        
        if(modalPane != null) modalPane.hide();
        if(onCloseAction != null) onCloseAction.run();
    }

    public void setOrder(Order order) {
        this.order = order;
        loadData();
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
}
