package com.maya_yagan.sms.payment.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.ValidationService;
import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.service.PaymentService;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ExceptionHandler;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class CashPaymentController implements Initializable {
    @FXML private TextField cashReceivedField;
    @FXML private Label changeLabel, totalAmountLabel;

    private final ValidationService validationService = new ValidationService();
    private final PaymentService paymentService = new PaymentService();
    private Runnable onCloseAction;
    private Receipt receipt;
    private Warehouse currentInventory;
    private ModalPane modalPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cashReceivedField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!validationService.isValidMoney(newValue))
                cashReceivedField.setText(oldValue);
        });
    }

    public boolean save(){
        if(receipt == null) return false;

        String receivedCash = cashReceivedField.getText().trim();
        if(receivedCash.isEmpty()){
            AlertUtil.showAlert(Alert.AlertType.ERROR,
                    "Missing Amount", "Please enter how much cash was received.");
            return false;
        }

        BigDecimal received;
        try {
            received = new BigDecimal(receivedCash);
        } catch (NumberFormatException e) {
            AlertUtil.showAlert(Alert.AlertType.ERROR,
                    "Invalid Number", "Please enter a valid amount.");
            return false;
        }

        try {
            paymentService.completeCashPayment(receipt, currentInventory, received);
            close();
            return true;
        } catch (CustomException ex) {
            ExceptionHandler.handleException(ex);
            return false;
        }
    }

    private void loadData() {
        totalAmountLabel.setText(paymentService.formatMoney(receipt.getTotalCost()));
        changeLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    if (receipt == null) return "";
                    try {
                        BigDecimal received = new BigDecimal(cashReceivedField.getText().trim());
                        BigDecimal change = received.subtract(receipt.getTotalCost());
                        return paymentService.formatMoney(change.max(BigDecimal.ZERO));
                    } catch (NumberFormatException ex) {
                        return "";
                    }
                }, cashReceivedField.textProperty())
        );
    }

    private void close(){
        if(onCloseAction != null) onCloseAction.run();
        if(modalPane != null) modalPane.hide();
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
        loadData();
    }

    public void setCurrentInventory(Warehouse currentInventory) {
        this.currentInventory = currentInventory;
    }

    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
}
