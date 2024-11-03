/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.maya2002yagan.supermarket_management.controller;

import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Maya Yagan
 */
public class AddCategoryController implements Initializable {

    @FXML
    private TextField categoryName;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label warningLabel;
    
    private final CategoryDAO categoryDAO = new CategoryDAO();
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        saveButton.setOnAction(event -> saveProduct());
        cancelButton.setOnAction(event -> closeForm());
    }    
    
    private void saveProduct(){
        warningLabel.setText("");
        String name = categoryName.getText();
        if(name.isEmpty()){
            warningLabel.setText("Please fill all fields");
            return;
        }
        Category category = new Category();
        category.setName(name);
        categoryDAO.insertCategory(category);
        closeForm();
    }
    
    private void closeForm(){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
