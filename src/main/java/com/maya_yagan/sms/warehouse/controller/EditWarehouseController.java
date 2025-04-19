package com.maya_yagan.sms.warehouse.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ExceptionHandler;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import java.net.URL;
import java.util.ResourceBundle;
import com.maya_yagan.sms.warehouse.service.WarehouseService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

/**
 * Controller class for editing warehouse information.
 * 
 * @author Maya Yagan
 */
public class EditWarehouseController implements Initializable {

    @FXML TextField nameField, capacityField;
    private Runnable onCloseAction;
    private ModalPane modalPane;
    private Warehouse warehouse;
    private final WarehouseService warehouseService = new WarehouseService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void setWarehouse(Warehouse warehouse){
        this.warehouse = warehouse;
        populateFields();
    }

    private void populateFields(){
        if(warehouse == null) return;
        nameField.setText(warehouse.getName());
        capacityField.setText(String.valueOf(warehouse.getCapacity()));
    }

    @FXML
    private void handleSave(){
        try{
            warehouseService.updateWarehouse(warehouse,
                    nameField.getText(),
                    capacityField.getText());
            if(onCloseAction != null) onCloseAction.run();
            close();
        } catch (CustomException e){
            ExceptionHandler.handleException(e);
        }
    }

    @FXML
    private void close(){ if(modalPane != null) modalPane.hide(); }

    public void setModalPane(ModalPane modalPane){ this.modalPane = modalPane; }

    public void setOnCloseAction(Runnable onCloseAction){ this.onCloseAction = onCloseAction; }
}