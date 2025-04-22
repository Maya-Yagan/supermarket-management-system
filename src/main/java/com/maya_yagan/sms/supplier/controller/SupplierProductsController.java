package com.maya_yagan.sms.supplier.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.supplier.service.SupplierService;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.ContextMenuUtil;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ExceptionHandler;
import com.maya_yagan.sms.util.MenuButtonUtil;
import com.maya_yagan.sms.util.ViewUtil;
import java.util.*;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * FXML Controller class for managing products of a supplier.
 *
 * @author Maya Yagan
 */
public class SupplierProductsController extends AbstractTableController<SupplierProduct> {
    
    @FXML private Text supplierName;
    @FXML private TableColumn<SupplierProduct, String> nameColumn;   
    @FXML private TableColumn<SupplierProduct, String> unitColumn;   
    @FXML private TableColumn<SupplierProduct, Float> priceColumn;
    @FXML private  Button addProductButton, backButton;   
    @FXML private  MenuButton categoryMenuButton;   
    @FXML private Label infoLabel;
    @FXML private StackPane stackPane;

    private ModalPane modalPane;
    private Supplier supplier;
    private Runnable onCloseAction;
    private Category selectedCategory;
    private final SupplierService supplierService = new SupplierService();
    private final ProductService productService = new ProductService();

    private void loadCategories() {
        MenuButtonUtil.populateMenuButton(
                categoryMenuButton,
                productService::getAllCategories,
                Category::getName,
                this::onCategorySelected,
                "All Categories",
                () -> {
                    selectedCategory = null;
                    categoryMenuButton.setText("All Categories");
                    refresh();
                }
        );
    }

    @Override
    protected void configureColumns() {
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        priceColumn.setCellValueFactory(cellData ->
                new SimpleFloatProperty(cellData.getValue().getPrice()).asObject());
        unitColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getUnit().getFullName()));
    }

    @Override
    protected Collection<SupplierProduct> fetchData() {
        if (supplier == null) return Collections.emptyList();
        return supplierService.getSupplierProductsByCategory(supplier, selectedCategory);
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<SupplierProduct>> menuItemsFor(SupplierProduct supplierProduct) {
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Edit Price", (item, row) -> handleEditAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Delete Product", (item, row) -> handleDeleteAction(item))
        );
    }

    @Override
    protected void postInit(){
        setupEventHandlers();
        loadCategories();
        modalPane = ViewUtil.initializeModalPane(stackPane);
        setupDynamicLayoutAdjustment();
    }
    
    private void setupEventHandlers(){
        addProductButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/supplier/AddProductToSupplier.fxml",
                (AddProductToSupplierController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setSupplier(supplier);
                    controller.setOnCloseAction(this::refresh);
                }, modalPane));
        backButton.setOnAction(event -> ViewUtil.switchView("/view/supplier/SupplierManagement.fxml",
                controller -> {}, stackPane, "Supplier Management"));
    }

    private void onCategorySelected(Category category){
        selectedCategory = category;
        categoryMenuButton.setText(category.getName());
        refresh();
    }

    private void handleDeleteAction(SupplierProduct supplierProduct) {
        AlertUtil.showDeleteConfirmation(supplierProduct,
                "Delete Product",
                "Are you sure you want to delete this product?",
                "This action cannot be undone.",
                deletedProduct -> {
                    supplierService.deleteProductFromSupplier(
                            deletedProduct.getSupplier(),
                            deletedProduct.getProduct()
                    );
                    refresh();
                }
        );
    }

    private void handleEditAction(SupplierProduct sp) {
        ViewUtil.showFloatInputDialog(
                "Edit Price",
                "Enter new price for " + sp.getProduct().getName(),
                "Price (" + sp.getProduct().getUnit().getShortName() + ")",
                sp.getPrice(),
                "Price",
                newPrice -> {
                    try {
                        supplierService.updateSupplier(supplier, sp, newPrice);
                        refresh();
                        AlertUtil.showAlert(
                                Alert.AlertType.INFORMATION,
                                "Success",
                                "The product price was successfully updated.");
                    } catch (CustomException ex) {
                        ExceptionHandler.handleException(ex);
                        refresh();
                    }
                }
        );
    }

    public void setSupplier(Supplier supplier){
        this.supplier = supplier;
        supplierName.setText(supplier.getName());
        refresh();
    }

    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }

    private void setupDynamicLayoutAdjustment(){
        ViewUtil.setupDynamicLayoutAdjustment(
                categoryMenuButton,
                Arrays.asList(addProductButton, infoLabel),
                Arrays.asList(10.0, 140.0)
        );
    }
} 