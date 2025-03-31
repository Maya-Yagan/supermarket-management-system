package com.maya_yagan.sms.supplier.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.supplier.dao.SupplierDAO;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.util.ShowAlert;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * FXML Controller class for editing supplier details
 * Allows modification of supplier name, email and phone number
 * Handles form interactions, data validation and database update
 *
 * @author Maya Yagan
 */
public class EditSupplierController implements Initializable {

    @FXML
    TextField nameField, emailField, phoneNumberField;
    @FXML
    Button saveButton, cancelButton;
    
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private Supplier supplier;
    private ModalPane modalPane;
    private Runnable onCloseAction;
    
    /**
     * Initializes the controller, setting up event handlers and populating fields
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        populateFields();
        setupEventHandlers();
    } 
    
    /**
     * Populates the text fields with the supplier's current details
     */
    private void populateFields(){
        if(supplier != null){
            nameField.setText(supplier.getName());
            emailField.setText(supplier.getEmail());
            phoneNumberField.setText(supplier.getPhoneNumber());
        }
    }
    
    /**
     * Sets up event handlers for buttons
     */
    public void setupEventHandlers(){
        saveButton.setOnAction(event -> updateSupplier());
        cancelButton.setOnAction(event -> closeForm());
    }
    
    /**
     * Updates the supplier's details in the database
     * Displays a success message upon successful update and closes the form
     */
    public void updateSupplier(){
        if(supplier != null){
            supplier.setName(nameField.getText());
            supplier.setEmail(emailField.getText());
            supplier.setPhoneNumber(phoneNumberField.getText());
            supplierDAO.updateSupplier(supplier);
            if(onCloseAction != null) onCloseAction.run();
            ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier information has been updated successfully.");
            closeForm();
        }
    }
    
    /**
     * Closes the form by hiding the modal pane
     */
    public void closeForm(){
        if(modalPane != null) modalPane.hide();
    }

    /**
     * Sets the supplier whose details are being edited
     * 
     * @param supplier The supplier to be edited
     */
    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
        populateFields();
    }

    /**
     * Sets the modal pane used for displaying the form
     * 
     * @param modalPane The modal pane instance
     */
    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    /**
     * Sets an action to be executed when the form is closed
     * 
     * @param onCloseAction The action to execute on close
     */
    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    } 
}
