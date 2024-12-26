package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.ProductDAO;
import com.maya2002yagan.supermarket_management.dao.WarehouseDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.ProductWarehouse;
import com.maya2002yagan.supermarket_management.model.Warehouse;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

/**
 * Controller class for adding products to a warehouse.
 *
 * This class facilitates the selection and addition of products to a warehouse. 
 * Users can filter products by category, select a product, specify the quantity, 
 * and save the product to the warehouse. The controller also validates warehouse 
 * capacity before adding products.
 * 
 * @author Maya Yagan
 */
public class AddProductToWarehouseController implements Initializable {

    @FXML
    private StackPane stackPane;
    @FXML
    private TableView<Product> productsTable;
    @FXML
    private TableColumn<Product, Integer> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product, String> productionDateColumn;
    @FXML
    private TableColumn<Product, String> expirationDateColumn;
    @FXML
    private MenuButton categoryMenuButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Spinner<Integer> spinnerAmount;

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final ObservableList<Product> productObservableList = FXCollections.observableArrayList();

    private Warehouse warehouse;
    private ModalPane modalPane;
    private Runnable onCloseAction;
    private Category selectedCategory;
    private Product selectedProduct;

    /**
     * Initializes the controller.
     *
     * Sets up the table columns, initializes the spinner for specifying product 
     * quantity, populates the category menu, and configures event handlers for 
     * user interactions.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        productsTable.setItems(productObservableList);
        spinnerAmount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1));
        populateCategoryMenu();
        setupEventHandlers();
        initializeModalPane();
    }
    
    /**
     * Sets the warehouse where products will be added.
     *
     * @param warehouse The warehouse to which products will be added.
     */
    private void initializeModalPane() {
        StackPane root = stackPane;
        modalPane = new ModalPane();
        modalPane.setId("modalPane");
        root.getChildren().add(modalPane);
    }

    /**
     * Sets the warehouse where products will be added.
     *
     * @param warehouse The warehouse to which products will be added.
     */
    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }
    
    /**
     * Configures the table columns to display product data.
     *
     * Sets up columns for product ID, name, price, production date, and expiration date.
     */
    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productionDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductionDate().toString()));
        expirationDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExpirationDate().toString()));
    }

    /**
     * Populates the category menu with available categories.
     *
     * Retrieves categories from the database and adds them to the category menu. 
     * Configures category selection to filter products in the table.
     */
    private void populateCategoryMenu() {
        categoryMenuButton.getItems().clear();
        categoryDAO.getCategories().forEach(category -> {
            MenuItem menuItem = new MenuItem(category.getName());
            menuItem.setOnAction(event -> handleCategorySelection(category));
            categoryMenuButton.getItems().add(menuItem);
        });
    }

    /**
     * Handles category selection from the menu.
     *
     * Updates the selected category and filters the product list to display 
     * only products in the chosen category.
     *
     * @param category The selected category.
     */
    private void handleCategorySelection(Category category) {
        selectedCategory = category;
        categoryMenuButton.setText(category.getName());
        loadProductsByCategory();
    }

    /**
     * Loads products belonging to the selected category into the table.
     */
    private void loadProductsByCategory() {
        productObservableList.clear();
        if (selectedCategory != null) {
            productObservableList.addAll(productDAO.getProductsByCategory(selectedCategory));
        }
    }

    /**
     * Sets up event handlers for user actions.
     *
     * Configures actions for the "Save" and "Cancel" buttons, and monitors product 
     * selection in the table.
     */
    private void setupEventHandlers() {
        saveButton.setOnAction(event -> saveProductToWarehouse());
        cancelButton.setOnAction(event -> closeModal());
        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedProduct = newSelection;
        });
    }

    /**
     * Saves the selected product to the warehouse with the specified quantity.
     *
     * Validates the warehouse's remaining capacity before adding the product. If 
     * the product already exists in the warehouse, updates the quantity; otherwise, 
     * creates a new entry. Displays success or error messages based on the result.
     */
    private void saveProductToWarehouse() {
        if (selectedProduct == null) {
            ShowAlert.showAlert(Alert.AlertType.WARNING, "No Product Selected", "Please select a product to add.");
            return;
        }

        int amountToAdd = spinnerAmount.getValue();
        int currentUsage = warehouse.getProductWarehouses().stream()
                .mapToInt(ProductWarehouse::getAmount).sum();
        int remainingCapacity = warehouse.getCapacity() - currentUsage;
        if(amountToAdd > remainingCapacity){
            if(remainingCapacity == 0) 
                ShowAlert.showAlert(Alert.AlertType.ERROR, "Warehouse Full", "The warehouse is already full. No more products can be added.");
            else
                ShowAlert.showAlert(Alert.AlertType.ERROR, "Capacity Exceeded", "The entered amount exceeds the remaining warehouse capacity. Remaining capacity: " + remainingCapacity);
            return;
        }
        
        Optional<ProductWarehouse> existingProductWarehouse = warehouse.getProductWarehouses()
                .stream()
                .filter(pw -> pw.getProduct().equals(selectedProduct))
                .findFirst();

        if (existingProductWarehouse.isPresent()) {
            // Update the amount for the existing product in the warehouse
            ProductWarehouse productWarehouse = existingProductWarehouse.get();
            productWarehouse.setAmount(productWarehouse.getAmount() + amountToAdd);
        } else {
            // Create a new ProductWarehouse entry if it doesn't already exist
            ProductWarehouse newProductWarehouse = new ProductWarehouse(warehouse, selectedProduct, amountToAdd);
            warehouse.getProductWarehouses().add(newProductWarehouse);
        }
        warehouseDAO.updateWarehouse(warehouse);
        ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Success", "The product amount was successfully updated.");
        if(onCloseAction != null) onCloseAction.run();
        closeModal();
    }

    /**
     * Closes the modal pane and clears any active UI elements.
     */
    private void closeModal() {
        if(modalPane != null) modalPane.hide();
    }

    /**
     * Sets the modal pane for displaying this controller's UI.
     *
     * @param modalPane The modal pane to display additional UI content.
     */
    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    /**
     * Sets the action to perform when the modal is closed.
     *
     * @param onCloseAction A runnable task to execute on close.
     */
    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
}
