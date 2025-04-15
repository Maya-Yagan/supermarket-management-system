package com.maya_yagan.sms.product.controller;

import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ExceptionHandler;
import javafx.fxml.FXML;

/**
 * FXML Controller class for adding a Product in the application.
 * 
 * @author Maya Yagan
 */
public class AddProductController extends BaseProductController {
 
    @Override
    @FXML
    public void handleSave() {
        try{
            if(productService.addProduct(
                    productNameField.getText(), 
                priceField.getText(), 
            productionDatePicker.getValue(),
            expirationDatePicker.getValue(), 
                 selectedCategory,
                    selectedUnit)){
                if(onCloseAction != null) onCloseAction.run();
                close();
            }
        } catch(CustomException e){
            ExceptionHandler.handleException(e);
        }
    }
}