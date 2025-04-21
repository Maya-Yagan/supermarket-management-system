package com.maya_yagan.sms.warehouse.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.util.*;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.warehouse.model.Warehouse;

import java.util.*;

import com.maya_yagan.sms.warehouse.service.WarehouseService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * Controller class for managing products within a warehouse.

 * @author Maya Yagan
 */
public class WarehouseProductsController extends AbstractTableController<ProductWarehouse> {
    
    @FXML private Text warehouseName;
    @FXML private TableColumn<ProductWarehouse, Integer> idColumn, amountColumn;
    @FXML private TableColumn<ProductWarehouse, String> nameColumn, productionDate, expirationDateColumn, unitColumn;
    @FXML private TableColumn<ProductWarehouse, Float> priceColumn;
    @FXML private Button backButton, orderButton;
    @FXML private MenuButton categoryMenuButton;
    @FXML private Label label1, label2;
    @FXML private StackPane stackPane;

    private String currentCategory = "All Categories";
    private ModalPane modalPane;
    private Warehouse warehouse;
    private final ProductService productService = new ProductService();
    private final WarehouseService warehouseService = new WarehouseService();

    @Override
    protected void configureColumns(){
        idColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getProduct().getId())
        );
        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getProduct().getName())
        );
        priceColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getProduct().getPrice())
        );
        productionDate.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getProduct().getProductionDate().toString())
        );
        expirationDateColumn.setCellValueFactory(cell -> {
            var exp = cell.getValue().getProduct().getExpirationDate();
            return new SimpleStringProperty(exp == null ? "No Expiry Date" : exp.toString());
        });
        unitColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getProduct().getUnit().getShortName())
        );
        amountColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getAmount())
        );
    }

    @Override
    protected Collection<ProductWarehouse> fetchData(){
        if(warehouse == null) return Collections.emptyList();
        return warehouseService.getProductWarehousesByCategory(warehouse, currentCategory);
    }

    @Override
    protected void postInit(){
        modalPane = ViewUtil.initializeModalPane(stackPane);
        setupDynamicLayoutAdjustment();
        setupEventHandlers();
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<ProductWarehouse>> menuItemsFor(ProductWarehouse pw){
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Delete Product",
                        (item, row) -> handleDeleteAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Update Amount",
                        (item, row) -> handleAmountUpdateAction(item))
        );
    }

    private void setupEventHandlers(){
        orderButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/order/AddOrder.fxml",
                controller -> {}, modalPane));

        backButton.setOnAction(event -> ViewUtil.switchView("/view/warehouse/WarehouseManagement.fxml",
                controller -> {}, stackPane, "Warehouse Management"));

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

    private void onCategorySelected(Category category){
        currentCategory = category.getName();
        categoryMenuButton.setText(currentCategory);
        refresh();
    }

    private void handleAmountUpdateAction(ProductWarehouse pw) {
        ViewUtil.showIntegerInputDialog(
                "Update Stock",
                "Enter new stock amount for " + pw.getProduct().getName(),
                "Stock (" + pw.getProduct().getUnit().getShortName() + ")",
                pw.getAmount(),
                "Stock",
                newAmt -> {
                    try {
                        warehouseService.updateProductStock(warehouse, pw, newAmt);
                        refresh();
                        AlertUtil.showAlert(
                                Alert.AlertType.INFORMATION,
                                "Success",
                                "The product amount was successfully updated.");
                    } catch (CustomException ex) {
                        ExceptionHandler.handleException(ex);
                        tableView.refresh();
                    }
                }
        );
    }

    private void handleDeleteAction(ProductWarehouse product) {
        AlertUtil.showDeleteConfirmation(product,
                "Delete Product from Warehouse",
                "Are you sure you want to delete this product from the warehouse?",
                "This action cannot be undone",
                (p) -> {
                    warehouseService.deleteProductFromWarehouse(p.getWarehouse(), p.getProduct());
                    refresh();
                }
        );
    }

    private void setupDynamicLayoutAdjustment(){
        ViewUtil.setupDynamicLayoutAdjustment(
                categoryMenuButton,
                Arrays.asList(orderButton, label1, label2),
                Arrays.asList(10.0, 140.0, 140.0)
        );
    }

    public void setWarehouse(Warehouse warehouse){
        this.warehouse = warehouse;
        warehouseName.setText(warehouse.getName());
        refresh();
    }
}