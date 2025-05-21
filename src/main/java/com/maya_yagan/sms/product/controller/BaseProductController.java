package com.maya_yagan.sms.product.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.ProductUnit;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.util.DateUtil;
import com.maya_yagan.sms.util.MenuButtonUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 
 * @author Maya Yagan
 */
public abstract class BaseProductController implements Initializable {

    @FXML protected TextField productNameField, discountField, priceField , barcodeField;
    @FXML protected DatePicker productionDatePicker, expirationDatePicker;
    @FXML protected MenuButton categoryMenuButton, unitMenuButton;


    protected Category selectedCategory;
    protected ProductUnit selectedUnit;
    protected ModalPane modalPane;
    protected Runnable onCloseAction;
    protected final ProductService productService = new ProductService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        loadProductUnits();
        DateUtil.applyDateFormat(productionDatePicker);
        DateUtil.applyDateFormat(expirationDatePicker);
    }

    protected void loadCategories() {
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

    protected void loadProductUnits() {
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
    
    @FXML protected abstract void handleSave();

    @FXML
    protected void close() {
        if (modalPane != null) modalPane.hide();
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
}
