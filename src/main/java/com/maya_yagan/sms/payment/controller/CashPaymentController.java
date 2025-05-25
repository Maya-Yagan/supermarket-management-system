package com.maya_yagan.sms.payment.controller;

import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.service.PaymentService;
import com.maya_yagan.sms.settings.service.SettingsService;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class CashPaymentController implements Initializable {
    @FXML private TextField cashReceivedField;
    @FXML private Label changeLabel, totalAmountLabel;

    private final PaymentService paymentService = new PaymentService();
    private Runnable onCloseAction;
    private Receipt receipt;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        cashReceivedField.setOnAction(e -> save());
    }

    public void save(){

    }

    private void loadData() {
        totalAmountLabel.setText(paymentService.formatMoney(receipt.getTotalCost()));
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
        loadData();
    }
}
