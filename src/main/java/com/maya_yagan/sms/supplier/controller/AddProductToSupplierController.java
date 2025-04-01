package com.maya_yagan.sms.supplier.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.product.dao.CategoryDAO;
import com.maya_yagan.sms.product.dao.ProductDAO;
import com.maya_yagan.sms.supplier.dao.SupplierDAO;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.util.AlertUtil;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.CheckBoxTableCell;
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
    private TableColumn<Product, String> nameColumn, unitColumn;
    @FXML
    private TableColumn<Product, Boolean> selectColumn;
    @FXML
    private MenuButton categoryMenuButton;
    @FXML 
    private Button saveButton, cancelButton;
    
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final SupplierDAO SupplierDAO = new SupplierDAO();
    private final ObservableList<Product> productObservableList = FXCollections.observableArrayList();
    private final Map<Product, SimpleBooleanProperty> selectedProducts = new HashMap<>();
    private final Map<Product, Float> productsPrice = new HashMap<>();
    
    private Supplier supplier;
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
        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUnit().getShortName()));
        setupSelectColumn();
    }
    
    /**
     * Sets up the select column with checkboxes.
     */
    private void setupSelectColumn(){
        productsTable.setEditable(true);
        selectColumn.setEditable(true);
        selectColumn.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            SimpleBooleanProperty selectedProperty =
                    selectedProducts.computeIfAbsent(product, p -> {
                        SimpleBooleanProperty prop = new SimpleBooleanProperty(false);
                        prop.addListener((obs, oldVal, newVal) -> {
                            if(newVal){
                                TextInputDialog dialog = new TextInputDialog("0.0");
                                dialog.setTitle("Enter the Supplier Price");
                                dialog.setHeaderText("Enter the supplier price for " + product.getName());
                                dialog.setContentText("Price:");
                                
                                Optional<String> result = dialog.showAndWait();
                                if(result.isPresent()){
                                    try{
                                        float price = Float.parseFloat(result.get());
                                        if(price <= 0){
                                            prop.set(false);
                                            AlertUtil.showAlert(Alert.AlertType.ERROR,
                                                    "Invalid Input", "The price must be greater than zero");
                                        }
                                        else
                                            productsPrice.put(product, price);
                                            
                                    } catch(NumberFormatException e){
                                        prop.set(false);
                                        AlertUtil.showAlert(Alert.AlertType.ERROR,
                                                "Invalid Input", "Please enter a valid number");
                                    }
                                }
                                else //the dialog was cancelled
                                    prop.set(false);
                            }
                            else //remove the product price if unchecked
                                productsPrice.remove(product);
                        });
                        return prop;
                    });
            return selectedProperty;
        });
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
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
        boolean anySelected = false;
        for(Map.Entry<Product, SimpleBooleanProperty> entry : selectedProducts.entrySet()){
            if(entry.getValue().get()){
                anySelected = true;
                Product product = entry.getKey();
                
                if(!productsPrice.containsKey(product)){
                    AlertUtil.showAlert(Alert.AlertType.WARNING,
                            "Missing Price", 
                           "Please enter a valid suppleir price for " + product.getName());
                    return;
                }
                
                Optional<SupplierProduct> existingSupplierProduct = supplier.getSupplierProducts()
                        .stream()
                        .filter(sp -> sp.getProduct().equals(product))
                        .findFirst();
                if(!existingSupplierProduct.isPresent()){
                    float supplierPrice = productsPrice.get(product);
                    SupplierProduct newSupplierProduct = new SupplierProduct(product, supplier, supplierPrice);
                    supplier.getSupplierProducts().add(newSupplierProduct);
                }
            }
        }
        if(!anySelected){
            AlertUtil.showAlert(Alert.AlertType.WARNING,
                    "No Product Selected",
                    "Please select at least one product to add");
            return;
        }
        SupplierDAO.updateSupplier(supplier);
        AlertUtil.showAlert(Alert.AlertType.INFORMATION,
                "Success",
                "The selected products were added successfully.");
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
