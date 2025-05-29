package com.maya_yagan.sms.payment.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.common.ValidationService;
import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.model.ReceiptItem;
import com.maya_yagan.sms.payment.service.CashBoxService;
import com.maya_yagan.sms.payment.service.PaymentService;
import com.maya_yagan.sms.payment.service.RefundService;
import com.maya_yagan.sms.util.*;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.warehouse.service.WarehouseService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefundPageController extends AbstractTableController<ReceiptItem> {
    private static final Log log = LogFactory.getLog(RefundPageController.class);
    @FXML private TableColumn<ReceiptItem, Double> amountColumn;
    @FXML private Button cancelButton, proceedButton;
    @FXML private Label dateTimeLabel, paymentMethodLabel, totalPaidAmountLabel, totalRefundLabel, totalCostLabel;
    @FXML private TableColumn<ReceiptItem, String> productNameColumn;
    @FXML private TableColumn<ReceiptItem, Boolean> selectColumn;
    @FXML private TableColumn<ReceiptItem, BigDecimal> totalPriceColumn, unitPriceColumn;

    private final WarehouseService warehouseService = new WarehouseService();
    private final ValidationService validationService = new ValidationService();
    private final PaymentService paymentService = new PaymentService();
    private final RefundService refundService = new RefundService();
    private final Map<ReceiptItem, Double> basket = new HashMap<>();
    private Receipt receipt;
    private Runnable onCloseAction;
    private ModalPane modalPane;
    private BigDecimal refundAmount;


    @Override
    protected void configureColumns() {
        productNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProductName()));

        amountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getQuantity()));

        unitPriceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getUnitPrice()));

        totalPriceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getLineTotal()));

        TableViewUtil.setupCheckboxColumn(
                selectColumn,
                basket,
                ((receiptItem, selected) -> {
                    if(selected)
                        promptForAmountAndSelect(receiptItem);
                    else{
                        basket.remove(receiptItem);
                        refresh();
                        updateTotals();
                    }
                })
        );
    }

    @Override
    protected Collection<ReceiptItem> fetchData() {
        return receipt == null ? List.of() : receipt.getItems();
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<ReceiptItem>> menuItemsFor(ReceiptItem receiptItem) {
        return List.of();
    }

    @Override
    protected void postInit(){
        tableView.setEditable(true);
        setupEventHandlers();
    }

    private void setupEventHandlers(){
        cancelButton.setOnAction(event -> close() ) ;
        proceedButton.setOnAction(event -> refund());
    }

    private void promptForAmountAndSelect(ReceiptItem receiptItem){
        ViewUtil.showFloatInputDialog(
                "Enter Product Amount",
                "Enter the amount for " + receiptItem.getProduct().getName(),
                "Amount",
                1f,
                "Amount",
                newAmt ->{
                    try{
                        validationService.validateRefundQuantity(receiptItem, newAmt);
                        basket.put(receiptItem, newAmt);
                        refresh();
                        updateTotals();
                    } catch (CustomException e){
                        ExceptionHandler.handleException(e);
                        tableView.refresh();
                    }
                }
        );
    }

    private void updateTotals(){
        refundAmount = refundService.calculateRefundTotal(basket);
        totalRefundLabel.setText(paymentService.formatMoney(refundAmount));
    }

    private void loadLabels(){
        totalPaidAmountLabel.setText(paymentService.formatMoney(receipt.getPaidAmount()));
        totalCostLabel.setText(paymentService.formatMoney(receipt.getTotalCost()));
        dateTimeLabel.setText(DateUtil.formatDateTime(receipt.getDateTime()));
        paymentMethodLabel.setText(receipt.getPaymentMethod().toString());
    }

    private void refund(){
        if (basket.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING,
                    "No items selected",
                    "Please select at least one item to refund.");
            return;
        }

        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            AlertUtil.showAlert(Alert.AlertType.WARNING,
                    "Invalid refund amount",
                    "Refund amount must be greater than zero.");
            return;
        }

        showWarehouseChoiceDialog();
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = refundService.getReceiptByCode(receipt.getCode());
        loadLabels();
        refresh();
    }

    private void close(){
        if(modalPane != null) modalPane.hide();
        if(onCloseAction != null) onCloseAction.run();
    }

    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }

    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }

    private void showWarehouseChoiceDialog() {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        if (warehouses.isEmpty()) {
            AlertUtil.showAlert(Alert.AlertType.WARNING,
                    "No Inventory",
                    "There are no inventories configured yet");
            return;
        }

        ChoiceDialog<Warehouse> dialog = new ChoiceDialog<>(warehouses.get(0), warehouses);
        dialog.setTitle("Select Inventory");
        dialog.setHeaderText("Choose an Inventory");
        dialog.setContentText("Select an inventory where the refunded products will be stored:");
        dialog.showAndWait().ifPresent(selectedWarehouse -> {
            try {
                refundService.completeRefund(
                        receipt,
                        selectedWarehouse,
                        basket,
                        refundAmount);

                AlertUtil.showAlert(Alert.AlertType.INFORMATION,
                        "Success",
                        "Refund completed and products returned to " +
                                selectedWarehouse.getName());
                close();

            } catch (CustomException e) {
                ExceptionHandler.handleException(e);
                if ("INSUFFICIENT_CAPACITY".equals(e.getErrorCode())) {
                    showWarehouseChoiceDialog();
                }
            }
        });
    }
}