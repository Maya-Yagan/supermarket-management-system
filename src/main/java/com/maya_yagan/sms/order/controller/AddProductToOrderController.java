package com.maya_yagan.sms.order.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.order.service.OrderService;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.supplier.service.SupplierService;
import com.maya_yagan.sms.util.*;

import java.util.*;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;

/**
 * Controller class for adding additional products to an order.
 *
 * @author Maya Yagan
 */
public class AddProductToOrderController extends AbstractTableController<SupplierProduct> {

    @FXML private TableColumn<SupplierProduct, Boolean> addToOrderColumn;
    @FXML private TableColumn<SupplierProduct, String> productColumn;
    @FXML private TableColumn<SupplierProduct, Float> priceColumn;
    @FXML private MenuButton categoryMenuButton;
    
    private ModalPane modalPane;
    private Runnable onCloseAction;
    private Order order;
    private String currentCategory = "All Categories";
    private Supplier selectedSupplier;

    private final SupplierService supplierService = new SupplierService();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final Map<SupplierProduct, Integer> productsAmount = new HashMap<>();

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
        productColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getName()));

        priceColumn.setCellValueFactory(cellData ->
                new SimpleFloatProperty(cellData.getValue().getPrice()).asObject());
        TableViewUtil.setupCheckboxColumn(
                addToOrderColumn,
                productsAmount,
                (product, selected) ->{
                    if(selected)
                        promptForAmountAndSelect(product);
                    else
                        productsAmount.remove(product);
                }
        );
    }

    @Override
    protected Collection<SupplierProduct> fetchData() {
        return supplierService.getSupplierProductsByCategoryAndSupplier(currentCategory, selectedSupplier);
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<SupplierProduct>> menuItemsFor(SupplierProduct item) {
        return List.of();
    }

    @Override
    protected void postInit(){
        loadCategories();
        tableView.setEditable(true);
        refresh();
    }

    private void onCategorySelected(Category category){
        currentCategory = category.getName();
        categoryMenuButton.setText(category.getName());
        refresh();
    }

    private void promptForAmountAndSelect(SupplierProduct sp){
        ViewUtil.showFloatInputDialog(
                "Enter the Amount",
                "Enter the amount for " + sp.getProduct().getName(),
                "Amount(" + sp.getProduct().getUnit().getShortName() + ")",
                0f,
                "Amount",
                newAmt -> {
                    try{
                        productsAmount.put(sp, newAmt.intValue());
                        refresh();
                    } catch (CustomException e){
                        ExceptionHandler.handleException(e);
                        tableView.refresh();
                    }
                }
        );
    }

    public void saveProducts(){
        if (productsAmount.isEmpty()) {
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
                "Please review the order details below and click OK to save the order",
                new ArrayList<>(productsAmount.keySet()),
                productsAmount);

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                orderService.addProductsToOrder(order, productsAmount);
                AlertUtil.showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "The products were added to the order successfully.");
                if (onCloseAction != null) onCloseAction.run();
                close();
            } catch (CustomException e) {
                ExceptionHandler.handleException(e);
            }
        }
    }

    private void close(){
        if(modalPane != null) modalPane.hide();
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setSupplier(Supplier supplier){
        this.selectedSupplier = supplier;
        refresh();
    }

    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
}
