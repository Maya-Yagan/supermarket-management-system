package com.maya_yagan.sms.product.controller;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ExceptionHandler;
import javafx.fxml.FXML;


/**
 * FXML Controller class for editing product details in the supermarket management system.
 * 
 * @author Maya Yagan
 */
public class EditProductController extends BaseProductController {

    private Product product;

    public void setProduct(Product product){
        this.product = product;
        populateFields();
    }
   
    private void populateFields(){
        if(product == null) return;
        productNameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        productionDatePicker.setValue(product.getProductionDate());
        expirationDatePicker.setValue(product.getExpirationDate());
        selectedCategory = product.getCategory();
        selectedUnit = product.getUnit();
        categoryMenuButton.setText(selectedCategory.toString());
        unitMenuButton.setText(selectedUnit.getFullName());
    }
    
    @FXML
    @Override
    public void handleSave(){
        try{
            productService.updateProductData(product, 
                    productNameField.getText(),
                    priceField.getText(),
                    productionDatePicker.getValue(), 
                    expirationDatePicker.getValue(), 
                    selectedCategory, 
                    selectedUnit);
            if(onCloseAction != null) onCloseAction.run();
            close();
        } catch(CustomException e){
            ExceptionHandler.handleException(e);
        }
    }
}
