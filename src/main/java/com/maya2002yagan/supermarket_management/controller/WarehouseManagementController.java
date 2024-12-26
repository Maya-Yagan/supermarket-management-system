package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya2002yagan.supermarket_management.dao.WarehouseDAO;
import com.maya2002yagan.supermarket_management.model.ProductWarehouse;
import com.maya2002yagan.supermarket_management.model.Warehouse;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

/**
 * Controller class for managing warehouses in the application.
 * 
 * This class provides functionality for displaying a list of warehouses,
 * viewing the products stored in a specific warehouse, and adding or editing warehouse details.
 * It interacts with the user interface through JavaFX components and handles database operations
 * via the WarehouseDAO.
 * 
 * @author Maya Yagan
 */
public class WarehouseManagementController implements Initializable {

    @FXML
    private TableView<Warehouse> warehouseTableView;
    @FXML
    private TableColumn<Warehouse, String> nameColumn;
    @FXML 
    private TableColumn<Warehouse, Integer> capacityColumn;
    @FXML
    private TableColumn<Warehouse, Integer> totalProductsColumn;
    @FXML
    private StackPane stackPane;
    private ModalPane modalPane;
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final ObservableList<Warehouse> warehouseObservableList = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller and sets up the warehouse table view and modal pane.
     * 
     * This method configures the table columns, sets the warehouse data into the table,
     * and defines the row click behavior to open warehouse product details in a modal.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure table columns for displaying warehouse data
        configureTableColumns();
        // Bind the observable list to the table view
        warehouseTableView.setItems(warehouseObservableList);
        
        // Define behavior for double-clicking a row
        warehouseTableView.setRowFactory(tv -> {
            TableRow<Warehouse> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    Warehouse selectedWarehouse = row.getItem();
                    openWarehouseProductsModal(selectedWarehouse);
                }
            });
            return row;
        });
        // Configure table view style and resizing policy
        warehouseTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        warehouseTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        loadData();
        initializeModalPane();
    }   
    
    /**
     * Initializes and configures the modal pane for the stack pane.
     */
    private void initializeModalPane() {
        StackPane root = stackPane;
        modalPane = new ModalPane();
        modalPane.setId("modalPane");
        root.getChildren().add(modalPane);
    }
    
    /**
     * Configures the columns of the warehouse table.
     * 
     * This method binds the table columns to the appropriate properties of the Warehouse model.
     * The totalProductsColumn computes the total products in a warehouse dynamically.
     */
    private void configureTableColumns(){
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        
        // Compute total products in each warehouse
        totalProductsColumn.setCellValueFactory(data -> {
            Warehouse warehouse = data.getValue();
            Set<ProductWarehouse> productWarehouses = warehouse.getProductWarehouses();
            if(productWarehouses  != null){
                int totalProducts = productWarehouses.stream()
                                                 .mapToInt(ProductWarehouse::getAmount)
                                                 .sum();
                return new ReadOnlyObjectWrapper<>(totalProducts);
            }    
            else
                return new ReadOnlyObjectWrapper<>(0);
        });
    }
    
    /**
     * Loads the list of warehouses from the database and updates the table view.
     */
    private void loadData(){
        List<Warehouse> warehouses = warehouseDAO.getWarehouses();
        warehouseObservableList.clear();
        if(warehouses != null) warehouseObservableList.addAll(warehouses);
    }
    
    /**
     * Opens the form to add a new warehouse.
     */
    @FXML
    private void addWarehouse(){
        openAddWarehouseForm();
    }
    
    /**
     * Opens the form to edit an existing warehouse.
     */
    @FXML
    private void editWarehouse(){
        openEditWarehouseForm();
    }
    
    /**
     * Opens the "Add Warehouse" form in a modal pane.
     */
    private void openAddWarehouseForm(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddWarehouse.fxml"));
            Parent root = loader.load();
            AddWarehouseController addController = loader.getController();
            addController.setModalPane(modalPane);
            modalPane.setContent(root);
            modalPane.show(root);
            addController.setOnCloseAction(() -> loadData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Opens the "Edit Warehouse" form in a modal pane.
     */
    private void openEditWarehouseForm(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditWarehouse.fxml"));
            Parent root = loader.load();
            EditWarehouseController editController = loader.getController();
            editController.setModalPane(modalPane);
            modalPane.setContent(root);
            modalPane.show(root);
            editController.setOnCloseAction(() -> loadData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Opens a modal to display the products stored in the selected warehouse.
     * 
     * @param warehouse The selected warehouse to view products for.
     */
    private void openWarehouseProductsModal(Warehouse warehouse){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WarehouseProducts.fxml"));
            Parent root = loader.load();
            WarehouseProductsController warehouseProductsController = loader.getController();
            warehouseProductsController.setWarehouse(warehouse);
            modalPane.setContent(root);
            modalPane.show(root);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
