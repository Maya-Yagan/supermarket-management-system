package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.ProductDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.ProductUnit;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.scene.control.Alert;


/**
 * FXML Controller class for handling the logic of the "Add Product" form in the application.
 * 
 * This controller manages the interaction with the UI components of the "Add Product" form, which includes 
 * input fields for the product's name, price, production date, expiration date, and category. It provides 
 * functionality to validate the form fields, save the new product to the database, and handle the form's 
 * closing action.
 * 
 * @author Maya Yagan
 */
public class AddProductFormController implements Initializable {

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
    private Button saveButton;
    
    @FXML
    private Button cancelButton;

    private Runnable onCloseAction;
    private ModalPane modalPane;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private Category selectedCategory;
    private ProductUnit selectedUnit;
    
    /**
     * Initializes the controller class.
     * 
     * This method sets up the category menu and action handlers for the save and cancel buttons.
     * The save button calls the saveProduct method, while the cancel button calls the closeForm method.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCategories();
        loadProductUnits();
        saveButton.setOnAction(event -> saveProduct());
        cancelButton.setOnAction(event -> closeForm());
    }    
    
    /**
     * Loads the product units
     */
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
     * Loads categories from the database and populates the category selection menu.
     * 
     * This method fetches all available categories from the database using the CategoryDAO, 
     * and adds them as menu items to the categoryMenuButton. When a menu item is selected, 
     * it sets the selectedCategory to the corresponding category and updates the button's text.
     */
    private void loadCategories() {
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
     
     /**
      * Sets the current modal pane for the form.
      * 
      * @param modalPane The modal pane to be set.
      */
     public void setModalPane(ModalPane modalPane){
         this.modalPane = modalPane;
     }
     
     /**
      * Sets the action to be executed when the form is closed.
      * 
      * @param onCloseAction The close action to be set.
      */
     public void setOnCloseAction(Runnable onCloseAction){
         this.onCloseAction = onCloseAction;
     }

     /**
     * Saves the product by validating the form fields and inserting it into the database.
     * 
     * This method collects the values from the form fields, validates them (checking for empty fields, 
     * valid price format, and expiration date validity), and creates a new Product object. If all fields are 
     * valid, the product is inserted into the database using the ProductDAO. After saving, the onCloseAction 
     * is run, and the form is closed.
     */
    private void saveProduct() {
        // Collect and validate form inputs
        String name = productNameField.getText();
        String priceText = priceField.getText();
        LocalDate productionDate = productionDatePicker.getValue();
        LocalDate expirationDate = expirationDatePicker.getValue();

        // Check if any fields are empty
        if (name.isEmpty() || priceText.isEmpty() || productionDate == null || selectedCategory == null || selectedUnit == null) {
            ShowAlert.showAlert(Alert.AlertType.WARNING, "Missing Fields","Please fill in all fields.");
            return;
        }

        // Parse the price
        float price;
        try {
            price = Float.parseFloat(priceText);
        } catch (NumberFormatException e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Invalid Price", "Invalid price format.");
            return;
        }

        // Check if the expiration date is before the production date
        if (expirationDate != null && expirationDate.isBefore(productionDate)) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Invalid Date", "Expiration date is invalid.");
            return;
        }

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setProductionDate(productionDate);
        product.setExpirationDate(expirationDate);
        product.setCategory(selectedCategory);
        product.setUnit(selectedUnit);
        
        productDAO.insertProduct(product);
        if(onCloseAction != null) onCloseAction.run();
        closeForm();
    }

    /**
     * Closes the form by hiding the modal pane.
     * 
     * This method hides the form by calling the hide method on the modal pane
     */
    private void closeForm() {
        if(modalPane != null) modalPane.hide();
    }
}
