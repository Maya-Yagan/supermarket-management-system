package com.maya_yagan.sms.payment.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.controls.Notification;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import com.google.zxing.WriterException;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.payment.model.PaymentMethod;
import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.service.PaymentService;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.settings.model.Settings;
import com.maya_yagan.sms.settings.service.SettingsService;
import com.maya_yagan.sms.util.*;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.warehouse.service.WarehouseService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.controlsfx.control.SearchableComboBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class PaymentPageController extends AbstractTableController<ProductWarehouse> {

    @FXML private TableColumn<ProductWarehouse, Boolean> addColumn;
    @FXML private TableColumn<ProductWarehouse, Float> discountColumn;
    @FXML private TableColumn<ProductWarehouse, Float> discountedPriceColumn;
    @FXML private TableColumn<ProductWarehouse, Float> unitPriceColumn;
    @FXML private TableColumn<ProductWarehouse, String> productColumn;

    @FXML private StackPane stackPane;
    @FXML private SearchableComboBox<String> nameComboBox;
    @FXML private SearchableComboBox<Category> categoryComboBox;
    @FXML private TextField barcodeField;
    @FXML private GridPane gridPane;
    @FXML private ImageView barcodeImageView;
    @FXML private Button  creditButton, cashButton, addButton, printButton;
    @FXML private Text addressText;
    @FXML private MenuButton inventoryMenuButton;
    @FXML private Label dateLabel, changeLabel, amountPaidLabel, employeeNameLabel,
                receiptNumberLabel, taxLabel, subtotalLabel, totalCostLabel, marketNameLabel, phoneLabel;

    private static final String STYLE = "-fx-font-weight: bold;";
    private static final String ALL_CATEGORIES = "All Categories";

    private final WarehouseService warehouseService = new WarehouseService();
    private final SettingsService settingsService = new SettingsService();
    private final ProductService productService = new ProductService();
    private final PaymentService paymentService = new PaymentService();
    private final Settings settings = settingsService.getSettings();
    private final Map<ProductWarehouse, Double> basket = new HashMap<>();
    private final String moneyUnit = settingsService.getSettings().getMoneyUnit();

    private boolean selectionHandled = false;
    private String currentCategory = ALL_CATEGORIES;
    private String currentWarehouse = "";
    private Warehouse selectedWarehouse;
    private ModalPane modalPane;

    private void loadInventories(){
        MenuButtonUtil.populateMenuButton(
                inventoryMenuButton,
                () -> new HashSet<>(warehouseService.getAllWarehouses()),
                Warehouse::getName,
                this::onInventorySelected,
                "Select",
                () -> {
                    currentWarehouse = "All Inventories";
                    inventoryMenuButton.setText(currentWarehouse);
                    refresh();
                }
        );
    }

    private void loadCategories() {
        List<Category> categories = new ArrayList<>(productService.getAllCategories());
        Category allCategories = new Category();
        allCategories.setName(ALL_CATEGORIES);
        categories.add(0, allCategories);

        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
        categoryComboBox.setValue(allCategories);
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || ALL_CATEGORIES.equals(newVal.getName()))
                currentCategory = ALL_CATEGORIES;
            else
                currentCategory = newVal.getName();
            refresh();
        });
    }

    private void loadProductNames() {
        nameComboBox.setItems(FXCollections.observableArrayList(productService.getProductNames()));
        nameComboBox.setEditable(true);

        nameComboBox.getEditor().addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ENTER){
                if(!selectionHandled){
                    selectionHandled = true;
                    commitSelection();
                }
                event.consume();
            }
            else if(event.getCode() == KeyCode.ESCAPE){
                clearEditor();
                event.consume();
            }
        });
        nameComboBox.setOnHidden(event -> {
            if(!selectionHandled){
                selectionHandled = true;
                commitSelection();
            }
            Platform.runLater(() -> selectionHandled = false);
        });
    }

    @Override
    protected void configureColumns() {
        productColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getName())
        );
        unitPriceColumn.setText("Unit Price (" +  moneyUnit + ")");
        unitPriceColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getProduct().getPrice())
        );
        discountColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getProduct().getDiscount())
        );
        discountedPriceColumn.setText("Discounted Price (" + moneyUnit + ")");
        discountedPriceColumn.setCellValueFactory(cellData -> {
            var productWarehouse = cellData.getValue();
            float discountedPrice = paymentService.calculateDiscountedPrice(productWarehouse);
            return new SimpleFloatProperty(discountedPrice).asObject();
        });

        discountedPriceColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Float value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", value));
                }
            }
        });

        TableViewUtil.setupCheckboxColumn(
                addColumn,
                basket,
                (productWarehouse, selected) -> {
                    if(selected)
                        promptForAmountAndSelect(productWarehouse);
                    else
                        onProductDeselected(productWarehouse);
                }
        );
    }

    @Override
    protected Collection<ProductWarehouse> fetchData() {
        if(selectedWarehouse == null) return Collections.emptyList();
        return warehouseService.getProductWarehousesByCategory(selectedWarehouse, currentCategory);
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<ProductWarehouse>> menuItemsFor(ProductWarehouse productWarehouse) {
        return List.of();
    }

    @Override
    protected void postInit(){
        tableView.setEditable(true);
        loadInventories();
        loadCategories();
        loadProductNames();
        setupEventHandlers();
        setStaticHeaderFields();
        modalPane = ViewUtil.initializeModalPane(stackPane);
    }

    private void setupEventHandlers(){
        barcodeField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                handleBarcodeEntry();
                event.consume();
            }
        });
        //printButton.setOnAction(event -> printCurrentReceipt());
        addButton.setOnAction(event -> handleBarcodeEntry());
        cashButton.setOnAction(event -> {
            try {
                Receipt receipt = paymentService.createReceipt(
                        basket,
                        receiptNumberLabel.getText(),
                        PaymentMethod.CASH
                );

                ViewUtil.showDialog(
                        "/view/payment/CashPayment.fxml",
                        "Cash Payment",
                        (CashPaymentController controller) -> {
                            controller.setCurrentInventory(selectedWarehouse);
                            controller.setReceipt(receipt);
                            controller.setModalPane(modalPane);
                            controller.setOnCloseAction(() -> {
                                basket.clear();
                                refresh();
                                refreshPaymentSection();
                                setStaticHeaderFields();
                                showPaymentNotification(stackPane);
                            });
                        },
                        CashPaymentController::save
                );
            } catch (IOException e) {
                throw new CustomException("Error loading view: " + e.getMessage(), "IO_ERROR");
            }
        });
    }

    private void promptForAmountAndSelect(ProductWarehouse productWarehouse){
        ViewUtil.showFloatInputDialog(
                "Enter Product Amount",
                "Enter the amount for " + productWarehouse.getProduct().getName(),
                "Amount",
                1f,
                "Amount",
                newAmt -> {
                    try {
                        basket.put(productWarehouse, newAmt);
                        refresh();
                        refreshPaymentSection();
                    } catch(CustomException e){
                        ExceptionHandler.handleException(e);
                    }
                }
        );
    }

    private void commitSelection(){
        String key = Optional.ofNullable(nameComboBox.getValue())
                .orElse(nameComboBox.getEditor().getText())
                .trim();
        if (key.isEmpty()) return;

        if (selectedWarehouse == null){
            AlertUtil.showAlert(Alert.AlertType.WARNING,
                    "Select Inventory",
                    "Please choose an inventory/shelf first.");
            return;
        }

        warehouseService.findProductWarehouseByName(selectedWarehouse, key)
                .ifPresentOrElse(pw -> {
                    promptForAmountAndSelect(pw);
                    Platform.runLater(this::clearEditor);
                }, () -> AlertUtil.showAlert(Alert.AlertType.ERROR,
                        "Product Not Found",
                        "There is no product named \""+key+"\" in the selected inventory."));
    }

    private void clearEditor() {
        nameComboBox.getSelectionModel().clearSelection();
        nameComboBox.getEditor().clear();
        nameComboBox.setValue(null);
    }

    private void showBarcode(String data){
        try{
            Image img = BarcodeUtil.code128(data);
            barcodeImageView.setImage(img);
        } catch (WriterException e){
            e.printStackTrace();
        }
    }

    private void handleBarcodeEntry(){
        String barcode = barcodeField.getText().trim();
        if (barcode.isEmpty()) return;

        if (selectedWarehouse == null){
            AlertUtil.showAlert(Alert.AlertType.WARNING,
                    "Select Inventory",
                    "Please choose an inventory/shelf first.");
            return;
        }

        warehouseService.findProductWarehouseByBarcode(selectedWarehouse, barcode)
                .ifPresentOrElse(pw -> {
                    promptForAmountAndSelect(pw);
                    Platform.runLater(() -> barcodeField.clear());
                }, () -> AlertUtil.showAlert(Alert.AlertType.ERROR,
                        "Product Not Found",
                        "No product with that barcode in the selected inventory."));
    }

    private void setStaticHeaderFields() {
        String receiptNumber = paymentService.generateReceiptNumber();
        String barcodeData   = paymentService.generateSimpleBarcodeData(receiptNumber);
        String marketName    = settings.getMarketName();
        String address       = settings.getAddress();
        String phone         = settings.getPhone();

        dateLabel.setText(DateUtil.formatDateTime(LocalDateTime.now()));
        employeeNameLabel.setText(paymentService.getCurrentEmployeeName());
        receiptNumberLabel.setText(receiptNumber);
        marketNameLabel.setText(marketName);
        addressText.setText(address);
        phoneLabel.setText(phone);

        showBarcode(barcodeData);
    }

    private void onInventorySelected(Warehouse warehouse){
        selectedWarehouse = warehouse;
        refresh();
    }

    private void onProductDeselected(ProductWarehouse product) {
        basket.remove(product);
        refreshPaymentSection();
    }

    private void refreshPaymentSection() {
        populateGridPane();
        float subtotal = paymentService.calculateSubtotal(basket);
        float tax = paymentService.calculateTotalTax(basket);
        float totalCost = paymentService.calculateTotalCost(basket);

        subtotalLabel.setText(String.format("%.2f", subtotal) + " "+ moneyUnit);
        taxLabel.setText(String.format("%.2f", tax) + " "+ moneyUnit);
        totalCostLabel.setText(String.format("%.2f", totalCost) + " "+ moneyUnit);
    }

    private void populateGridPane() {
        gridPane.getChildren().clear();

        Label productHeader = createWrappedLabel("Product");
        Label amountHeader = createWrappedLabel("Amount");
        Label unitPriceHeader = createWrappedLabel("Unit Price");
        Label totalHeader = createWrappedLabel("Total Price");
        Label taxHeader = createWrappedLabel("Tax");

        productHeader.setStyle(STYLE);
        amountHeader.setStyle(STYLE);
        unitPriceHeader.setStyle(STYLE);
        totalHeader.setStyle(STYLE);
        taxHeader.setStyle(STYLE);

        gridPane.add(productHeader,    0, 0);
        gridPane.add(amountHeader,     1, 0);
        gridPane.add(unitPriceHeader,  2, 0);
        gridPane.add(totalHeader,      3, 0);
        gridPane.add(taxHeader, 4, 0);

        int row = 1;
        for (var entry : basket.entrySet()) {
            ProductWarehouse pw = entry.getKey();
            double amount = entry.getValue();
            float unitPrice = paymentService.calculateDiscountedPrice(pw);
            float taxRate = pw.getProduct().getTaxPercentage();
            double total = amount * unitPrice;
            String amountWithUnit = String.format("%.2f %s", amount, pw.getProduct().getUnit().getShortName());

            gridPane.add(createWrappedLabel(pw.getProduct().getName()), 0, row);
            gridPane.add(createWrappedLabel(amountWithUnit), 1, row);
            gridPane.add(createWrappedLabel(String.format("%.2f", unitPrice)), 2, row);
            gridPane.add(createWrappedLabel(String.format("%.2f", total)), 3, row);
            gridPane.add(createWrappedLabel(String.format("%.2f", taxRate)), 4, row);
            row++;
        }
    }

    private Label createWrappedLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(75);
        label.setStyle("-fx-padding: 4;");
        return label;
    }

    private static void showPaymentNotification(StackPane root) {

        var msg = new Notification(
                "Payment completed successfully",
                new FontIcon(Feather.CHECK_CIRCLE)
        );
        msg.getStyleClass().addAll(Styles.SUCCESS, Styles.ELEVATED_1);
        msg.setPrefHeight(Region.USE_PREF_SIZE);
        msg.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(msg, Pos.TOP_RIGHT);
        StackPane.setMargin(msg, new Insets(10, 10, 0, 0));

        // ---------- add it to the scene (if not already present) ----------
        if (!root.getChildren().contains(msg)) {
            root.getChildren().add(msg);
        }

        // ---------- slide-in animation ----------
        var slideIn = Animations.slideInDown(msg, Duration.millis(250));
        slideIn.play();

        // ---------- let it stay up for 3 s, then slide out ----------
        slideIn.setOnFinished(ev -> {
            var wait = new PauseTransition(Duration.seconds(3));
            wait.setOnFinished(e -> {
                var slideOut = Animations.slideOutUp(msg, Duration.millis(250));
                slideOut.setOnFinished(f -> root.getChildren().remove(msg));
                slideOut.play();
            });
            wait.play();
        });

        // Manual close
        msg.setOnClose(e -> {
            var slideOut = Animations.slideOutUp(msg, Duration.millis(250));
            slideOut.setOnFinished(f -> root.getChildren().remove(msg));
            slideOut.playFromStart();
        });
    }

}