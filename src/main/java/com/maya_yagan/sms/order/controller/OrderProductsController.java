package com.maya_yagan.sms.order.controller;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.order.model.OrderProduct;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author Maya Yagan
 */
public class OrderProductsController implements Initializable {

    @FXML
    private TableView<OrderProduct> orderDetailsTable;
    @FXML
    private TableColumn<OrderProduct, String> productColumn, unitColumn;
    @FXML
    private TableColumn<OrderProduct, Double> priceColumn;
    @FXML
    private TableColumn<OrderProduct, Integer> amountColumn;
    @FXML
    private Label totalPriceLabel, totalProductsLabel, orderLabel;
    @FXML
    private Button closeButton;
    
    private Order order;
    private ModalPane modalPane;
    
    private final ObservableList<OrderProduct> orderObservableList = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        populateTable();
        closeButton.setOnAction(event -> closeModal());
        orderDetailsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        orderDetailsTable.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
    }  
    
    private void configureTableColumns(){
        productColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProduct().getName())
        );
        
        priceColumn.setCellValueFactory(cellData -> {
            float price = getSupplierPrice(cellData.getValue());
            return new SimpleDoubleProperty(price).asObject();
        });
        
        amountColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getAmount()).asObject()
        );
        
        unitColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getProduct().getUnit().getFullName())
        );
        
        priceColumn.setCellFactory(column -> new TableCell<OrderProduct, Double>(){
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
    
    private void populateTable(){
        if(order != null){
            orderObservableList.clear();
            orderObservableList.addAll(order.getOrderProducts());
            orderDetailsTable.setItems(orderObservableList);
            updateTotals();
        }
    }
    
    private float getSupplierPrice(OrderProduct op){
        if(order != null && order.getSupplier() != null){
            for(SupplierProduct sp : order.getSupplier().getSupplierProducts()){
                if(sp.getProduct().equals(op.getProduct()))
                    return sp.getPrice();
            }
        }
        return 0f;
    }
    
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
    
    private void closeModal(){
        if(modalPane != null) modalPane.hide();
    }

    public void setOrder(Order order) {
        this.order = order;
        orderLabel.setText(order.getName());
        populateTable();
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }
}
