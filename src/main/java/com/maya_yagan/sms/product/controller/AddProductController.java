package com.maya_yagan.sms.product.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.ProductUnit;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.util.MenuButtonUtil;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.DateUtil;
import com.maya_yagan.sms.util.ExceptionHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;

/**
 * FXML Controller class for adding a Product in the application.
 * 
 * @author Maya Yagan
 */
public class AddProductController implements Initializable {

    @FXML private TextField productNameField, priceField;
    @FXML private DatePicker productionDatePicker, expirationDatePicker;
    @FXML private MenuButton categoryMenuButton, unitMenuButton;

    private Runnable onCloseAction;
    private ModalPane modalPane;
    private Category selectedCategory;
    private ProductUnit selectedUnit;
    private final ProductService productService = new ProductService();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCategories();
        loadProductUnits();
        DateUtil.applyDateFormat(productionDatePicker);
        DateUtil.applyDateFormat(expirationDatePicker);
    }    
    
    private void loadProductUnits(){
        MenuButtonUtil.populateMenuButton(
       unitMenuButton,
                productService::getProductUnits,
                ProductUnit::getFullName,
                unit -> {
                    selectedUnit = unit;
                    unitMenuButton.setText(unit.getFullName());
                },
     "Select",
                () -> {
                    selectedUnit = null;
                    unitMenuButton.setText("Select");
                }
        );
    }
    
    private void loadCategories() {
        MenuButtonUtil.populateMenuButton(
       categoryMenuButton,
                productService::getAllCategories,
                Category::getName,
                category -> {
                    selectedCategory = category;
                    categoryMenuButton.setText(category.getName());
                },
     "Select",
 onCloseAction
        );
    }

    public void setModalPane(ModalPane modalPane){
         this.modalPane = modalPane;
    }
     
    public void setOnCloseAction(Runnable onCloseAction){
         this.onCloseAction = onCloseAction;
    }

    @FXML public void saveProduct() {
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

    @FXML public void close() {
        if(modalPane != null) modalPane.hide();
    }
}