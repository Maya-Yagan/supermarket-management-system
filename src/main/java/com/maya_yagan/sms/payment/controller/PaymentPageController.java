package com.maya_yagan.sms.payment.controller;

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
import javafx.application.Platform;
import javafx.beans.property.SimpleFloatProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.controlsfx.control.SearchableComboBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class PaymentPageController extends AbstractTableController<Product> {

    @FXML private TableColumn<Product, Boolean> addColumn;
    @FXML private TableColumn<Product, Float> discountColumn;
    @FXML private TableColumn<Product, Float> discountedPriceColumn;
    @FXML private TableColumn<Product, Float> unitPriceColumn;
    @FXML private TableColumn<Product, String> productColumn;

    @FXML private SearchableComboBox<String> nameComboBox;
    @FXML private SearchableComboBox<Category> categoryComboBox;
    @FXML private TextField barcodeField;

    @FXML private GridPane gridPane;
    @FXML private ImageView barcodeImageView;
    @FXML private Button returnButton, creditButton, cashButton, addButton;
    @FXML private Text addressText;
    @FXML private Label dateLabel, changeLabel, amountPaidLabel, employeeNameLabel,
                receiptNumberLabel, taxLabel, subtotalLabel, totalCostLabel, marketNameLabel, phoneLabel;


    private static final String STYLE = "-fx-font-weight: bold;";
    private static final String ALL_CATEGORIES = "All Categories";

    private final SettingsService settingsService = new SettingsService();
    private final ProductService productService = new ProductService();
    private final PaymentService paymentService = new PaymentService();
    private final Settings settings = settingsService.getSettings();
    private final Map<Product, Double> basket = new HashMap<>();

    private boolean selectionHandled = false;
    private String currentCategory = ALL_CATEGORIES;

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
        productColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        discountedPriceColumn.setCellValueFactory(cellData -> {
            var product = cellData.getValue();
            float discountedPrice = paymentService.calculateDiscountedPrice(product);
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
                (product, selected) -> {
                    if(selected)
                        promptForAmountAndSelect(product);
                    else
                        onProductDeselected(product);
                }
        );
    }

    @Override
    protected Collection<Product> fetchData() {
        return productService.getFilteredProductsByCategory(currentCategory);
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<Product>> menuItemsFor(Product product) {
        return List.of();
    }

    @Override
    protected void postInit(){
        tableView.setEditable(true);
        loadCategories();
        loadProductNames();
        setupEventHandlers();
        setStaticHeaderFields();
    }

    private void setupEventHandlers(){
        barcodeField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                handleBarcodeEntry();
                event.consume();
            }
        });
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
                        (CashPaymentController controller) -> controller.setReceipt(receipt),
                        CashPaymentController::save
                );
            } catch (IOException e) {
                throw new CustomException("Error loading view: " + e.getMessage(), "IO_ERROR");
            }
        });
    }

    private void promptForAmountAndSelect(Product product){
        ViewUtil.showFloatInputDialog(
                "Enter Product Amount",
                "Enter the amount for " + product.getName(),
                "Amount",
                1f,
                "Amount",
                newAmt -> {
                    try {
                        basket.put(product, newAmt);
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
        if (key.isEmpty()) {
            return;
        }

        productService.findProductByName(key)
                .ifPresentOrElse(p -> {
                    promptForAmountAndSelect(p);
                    Platform.runLater(this::clearEditor);
                }, () -> AlertUtil.showAlert(Alert.AlertType.ERROR,
                        "Product Not Found",
                        "There is no product named \"" + key + "\"."));
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
        if(barcode.isEmpty()) return;
        productService.findProductByBarcode(barcode).ifPresentOrElse(product -> {
            promptForAmountAndSelect(product);
            Platform.runLater(() -> barcodeField.clear());
        },() -> AlertUtil.showAlert(Alert.AlertType.ERROR,
                        "Product Not Found",
                        "No product found with the given barcode"));
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

    private void onProductDeselected(Product p) {
        basket.remove(p);
        refreshPaymentSection();
    }

    private void refreshPaymentSection() {
        populateGridPane();
        float subtotal = paymentService.calculateSubtotal(basket);
        float tax = paymentService.calculateTotalTax(basket);
        float totalCost = paymentService.calculateTotalCost(basket);

        subtotalLabel.setText(String.format("%.2f", subtotal) + " "+ settings.getMoneyUnit());
        taxLabel.setText(String.format("%.2f", tax) + " "+ settings.getMoneyUnit());
        totalCostLabel.setText(String.format("%.2f", totalCost) + " "+ settings.getMoneyUnit());
    }

    private void populateGridPane() {
        gridPane.getChildren().clear();

        Label productHeader = new Label("Product");
        productHeader.setStyle(STYLE);

        Label amountHeader = new Label("Amount");
        amountHeader.setStyle(STYLE);

        Label unitPriceHeader = new Label("Unit Price");
        unitPriceHeader.setStyle(STYLE);

        Label totalHeader = new Label("Total Price");
        totalHeader.setStyle(STYLE);

        Label taxHeader = new Label("Tax");
        taxHeader.setStyle(STYLE);

        gridPane.add(productHeader,    0, 0);
        gridPane.add(amountHeader,     1, 0);
        gridPane.add(unitPriceHeader,  2, 0);
        gridPane.add(totalHeader,      3, 0);
        gridPane.add(taxHeader, 4, 0);

        int row = 1;
        for (var entry : basket.entrySet()) {
            Product p = entry.getKey();
            double amount = entry.getValue();
            float unitPrice = paymentService.calculateDiscountedPrice(p);
            float taxRate = p.getTaxPercentage();
            double total = amount * unitPrice;
            String amountWithUnit = String.format("%.2f %s", amount, p.getUnit().getShortName());

            gridPane.add(new Label(p.getName()),0, row);
            gridPane.add(new Label(amountWithUnit),  1, row);
            gridPane.add(new Label(String.format("%.2f", unitPrice)), 2, row);
            gridPane.add(new Label(String.format("%.2f", total)),   3, row);
            gridPane.add(new Label(String.format("%.2f", taxRate)), 4, row);
            row++;
        }
    }
}