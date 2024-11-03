package com.maya2002yagan.supermarket_management.controller;

import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.ProductDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Product;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
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
    private MenuButton categoryMenuButton;
    
    @FXML
    private Label warningLabel;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private Product product;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private Category selectedCategory;
    
    /**
     * Initializes the controller class.
     * 
     * @param location  The location of the FXML resource.
     * @param resources The resources to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCategories();
    }

    public void setProduct(Product product){
        this.product = product;
        populateFields();
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
    
    private void populateFields(){
        if(product == null) return;
        
        productNameField.setText(product.getName());
        priceField.setText("" + product.getPrice());
        productionDatePicker.setValue(product.getProductionDate());
        expirationDatePicker.setValue(product.getExpirationDate());
        
        selectedCategory = product.getCategory();
        categoryMenuButton.setText(selectedCategory.toString());
    }
    
    @FXML
    private void handleDelete(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Are you sure you want to delete this product?");
        alert.setContentText("This action cannot be undone");
        alert.showAndWait().ifPresent(response -> {
            if(response == ButtonType.OK){
                productDAO.deleteProduct(product.getId());
                closeForm();
            }
        });
    }
    
    @FXML
    private void handleSave(){
        if(product == null) return;
        
        product.setName(productNameField.getText());
        product.setPrice(Float.parseFloat(priceField.getText()));
        product.setProductionDate(productionDatePicker.getValue());
        product.setExpirationDate(expirationDatePicker.getValue());
        product.setCategory(selectedCategory);
        
        productDAO.updateProduct(product);
        closeForm();
    }
    
    @FXML
    private void handleCancel(){
        closeForm();
    }
    
    private void closeForm(){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        if(stage != null) stage.close();
    }
}
