package com.maya_yagan.sms.order.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.order.service.OrderService;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.supplier.service.SupplierService;
import com.maya_yagan.sms.util.*;

import java.util.*;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Maya Yagan
 */
public class AddOrderController extends AbstractTableController<SupplierProduct> {

    @FXML private TableColumn<SupplierProduct, Boolean> addToOrderColumn;
    @FXML private TableColumn<SupplierProduct, String> supplierColumn, productColumn;
    @FXML private TableColumn<SupplierProduct, Double> priceColumn;
    @FXML private Button ordersButton, saveOrderButton;
    @FXML private Label totalPrice;
    @FXML private MenuButton categoryMenuButton, supplierMenuButton;
    @FXML private StackPane stackPane;

    private final OrderService orderService = new OrderService();
    private final SupplierService supplierService = new SupplierService();
    private final ProductService productService = new ProductService();

    private final Map<SupplierProduct, Integer> productsAmount = new HashMap<>();
    private ModalPane modalPane;
    private Category currentCategory;
    private Supplier selectedSupplier;


    private void loadCategories() {
        MenuButtonUtil.populateMenuButton(
                categoryMenuButton,
                productService::getAllCategories,
                Category::getName,
                this::onCategorySelected,
                "All Categories",
                () -> {
                    currentCategory = null;
                    categoryMenuButton.setText("All Categories");
                    refresh();
                }
        );
    }

    private void loadSuppliers(){
        MenuButtonUtil.populateMenuButton(
                supplierMenuButton,
                supplierService::getAllSuppliers,
                Supplier::getName,
                this::onSupplierSelected,
                "Select Supplier",
                () -> {
                    selectedSupplier = null;
                    supplierMenuButton.setText("Select Supplier");
                    refresh();
                }
        );
    }

    @Override
    protected void configureColumns() {
        supplierColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSupplier().getName())
        );

        productColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getName())
        );

        priceColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject()
        );

        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("%.2f", price));
            }
        });

        TableViewUtil.setupCheckboxColumn(
                addToOrderColumn,
                productsAmount,
                (sp , selected) -> {
                    if(selected)
                        promptForAmountAndSelect(sp);
                    else {
                        productsAmount.remove(sp);
                        refresh();
                        updateTotals();
                    }
                }
        );
    }

    @Override
    protected Collection<SupplierProduct> fetchData() {
        if (selectedSupplier == null)
            return List.of();

        if (currentCategory == null)
            return supplierService.getSupplierProductsByCategory(selectedSupplier, null);

        return supplierService
                .getSupplierProductsByCategoryAndSupplier(currentCategory.getName(), selectedSupplier);
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<SupplierProduct>> menuItemsFor(SupplierProduct item) {
        return List.of();
    }

    @Override
    protected void postInit(){
        tableView.setEditable(true);
        modalPane = ViewUtil.initializeModalPane(stackPane);
        loadSuppliers();
        loadCategories();
        setupEventHandlers();
        setupDynamicLayoutAdjustment();
    }

    private void setupEventHandlers(){
        ordersButton.setOnAction(event -> ViewUtil.displayModalPaneView(
                "/view/order/OrderManagement.fxml",
                (controller) -> {}, modalPane));
        saveOrderButton.setOnAction(event -> saveOrder());
    }

    private void onCategorySelected(Category category){
        currentCategory = category;
        categoryMenuButton.setText(category.getName());
        refresh();
    }

    private void onSupplierSelected(Supplier supplier){
        selectedSupplier = supplier;
        supplierMenuButton.setText(supplier.getName());
        refresh();
    }

    private void promptForAmountAndSelect(SupplierProduct sp){
        ViewUtil.showIntegerInputDialog(
                "Enter Product Amount",
                "Enter the amount for " + sp.getProduct().getName(),
                "Amount",
                1,
                "Amount",
                newAmt -> {
                    try{
                        productsAmount.put(sp, newAmt);
                        refresh();
                        updateTotals();
                    } catch(CustomException e){
                        ExceptionHandler.handleException(e);
                        tableView.refresh();
                    }
                }
        );
    }

    private void saveOrder(){
        if(productsAmount.isEmpty()){
            AlertUtil.showAlert(
                    Alert.AlertType.WARNING,
                    "No Products Selected",
                    "Please select at least one product to add to the order."
            );
            return;
        }

        Optional<ButtonType> result =
                ViewUtil.showOrderSummaryDialog(
                        "Confirm Order",
                        "Please review the order details below and click OK to save the order.",
                        new ArrayList<>(productsAmount.keySet()),
                        productsAmount
                );

        if(result.isPresent() && result.get() == ButtonType.OK){
            try {
                orderService.saveOrder(productsAmount, selectedSupplier);
                AlertUtil.showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "The products were added to the order successfully.");
                productsAmount.clear();
                tableView.refresh();
                updateTotals();
            } catch (CustomException e){
                ExceptionHandler.handleException(e);
            }
        }
    }

    private void updateTotals(){
        float totalPriceValue = orderService.calculateTotalPrice(productsAmount);
        totalPrice.setText(String.format("%.2f", totalPriceValue));
    }
    
    private void setupDynamicLayoutAdjustment(){
        ViewUtil.setupDynamicLayoutAdjustment(categoryMenuButton, Collections.singletonList(supplierMenuButton), List.of(10.0));
    }
}

