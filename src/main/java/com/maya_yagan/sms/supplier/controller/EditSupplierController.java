package com.maya_yagan.sms.supplier.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.supplier.dao.SupplierDAO;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.service.SupplierService;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ValidationService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * FXML Controller class for editing supplier details
 * 
 * @author Maya Yagan
 */

public class EditSupplierController implements Initializable {

    @FXML TextField nameField, emailField, phoneNumberField;
    @FXML Button saveButton, cancelButton;
    
    private final SupplierService supplierService = new SupplierService(); 
    private Supplier supplier;
    private ModalPane modalPane;
    private Runnable onCloseAction;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        populateFields();
        setupEventHandlers();
    } 
    
    private void populateFields(){
        if(supplier != null){
            nameField.setText(supplier.getName());
            emailField.setText(supplier.getEmail());
            phoneNumberField.setText(supplier.getPhoneNumber());
        }
    }
    
    public void setupEventHandlers(){
        saveButton.setOnAction(event -> updateSupplier());
        cancelButton.setOnAction(event -> closeForm());
    }
   
     public void updateSupplier(){
        if(supplier != null){
            try{
                supplierService.updateSupplierData(supplier , nameField.getText(), emailField.getText(),phoneNumberField.getText());
                
                if(onCloseAction != null) onCloseAction.run();
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "Supplier information has been updated successfully.");
                closeForm();
            }catch (CustomException e){
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());      
            }
        }
     }     
     
    public void closeForm(){
        if(modalPane != null) modalPane.hide();
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
        populateFields();
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    } 
}
