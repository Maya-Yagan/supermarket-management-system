package com.maya_yagan.sms.product.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.product.dao.CategoryDAO;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.util.ShowAlert;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * FXML Controller class for handling the logic of adding a new category in the application.
 * 
 * This controller manages the interaction with the UI components of the "Add Category" form. It provides 
 * functionality to save a new category, handle validation for empty fields, and close the form. The controller 
 * also provides mechanisms for notifying the caller when the form is closed or a category is successfully added.
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
    
    private Runnable onCloseAction;
    private ModalPane modalPane;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    
    /**
     * Initializes the controller class.
     * 
     * This method sets up the action handlers for the save and cancel buttons. 
     * The save button calls the saveCategory method, while the cancel button calls the closeForm method.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        saveButton.setOnAction(event -> saveCategory());
        cancelButton.setOnAction(event -> closeForm());
    }  
    
    /**
     * Sets the modal pane for the form.
     * 
     * @param modalPane The ModalPane to set for the form.
     */
    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
    
    /**
     * Sets the action to be executed when the form is closed.
     * 
     * @param onCloseAction The action to perform when the form is closed (can be null).
     */
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
    
    /**
     * Saves the category by validating the input and inserting it into the database.
     * 
     * The method first checks if the category name is empty and shows an error alert if so. If the name is valid,
     * it creates a new Category object, sets its name, and inserts it into the database using the CategoryDAO.
     * After saving, it runs the onCloseAction and closes the form.
     */
    private void saveCategory(){
        // Get the text entered in the category name field
        String name = categoryName.getText();
        
        // Check if the name field is empty
        if(name.isEmpty()){
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Empty Fields", "Please fill all fields");
            return;
        }
        // Create a new Category object and set its name
        Category category = new Category();
        category.setName(name);
        
        // Insert the category into the database
        categoryDAO.insertCategory(category);
        
        // Run the onCloseAction if it's set
        if(onCloseAction != null) onCloseAction.run();
        
        // Close the form after saving
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
}
