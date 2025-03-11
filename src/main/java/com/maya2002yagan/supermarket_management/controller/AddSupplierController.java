package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.ProductDAO;
import com.maya2002yagan.supermarket_management.dao.SupplierDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.Supplier;
import com.maya2002yagan.supermarket_management.model.SupplierProduct;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

/**
 * FXML Controller class for handling the logic for adding a supplier
 *
 * @author Maya Yagan
 */
public class AddSupplierController implements Initializable {
    
    @FXML
    private TextField nameField, emailField, phoneNumberField;
    @FXML
    private MenuButton categoryMenu, productsMenu;
    @FXML
    private Button saveButton, cancelButton;
    
    private Runnable onCloseAction;
    private ModalPane modalPane;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private Category selectedCategory = null;

    /**
     * Initializes the controller class.
     * 
     * This method sets up the category menu and button event handlers.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCategories();
        setupEventHandlers();
    }    
    
    /**
     * Loads categories from the database and populates the category selection menu.
     * 
     * This method fetches all available categories from the database using the CategoryDAO, 
     * and adds them as menu items to the categoryMenu.
     */
    private void loadCategories(){
        Set<Category> categories = categoryDAO.getCategories();
        if(categories != null){
            for(Category category : categories){
                MenuItem menuItem = new MenuItem(category.getName());
                menuItem.setOnAction(event -> {
                    selectedCategory = category;
                    categoryMenu.setText(category.getName());
                    loadProductsByCategory(category);
                });
                categoryMenu.getItems().add(menuItem);
            }
        }
    }
    
    /**
     * Loads products for the selected category and updates the product selection menu.
     *
     * @param category The selected category for filtering products.
     */
    private void loadProductsByCategory(Category category){
        productsMenu.getItems().clear();
        Set<Product> products = productDAO.getProductsByCategory(category);
        if(products != null && !products.isEmpty()){
            for(Product product : products){
                CheckMenuItem productMenuItem = new CheckMenuItem(product.getName());
                productsMenu.getItems().add(productMenuItem);
            }
        } 
        else productsMenu.setText("No products available");
    }
    
    /**
     * Sets up event handlers for the save and cancel buttons.
     */
    private void setupEventHandlers(){
        saveButton.setOnAction(event -> saveSupplier());
        cancelButton.setOnAction(event -> closeForm());
    }
    
    /**
     * Saves the supplier with the provided details and selected products.
     * Displays alerts if required fields are missing or no products are selected.
     */
    private void saveSupplier(){
        String name = nameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();
        if(name.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || selectedCategory == null){
            ShowAlert.showAlert(Alert.AlertType.WARNING, "Empty fields!", "Please fill in all fields.");
            return;
        }
        Supplier supplier = new Supplier();
        supplier.setEmail(email);
        supplier.setName(name);
        supplier.setPhoneNumber(phoneNumber);
        Set<SupplierProduct> supplierProducts = new HashSet<>();
        //i need to find a way to set the suppleir products properly
        for(MenuItem menuItem : productsMenu.getItems()){
            if(menuItem instanceof CheckMenuItem checkMenuItem && checkMenuItem.isSelected()){
                String productName = checkMenuItem.getText();
                Product product = productDAO.getProductsByCategory(selectedCategory).stream()
                        .filter(p -> p.getName().equals(productName))
                        .findFirst()
                        .orElse(null);
                if(product != null){
                    SupplierProduct supplierProduct = new SupplierProduct();
                    supplierProduct.setProduct(product);
                    supplierProduct.setSupplier(supplier);
                    supplierProducts.add(supplierProduct);
                }
            }
        }
        if(supplierProducts.isEmpty()){
            ShowAlert.showAlert(Alert.AlertType.WARNING, "No products selected!", "Please select at least one product.");
            return;
        }
        supplier.setSupplierProducts(supplierProducts);
        
        supplierDAO.insertSupplier(supplier);
        if(onCloseAction != null) onCloseAction.run();
        closeForm();
    }
    
    /**
     * Closes the form by hiding the modal pane.
     * 
     * This method hides the form by calling the hide method on the modal pane
     */
    private void closeForm(){
        if(modalPane != null) modalPane.hide();
    }
    
    /**
     * Sets the action to be performed when the form is closed.
     *
     * @param onCloseAction The action to run on form close.
     */
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
    
    /**
     * Sets the modal pane for the form.
     *
     * @param modalPane The modal pane to be used for the form.
     */
    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
}
