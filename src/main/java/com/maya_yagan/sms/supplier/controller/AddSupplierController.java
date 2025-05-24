package com.maya_yagan.sms.supplier.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.supplier.service.SupplierService;

import java.util.*;

import com.maya_yagan.sms.util.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class for handling the logic for adding a supplier
 *
 * @author Maya Yagan
 */
public class AddSupplierController extends AbstractTableController<Product> {
    
    @FXML private TextField nameTextField, emailTextField, phoneTextField;
    @FXML private TableColumn<Product, String> productColumn;
    @FXML private TableColumn<Product, Boolean> selectColumn;
    @FXML private TableColumn<Product, Float> supplierPriceColumn;
    @FXML private MenuButton categoryMenuButton;
    @FXML private Button saveButton, cancelButton;

    private Runnable onCloseAction;
    private ModalPane modalPane;
    private String currentCategory = "All Categories";
    private final ProductService productService = new ProductService();
    private final SupplierService supplierService = new SupplierService();
    private final Map<Product, Float> selectedProducts = new HashMap<>();

    private void loadCategories() {
        MenuButtonUtil.populateMenuButton(
                categoryMenuButton,
                productService::getAllCategories,
                Category::getName,
                this::onCategorySelected,
                "All Categories",
                () -> {
                    currentCategory = "All Categories";
                    categoryMenuButton.setText(currentCategory);
                    refresh();
                }
        );
    }

    @Override
    protected void configureColumns() {
        productColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        supplierPriceColumn.setCellValueFactory(cell -> {
            Product product = cell.getValue();
            Float price = selectedProducts.getOrDefault(product,  0f);
            return new SimpleObjectProperty<>(price);
        });
        TableViewUtil.setupCheckboxColumn(
                selectColumn,
                selectedProducts,
                (product, selected) -> {
                    if(selected)
                        promptForPriceAndSelect(product);
                    else {
                        selectedProducts.remove(product);
                        refresh();
                    }
                });
        }

    @Override
    protected Collection<Product> fetchData() {
        return productService.getFilteredProductsByCategory(currentCategory);
    }

    @Override
    protected void postInit(){
        setupEventHandlers();
        loadCategories();
        tableView.setEditable(true);
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<Product>> menuItemsFor(Product s) {
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Edit Price", (item, row) -> promptForPriceAndSelect(item))
        );
    }

    private void setupEventHandlers(){
        saveButton.setOnAction(event -> handleSaveAction());
        cancelButton.setOnAction(event -> close());
    }

    private void onCategorySelected(Category category){
        currentCategory = category.getName();
        categoryMenuButton.setText(currentCategory);
        refresh();
    }

    private void handleSaveAction(){
        try{
            supplierService.addSupplier(
                    nameTextField.getText(),
                    emailTextField.getText(),
                    phoneTextField.getText(),
                    selectedProducts
            );
            if(onCloseAction != null) onCloseAction.run();
            close();
        } catch (CustomException e){
            ExceptionHandler.handleException(e);
        }
    }

    private void promptForPriceAndSelect(Product product){
        ViewUtil.showFloatInputDialog(
                "Enter Supplier Price",
                "Enter the price for " + product.getName(),
                "Price",
                0f,
                "Price",
                newAmt -> {
                    try{
                        selectedProducts.put(product, newAmt.floatValue());
                        refresh();
                    } catch (CustomException e){
                        ExceptionHandler.handleException(e);
                        tableView.refresh();
                    }
                }
        );
    }

    private void close(){
        if(modalPane != null) modalPane.hide();
    }

    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }

    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
}
