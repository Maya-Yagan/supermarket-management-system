package com.maya_yagan.sms.payment.controller;

import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.service.PaymentService;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class CashPaymentController implements Initializable {
    @FXML private TextField cashReceivedField;
    @FXML private Label changeLabel, totalAmountLabel;

    private final PaymentService paymentService = new PaymentService();
    private Runnable onCloseAction;
    private Receipt receipt;
    private final NumberFormat money = NumberFormat.getCurrencyInstance();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    if (receipt == null) return "";
                    try {
                        BigDecimal received =
                                new BigDecimal(cashReceivedField.getText().trim());
                        BigDecimal change   =
                                received.subtract(receipt.getTotalCost());
                        return money.format(change.max(BigDecimal.ZERO));
                    } catch (NumberFormatException ex) {
                        return "";     // empty / invalid entry
                    }
                }, cashReceivedField.textProperty())
        );
        changeLabel.setGraphic(null);
        // Optional: press Enter to trigger save
        cashReceivedField.setOnAction(e -> save());
    }

    public void save(){

    }

    private void loadData() {
        totalAmountLabel.setText(money.format(receipt.getTotalCost()));
        totalAmountLabel.setGraphic(null);
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
        loadData();
    }
}
