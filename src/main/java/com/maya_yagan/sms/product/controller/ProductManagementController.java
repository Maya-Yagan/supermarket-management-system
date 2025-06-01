package com.maya_yagan.sms.product.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.ProductUnit;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.settings.service.SettingsService;
import com.maya_yagan.sms.util.*;
import javafx.beans.property.SimpleStringProperty;

import java.util.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class for managing products in the supermarket management system.
 *
 * @author Maya Yagan
 */
public class  ProductManagementController extends AbstractTableController<Product> {

    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn, productionDate, expirationDateColumn, unitColumn , barcodeColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Float> discountsColumn;
    @FXML private Button addProductButton, addCategoryButton, editCategoriesButton;
    @FXML private MenuButton categoryMenuButton;
    @FXML private StackPane stackPane;
    @FXML private Label infoLabel;


    private ModalPane modalPane;
    private String currentCategory = "All Categories";
    private final ProductService productService = new ProductService();
    private final SettingsService settingsService = new SettingsService();
    private final String moneyUnit = settingsService.getSettings().getMoneyUnit();

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
    protected  void configureColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setText("Price (" + moneyUnit + ")");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productionDate.setCellValueFactory(new PropertyValueFactory<>("productionDate"));
        discountsColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        expirationDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(productService.formatExpirationDate(cellData.getValue()))
        );
        unitColumn.setCellValueFactory(cellData ->{
            ProductUnit unit = cellData.getValue().getUnit();
            String text = (unit != null) ? unit.getFullName() : "";
            return new SimpleStringProperty(text);
        });
    }

    @Override
    protected Collection<Product> fetchData(){
        return productService.getFilteredProductsByCategory(currentCategory);
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<Product>> menuItemsFor(Product p){
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Edit Product", (item, row) -> handleEditAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Delete Product", (item, row) -> handleDeleteAction(item))
        );
    }

    @Override
    protected void postInit(){
        modalPane = ViewUtil.initializeModalPane(stackPane);
        setupEventHandlers();
        loadCategories();
        setupDynamicLayoutAdjustment();
    }

    private void setupEventHandlers() {
        addProductButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/product/AddProduct.fxml",
                (AddProductController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(this::refresh);
                }, modalPane));

        addCategoryButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/product/AddCategory.fxml",
                (AddCategoryController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(this::loadCategories);
                }, modalPane));

        editCategoriesButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/product/EditCategories.fxml",
                (EditCategoriesController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(this::loadCategories);
                }, modalPane));
    }

    private void handleDeleteAction(Product product) {
        AlertUtil.showDeleteConfirmation(product,
                "Delete Product",
                "Are you sure you want to delete this product?",
                "This action cannot be undone",
                (p) -> {
                    productService.deleteProduct(p.getId());
                    refresh();
                }
        );
    }

    private void handleEditAction(Product product) {
        ViewUtil.displayModalPaneView("/view/product/EditProduct.fxml",
                (EditProductController controller) -> {
                    controller.setProduct(product);
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() ->
                            onCategorySelected(product.getCategory()));
                }, modalPane);
    }

    private void onCategorySelected(Category category) {
        currentCategory = category.getName();
        categoryMenuButton.setText(currentCategory);
        refresh();
    }

    private void setupDynamicLayoutAdjustment() {
        ViewUtil.setupDynamicLayoutAdjustment(
                categoryMenuButton,
                Arrays.asList(addCategoryButton, editCategoriesButton, addProductButton, infoLabel),
                Arrays.asList(10.0, 130.0, 260.0, 390.0)
        );
    }
}