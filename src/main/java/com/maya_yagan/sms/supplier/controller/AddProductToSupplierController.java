package com.maya_yagan.sms.supplier.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.service.SupplierService;
import com.maya_yagan.sms.util.*;

import java.util.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class for adding products to a supplier.
 *
 * @author Maya Yagan
 */
public class AddProductToSupplierController extends AbstractTableController<Product> {

    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn, unitColumn;
    @FXML private TableColumn<Product, Boolean> selectColumn;
    @FXML private MenuButton categoryMenuButton;   
    @FXML  private Button saveButton, cancelButton;

    private final SupplierService supplierService = new SupplierService();
    private final ProductService productService = new ProductService();
    private final Map<Product, Float> selectedProducts = new HashMap<>();

    private Supplier supplier;
    private String currentCategory = "All Categories";
    private Runnable onCloseAction;
    private ModalPane modalPane;

    private void loadCategories() {
        MenuButtonUtil.populateMenuButton(
                categoryMenuButton,
                productService::getAllCategories,
                Category::getName,
                this::onCategorySelected,
                "All Categories",
                () -> {
                    currentCategory = "All Categories";
                    categoryMenuButton.setText("All Categories");
                    refresh();
                }
        );
    }

    @Override
    protected void configureColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        unitColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUnit().getShortName()));
        TableViewUtil.setupCheckboxColumn(
                selectColumn,
                selectedProducts,
                this::promptForPriceAndSelect
        );
    }

    @Override
    protected Collection<Product> fetchData() {
        return productService.getFilteredProductsByCategory(currentCategory);
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<Product>> menuItemsFor(Product item) {
        return List.of();
    }

    @Override
    protected void postInit(){
        setupEventHandlers();
        loadCategories();
        tableView.setEditable(true);
    }
    
    private void setupEventHandlers(){
        saveButton.setOnAction(event -> saveProductToSupplier());
        cancelButton.setOnAction(event -> close());
    }
    
    private void onCategorySelected(Category category){
        currentCategory = category.getName();
        categoryMenuButton.setText(category.getName());
        refresh();
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

    private void saveProductToSupplier(){
        try{
            supplierService.addProductsToSupplier(supplier, selectedProducts);
            AlertUtil.showAlert(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "The selected products were added successfully."
            );
            if(onCloseAction != null) onCloseAction.run();
            close();
        } catch(CustomException e){
            ExceptionHandler.handleException(e);
        }
    }
    
    public void setSupplier(Supplier supplier){
        this.supplier = supplier;
    }
    
    public void close(){
        if(modalPane != null) modalPane.hide();
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }
}
