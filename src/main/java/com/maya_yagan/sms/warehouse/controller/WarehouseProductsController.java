package com.maya_yagan.sms.warehouse.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.settings.service.SettingsService;
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
import javafx.scene.layout.GridPane;
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
    @FXML private Label label1;
    @FXML private StackPane stackPane;

    private String currentCategory = "All Categories";
    private ModalPane modalPane;
    private Warehouse warehouse;

    private final ProductService productService = new ProductService();
    private final WarehouseService warehouseService = new WarehouseService();
    private final SettingsService settingsService = new SettingsService();
    private final String moneyUnit = settingsService.getSettings().getMoneyUnit();

    @Override
    protected void configureColumns(){
        idColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getProduct().getId())
        );
        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getProduct().getName())
        );
        priceColumn.setText("Price (" + moneyUnit + ")");
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
                new ContextMenuUtil.MenuItemConfig<>("Update Amount",
                        (item, row) -> handleAmountUpdateAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Transfer Product",
                        (item, row) -> handleTransferAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Delete Product",
                        (item, row) -> handleDeleteAction(item))
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
                        refresh();
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

    private void handleTransferAction(ProductWarehouse pw){
        // --- UI -----------------------------------------------------------------
        TextField amountField = new TextField(String.valueOf(pw.getAmount()));
        amountField.setPromptText("Amount");

        MenuButton inventoryMenu = new MenuButton("Choose inventory");
        var allWarehouses = warehouseService.getAllWarehouses()
                .stream()
                .filter(w -> w.getId() != warehouse.getId())   // exclude current
                .toList();

        // keep a reference to the chosen warehouse
        final Warehouse[] chosenTarget = new Warehouse[1];

        for (Warehouse w : allWarehouses) {
            MenuItem mi = new MenuItem(w.getName());
            mi.setOnAction(e -> {
                inventoryMenu.setText(w.getName());
                chosenTarget[0] = w;
            });
            inventoryMenu.getItems().add(mi);
        }

        GridPane content = new GridPane();
        content.setHgap(15);
        content.setVgap(10);
        content.addRow(0, new Label("Amount:"), amountField);
        content.addRow(1, new Label("Destination:"), inventoryMenu);

        // --- Dialog -----------------------------------------------------------------
        Optional<ButtonType> result =
                ViewUtil.showCustomDialog("Transfer Product",
                        "Transfer " + pw.getProduct().getName(),
                        content);

        if (result.isPresent() && result.get() == ButtonType.OK){
            try {
                int amt = Integer.parseInt(amountField.getText().trim());
                Warehouse dest = chosenTarget[0];

                if (dest == null)
                    throw new CustomException("Please select a destination warehouse", "NOT_FOUND");

                warehouseService.transferProduct(warehouse, pw, amt, dest);

                refresh();
                AlertUtil.showAlert(Alert.AlertType.INFORMATION,
                        "Success",
                        "Product transferred successfully.");

            } catch (NumberFormatException ex){
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid amount",
                        "Please enter a valid integer.");
            } catch (CustomException ex){
                ExceptionHandler.handleException(ex);
            }
        }
    }


    private void setupDynamicLayoutAdjustment(){
        ViewUtil.setupDynamicLayoutAdjustment(
                categoryMenuButton,
                Arrays.asList(orderButton, label1),
                Arrays.asList(10.0, 140.0)
        );
    }

    public void setWarehouse(Warehouse warehouse){
        this.warehouse = warehouse;
        warehouseName.setText(warehouse.getName());
        refresh();
    }
}