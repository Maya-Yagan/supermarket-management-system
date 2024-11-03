package com.maya2002yagan.supermarket_management.controller;

import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.ProductDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Product;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.Set;


/**
 * FXML Controller class
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
    private MenuButton categoryMenuButton;
    
    @FXML
    private Label warningLabel;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();

    private Category selectedCategory = null;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCategories();
        saveButton.setOnAction(event -> saveProduct());
        cancelButton.setOnAction(event -> closeForm());
    }    
    
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

    private void saveProduct() {
        // Clear previous warnings
        warningLabel.setText("");

        // Collect and validate form inputs
        String name = productNameField.getText();
        String priceText = priceField.getText();
        LocalDate productionDate = productionDatePicker.getValue();
        LocalDate expirationDate = expirationDatePicker.getValue();

        if (name.isEmpty() || priceText.isEmpty() || productionDate == null || expirationDate == null || selectedCategory == null) {
            warningLabel.setText("Please fill in all fields.");
            return;
        }

        // Parse the price
        float price;
        try {
            price = Float.parseFloat(priceText);
        } catch (NumberFormatException e) {
            warningLabel.setText("Invalid price format.");
            return;
        }

        // Check date validity
        if (expirationDate.isBefore(productionDate)) {
            warningLabel.setText("Expiration date invalid. ");
            return;
        }

        // Create the new product object
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setProductionDate(productionDate);
        product.setExpirationDate(expirationDate);
        product.setCategory(selectedCategory);

        // Save the product to the database
        productDAO.insertProduct(product);

        // Close the form (or clear the fields if you want to allow adding multiple products)
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
}
