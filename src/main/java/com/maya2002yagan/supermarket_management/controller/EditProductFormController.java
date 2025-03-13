package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.ProductDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.ProductUnit;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

/**
 * FXML Controller class for editing product details in the supermarket management system.
 * 
 * This controller is responsible for handling the editing of product information. The updated information 
 * is saved to the database. The controller also provides functionality for deleting a product and 
 * canceling the editing operation.
 *
 * @author Maya Yagan
 */
public class EditProductFormController implements Initializable {

    @FXML
    private TextField productNameField;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private DatePicker productionDatePicker;
    
    @FXML
    private DatePicker expirationDatePicker;
    
    @FXML
    private MenuButton categoryMenuButton, unitMenuButton;
    
    @FXML
    private Label warningLabel;
    
    @FXML
    private Button cancelButton;
    
    private Product product;
    private ModalPane modalPane;
    private Runnable onCloseAction;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private Category selectedCategory;
    private ProductUnit selectedUnit;
    
    /**
     * Initializes the controller class.
     * 
     * @param location  The location of the FXML resource.
     * @param resources The resources to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCategories();
        loadProductUnits();
    }

    /**
     * Sets the current product and populates the fields with the product's data.
     *
     * @param product The product to be set.
     */
    public void setProduct(Product product){
        this.product = product;
        populateFields();
    }
    
    /**
     * Sets the current modal pane.
     *
     * @param modalPane The modal pane to be set.
     */
    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
    
    /**
     * Sets the current close action.
     *
     * @param onCloseAction The close action to be set.
     */
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
    
    private void loadCategories(){
        Set<Category> categories = categoryDAO.getCategories();
        if (categories != null) {
            for (Category category : categories) {
                MenuItem menuItem = new MenuItem(category.getName());
                menuItem.setOnAction(event -> {
                    selectedCategory = category;
                    categoryMenuButton.setText(category.getName());
                });
                categoryMenuButton.getItems().add(menuItem);
            }
        }
    }
    
    private void loadProductUnits(){
        for(ProductUnit unit : ProductUnit.values()){
            MenuItem menuItem = new MenuItem(unit.getFullName());
            menuItem.setOnAction(event -> {
                selectedUnit = unit;
                unitMenuButton.setText(unit.getFullName());
            });
            unitMenuButton.getItems().add(menuItem);
        }
    }
    
    /**
     * Populates the form fields with the selected product's data.
     */
    private void populateFields(){
        if(product == null) return;
        
        productNameField.setText(product.getName());
        priceField.setText("" + product.getPrice());
        productionDatePicker.setValue(product.getProductionDate());
        expirationDatePicker.setValue(product.getExpirationDate());
        
        selectedCategory = product.getCategory();
        selectedUnit = product.getUnit();
        categoryMenuButton.setText(selectedCategory.toString());
        unitMenuButton.setText(selectedUnit.getFullName());
    }
    
    /**
     * Handles the deletion of a product by confirming the action with the user,
     * and if confirmed, deletes the product from the database.
     */
    @FXML
    private void handleDelete(){
        ShowAlert.showDeleteConfirmation(product,
                "Delete Product",
                "Are you sure you want to delete this product?",
                "This action cannot be undone",
                (Product p) -> {
                    productDAO.deleteProduct(p.getId());
                    if(onCloseAction != null) onCloseAction.run();
                    closeForm();
                });
    }
    
    /**
     * Saves the edited product information to the database. Updates the product info
     * and closes the form upon successful save.
     */
    @FXML
    private void handleSave(){
        if(product == null) return;
        
        product.setName(productNameField.getText());
        product.setPrice(Float.parseFloat(priceField.getText()));
        product.setProductionDate(productionDatePicker.getValue());
        product.setExpirationDate(expirationDatePicker.getValue());
        product.setCategory(selectedCategory);
        product.setUnit(selectedUnit);
        
        productDAO.updateProduct(product);
        if(onCloseAction != null) onCloseAction.run();
        closeForm();
    }
    
    /**
     * Cancels the operation and closes the form without saving changes.
     */
    @FXML
    private void handleCancel(){
        closeForm();
    }
    
    /**
     * Closes the current form window.
     */
    private void closeForm(){
        if(modalPane != null) modalPane.hide();
    }
}
