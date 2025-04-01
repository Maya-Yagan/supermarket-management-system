package com.maya_yagan.sms.warehouse.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.warehouse.dao.WarehouseDAO;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.util.AlertUtil;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Controller class for adding a new warehouse.
 *
 * This class provides functionality to create a new warehouse by entering its name 
 * and capacity. It validates user inputs and saves the warehouse data to the database. 
 * The form can also be closed without saving.
 * 
 * @author Maya Yagan
 */
public class AddWarehouseController implements Initializable {
    
    @FXML
    private TextField nameField;
    @FXML
    private TextField capacityField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    
    private Runnable onCloseAction;
    private ModalPane modalPane;
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    
    /**
     * Initializes the controller.
     *
     * Configures the event handlers for the "Save" and "Cancel" buttons. Adds validation 
     * to the capacity field to ensure only numeric input is allowed.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        saveButton.setOnAction(event -> saveWarehouse());
        cancelButton.setOnAction(event -> closeForm());
        capacityField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("\\d*")) capacityField.setText(oldValue);
        });
    }    
    
    /**
     * Sets the modal pane for displaying this controller's UI.
     *
     * @param modalPane The modal pane to display additional UI content.
     */
    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
    
    /**
     * Sets the action to perform when the form is closed.
     *
     * @param onCloseAction A runnable task to execute on close.
     */
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
    
    /**
     * Saves a new warehouse based on user input.
     *
     * Retrieves the warehouse name and capacity from the input fields, validates 
     * the data, and saves the warehouse to the database. Displays an error alert 
     * if fields are empty or invalid. Closes the form upon successful save.
     */
    private void saveWarehouse(){
        String name = nameField.getText();
        Integer capacity = Integer.valueOf(capacityField.getText());
        if(name.isEmpty() || capacity == 0){
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Empty Fields", "Please fill all fields");
            return;
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setName(name);
        warehouse.setCapacity(capacity);
        warehouseDAO.insertWarehouse(warehouse);
        if(onCloseAction != null) onCloseAction.run();
        closeForm();
    }
    
    /**
     * Closes the warehouse creation form.
     *
     * Hides the modal pane and clears any active UI elements.
     */
    private void closeForm(){
        if(modalPane != null) modalPane.hide();
    }
}
