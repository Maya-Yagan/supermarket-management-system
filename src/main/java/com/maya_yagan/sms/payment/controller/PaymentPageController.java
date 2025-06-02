package com.maya_yagan.sms.payment.controller;

import atlantafx.base.controls.ModalPane;
import com.google.zxing.WriterException;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.common.ValidationService;
import com.maya_yagan.sms.payment.creditcard.CreditCardPaymentController;
import com.maya_yagan.sms.payment.model.PaymentMethod;
import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.service.PaymentService;
import com.maya_yagan.sms.payment.service.RefundService;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.settings.model.Settings;
import com.maya_yagan.sms.settings.service.SettingsService;
import com.maya_yagan.sms.user.service.UserService;
import com.maya_yagan.sms.util.*;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.warehouse.service.WarehouseService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.controlsfx.control.SearchableComboBox;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PaymentPageController extends AbstractTableController<ProductWarehouse> {

    @FXML private TableColumn<ProductWarehouse, Boolean> addColumn;
    @FXML private TableColumn<ProductWarehouse, Float> discountColumn;
    @FXML private TableColumn<ProductWarehouse, Float> discountedPriceColumn;
    @FXML private TableColumn<ProductWarehouse, Float> unitPriceColumn;
    @FXML private TableColumn<ProductWarehouse, String> productColumn;

    @FXML private ScrollPane scrollPane;
    @FXML private StackPane stackPane;
    @FXML private SearchableComboBox<String> nameComboBox;
    @FXML private SearchableComboBox<Category> categoryComboBox;
    @FXML private TextField barcodeField;
    @FXML private GridPane gridPane;
    @FXML private ImageView barcodeImageView;
    @FXML private Button  creditButton, cashButton, addButton, returnButton;
    @FXML private Text addressText;
    @FXML private MenuButton inventoryMenuButton;
    @FXML private Label dateLabel, employeeNameLabel, receiptNumberLabel, taxLabel,
                        subtotalLabel, totalCostLabel, marketNameLabel, phoneLabel,
                        paidAmountLabel, changeGivenLabel, paymentMethodLabel, receiptStatusLabel;

    private static final String STYLE = "-fx-font-weight: bold;";
    private static final String ALL_CATEGORIES = "All Categories";

    private final UserService userService = new UserService();
    private final ValidationService validationService = new ValidationService();
    private final RefundService refundService = new RefundService();
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
    private Receipt currentReceipt;

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
            BigDecimal discountedPrice = paymentService.calculateDiscountedPrice(productWarehouse);
            return new SimpleObjectProperty<>(discountedPrice.floatValue());
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
        addButton.setOnAction(event -> handleBarcodeEntry());

        returnButton.setOnAction(event ->
                ViewUtil.showStringInputDialog(
                "Enter Receipt Code",
                "Product Refund",
                "Please enter the receipt code",
                "RC-XXXXXXXX",
                "Receipt Code",
                code -> {
                    Receipt receipt = refundService.getReceiptByCode(code);
                    validationService.validateReceiptCode(receipt, code);
                        ViewUtil.displayModalPaneView(
                        "/view/payment/RefundPage.fxml",
                        (RefundPageController controller) -> {
                            controller.setReceipt(receipt);
                            controller.setModalPane(modalPane);
                        }, modalPane);
                    }
                )
        );

        barcodeField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                handleBarcodeEntry();
                event.consume();
            }
        });

        creditButton.setOnAction(event -> {
            if(basket.isEmpty()){
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Empty basket", "Please add items first.");
                return;
            }
            try {
                Receipt receipt = paymentService.createReceipt(
                        basket,
                        receiptNumberLabel.getText(),
                        PaymentMethod.CARD
                );
                currentReceipt = receipt;
                updateReceiptLabels(currentReceipt, false);

                ViewUtil.displayModalPaneView(
                        "/view/payment/CreditCardPayment.fxml",
                        (CreditCardPaymentController controller) -> {
                            controller.setReceipt(receipt);
                            controller.setWarehouse(selectedWarehouse);
                            controller.setPaymentResultListener(() ->
                                Platform.runLater(() -> {
                                    modalPane.hide();
                                    updateReceiptLabels(currentReceipt, true);
                                    printReceipt();
                                    refreshAfterPayment();
                                }
                            ));
                        }, modalPane);
            } catch (CustomException e) {
                ExceptionHandler.handleException(e);
            }
        });

        cashButton.setOnAction(event -> {
            if(basket.isEmpty()){
                AlertUtil.showAlert(Alert.AlertType.WARNING, "Empty basket", "Please add items first.");
                return;
            }
            try {
                Receipt receipt = paymentService.createReceipt(
                        basket,
                        receiptNumberLabel.getText(),
                        PaymentMethod.CASH
                );
                currentReceipt = receipt;
                updateReceiptLabels(currentReceipt, false);

                ViewUtil.showDialog(
                        "/view/payment/CashPayment.fxml",
                        "Cash Payment",
                        (CashPaymentController controller) -> {
                            controller.setCurrentInventory(selectedWarehouse);
                            controller.setReceipt(receipt);
                            controller.setModalPane(modalPane);
                            controller.setOnCloseAction(() -> {
                                updateReceiptLabels(currentReceipt, true);
                                printReceipt();
                                refreshAfterPayment();
                            });
                        },
                        CashPaymentController::save
                );
            } catch (IOException e) {
                throw new CustomException("Error loading view: " + e.getMessage(), "IO_ERROR");
            }
        });
    }

    private void printReceipt(){
        File receiptsDir = new File("receipts");
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs();  // creates folder(s) if missing
        }
        File file = new File(receiptsDir, "receipt_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");

        PdfExportUtil.exportReceiptToPdf(this, file);
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
        employeeNameLabel.setText(userService.getCurrentEmployeeName());
        receiptNumberLabel.setText(receiptNumber);
        marketNameLabel.setText(marketName);
        addressText.setText(address);
        phoneLabel.setText(phone);

        showBarcode(barcodeData);
    }

    private void updateReceiptLabels(Receipt r, boolean showAmounts){
        if(r == null){
            paidAmountLabel.setText("00.0");
            changeGivenLabel.setText("00.0");
            paymentMethodLabel.setText("UNKNOWN");
            receiptStatusLabel.setText("PENDING");
            return;
        }

        paymentMethodLabel.setText(r.getPaymentMethod().name());
        receiptStatusLabel.setText(r.getStatus().name());

        if (!showAmounts || r.getPaidAmount() == null) {
            paidAmountLabel.setText("");
            changeGivenLabel.setText("");
            return;
        }

        // --- payment has finished, we have the real numbers ------------------
        BigDecimal paid = r.getPaidAmount();               // never null now
        BigDecimal change =
                (r.getPaymentMethod() == PaymentMethod.CARD)
                        ? r.getTotalCost()                 // card rule
                        : Objects.requireNonNullElse(r.getChangeGiven(),
                        BigDecimal.ZERO);

        paidAmountLabel.setText(MoneyUtil.formatMoney(paid));
        changeGivenLabel.setText(MoneyUtil.formatMoney(change));
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
        BigDecimal subtotal = paymentService.calculateSubtotal(basket);
        BigDecimal tax = paymentService.calculateTotalTax(basket);
        BigDecimal totalCost = paymentService.calculateTotalCost(basket);

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
            BigDecimal unitPrice = paymentService.calculateDiscountedPrice(pw);
            float taxRate = pw.getProduct().getTaxPercentage();
            BigDecimal total =  unitPrice.multiply(BigDecimal.valueOf(amount));
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

    private void refreshAfterPayment(){
        basket.clear();
        refresh();
        refreshPaymentSection();
        setStaticHeaderFields();
        currentReceipt = null;
        updateReceiptLabels(null, false);
        AlertUtil.showSuccess(stackPane, "Payment completed successfully");
    }

    public Label getPaidAmountLabel()      { return paidAmountLabel; }
    public Label getChangeGivenLabel()     { return changeGivenLabel; }
    public Label getReceiptStatusLabel()   { return receiptStatusLabel; }
    public Label getPaymentMethodLabel()   { return paymentMethodLabel; }
    public Label   getDateLabel()          { return dateLabel;          }
    public Label   getReceiptNumberLabel() { return receiptNumberLabel; }
    public Label   getEmployeeNameLabel()  { return employeeNameLabel;  }
    public Label   getMarketNameLabel()    { return marketNameLabel;    }
    public Label   getPhoneLabel()         { return phoneLabel;         }
    public Text    getAddressText()        { return addressText;        }
    public Label   getSubtotalLabel()      { return subtotalLabel;      }
    public Label   getTaxLabel()           { return taxLabel;           }
    public Label   getTotalCostLabel()     { return totalCostLabel;     }
    public ImageView getBarcodeImageView() { return barcodeImageView;   }
    public ScrollPane getItemsScrollPane() { return scrollPane;         }
}