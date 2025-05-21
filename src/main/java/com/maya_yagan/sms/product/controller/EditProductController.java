package com.maya_yagan.sms.product.controller;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ExceptionHandler;
import javafx.fxml.FXML;

public class EditProductController extends BaseProductController {

    private Product product;

    public void setProduct(Product product){
        this.product = product;
        populateFields();
    }

    private void populateFields(){
        if (product == null) return;

        productNameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        discountField.setText(String.valueOf(product.getDiscount()));
        productionDatePicker.setValue(product.getProductionDate());
        expirationDatePicker.setValue(product.getExpirationDate());
        barcodeField.setText(product.getBarcode());
        selectedCategory = product.getCategory();
        selectedUnit = product.getUnit();

        if (selectedCategory != null)
            categoryMenuButton.setText(selectedCategory.getName());

        if (selectedUnit != null)
            unitMenuButton.setText(selectedUnit.getFullName());
    }

    @FXML
    @Override
    public void handleSave(){
        try {
            String discountInput = discountField.getText();
            String discount = (discountInput == null || discountInput.trim().isEmpty()) ? "0" : discountInput.trim();

            product.setBarcode(barcodeField.getText());
            productService.updateProductData(
                    product,
                    productNameField.getText(),
                    priceField.getText(),
                    discount,
                    productionDatePicker.getValue(),
                    expirationDatePicker.getValue(),
                    selectedCategory,
                    selectedUnit,
                    barcodeField.getText()
            );
            if (onCloseAction != null) onCloseAction.run();
            close();
        } catch (CustomException e) {
            ExceptionHandler.handleException(e);
        }
    }
}
