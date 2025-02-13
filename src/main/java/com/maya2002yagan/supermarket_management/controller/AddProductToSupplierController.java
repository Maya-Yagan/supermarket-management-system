package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.ProductDAO;
import com.maya2002yagan.supermarket_management.dao.SupplierDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.Supplier;
import com.maya2002yagan.supermarket_management.model.SupplierProduct;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class for adding products to a supplier.
 * 
 * This class facilitates the selection and addition of products to a supplier.
 * Users can filter products by category, select a product and save the product
 * to the supplier.
 *
 * @author Maya Yagan
 */
public class AddProductToSupplierController implements Initializable {

    @FXML
    private TableView<Product> productsTable;
    @FXML
    private TableColumn<Product, Integer> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, String> priceColumn;
    @FXML
    private MenuButton categoryMenuButton;
    @FXML 
    private Button saveButton, cancelButton;
    
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final SupplierDAO SupplierDAO = new SupplierDAO();
    private final ObservableList<Product> productObservableList = FXCollections.observableArrayList();
    
    private Supplier supplier;
    private Product selectedProduct;
    private Category selectedCategory;
    private Runnable onCloseAction;
    private ModalPane modalPane;
    
    /**
     * Initializes the controller.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        populateCategoryMenu();
        setupEventHandlers();
    }    
    
    /**
     * Configures the table columns to display product data.
     */
    private void configureTableColumns() {
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productsTable.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        productsTable.setItems(productObservableList);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            if (supplier == null) {
                return new SimpleStringProperty("Unavailable");
            }
            SupplierProduct supplierProduct = supplier.getSupplierProducts().stream()
                    .filter(sp -> sp.getProduct().equals(product))
                    .findFirst()
                    .orElse(null);
            return new SimpleStringProperty(supplierProduct != null ? String.valueOf(supplierProduct.getPrice()) : "Unavailable");
        });
    }

    /**
     * Populates the category menu with available categories.
     */
    private void populateCategoryMenu(){
        categoryMenuButton.getItems().clear();
        categoryDAO.getCategories().forEach(category -> {
            MenuItem menuItem = new MenuItem(category.getName());
            menuItem.setOnAction(event -> handleCategorySelection(category));
            categoryMenuButton.getItems().add(menuItem);
        });
    }
    
    /**
     * Sets up handlers for events like clicking buttons
     */
    private void setupEventHandlers(){
        saveButton.setOnAction(event -> saveProductToSupplier());
        cancelButton.setOnAction(event -> closeModal());
        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedProduct = newSelection;
        });
    }
    
    /**
     * Handles category selection from the menu.
     * 
     * @param category The selected category
     */
    private void handleCategorySelection(Category category){
        selectedCategory = category;
        categoryMenuButton.setText(category.getName());
        loadProductsByCategory();
    }
    
    /**
     * Loads products belonging to the selected category into the table.
     */
    private void loadProductsByCategory(){
        productObservableList.clear();
        if(selectedCategory != null)
            productObservableList.addAll(productDAO.getProductsByCategory(selectedCategory));
    }
    
    /**
     * Saves the selected product to the supplier.
     */
    private void saveProductToSupplier(){
        if(selectedProduct == null){
            ShowAlert.showAlert(Alert.AlertType.WARNING, "No Product Selected", "Please select a product to add.");
            return;
        }
        
        Optional<SupplierProduct> existingSupplierProduct = supplier.getSupplierProducts()
                .stream()
                .filter(sp -> sp.getProduct().equals(selectedProduct))
                .findFirst();
        if(!existingSupplierProduct.isPresent()){
            SupplierProduct newSupplierProduct = new SupplierProduct(selectedProduct, supplier, 0);
            supplier.getSupplierProducts().add(newSupplierProduct);
        }
        SupplierDAO.updateSupplier(supplier);
        ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Success", "The product was successfully added.");
        if(onCloseAction != null) onCloseAction.run();
        closeModal();
    }
    
    /**
     * Sets the supplier which the products belong to.
     * 
     * @param supplier The supplier of the products.
     */
    public void setSupplier(Supplier supplier){
        this.supplier = supplier;
    }
    
    /**
     * Closes the modal pane and clears any active UI elements.
     */
    public void closeModal(){
        if(modalPane != null) modalPane.hide();
    }

    /**
     * Sets the action to perform when the modal is closed.
     *
     * @param onCloseAction A runnable task to execute on close.
     */
    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    /**
     * Sets the modal pane for displaying this controller's UI.
     * 
     * @param modalPane The modal pane to display additional UI content.
     */
    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }
}
