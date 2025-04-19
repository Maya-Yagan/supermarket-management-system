package com.maya_yagan.sms.warehouse.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ExceptionHandler;
import com.maya_yagan.sms.warehouse.dao.WarehouseDAO;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import java.net.URL;
import java.util.ResourceBundle;

import com.maya_yagan.sms.warehouse.service.WarehouseService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

/**
 * Controller class for adding a new warehouse.
 * 
 * @author Maya Yagan
 */
public class AddWarehouseController implements Initializable {
    
    @FXML private TextField nameField, capacityField;
    
    private Runnable onCloseAction;
    private ModalPane modalPane;
    private final WarehouseService warehouseService = new WarehouseService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    @FXML
    private void handleSave(){
        try{
            warehouseService.addWarehouse(
                    nameField.getText(),
                    capacityField.getText()
            );
            if(onCloseAction != null) onCloseAction.run();
            close();
        } catch(CustomException e){
            ExceptionHandler.handleException(e);
        }

    }

    @FXML
    private void close(){ if(modalPane != null) modalPane.hide(); }

    public void setModalPane(ModalPane modalPane){ this.modalPane = modalPane; }

    public void setOnCloseAction(Runnable onCloseAction){ this.onCloseAction = onCloseAction; }
}
