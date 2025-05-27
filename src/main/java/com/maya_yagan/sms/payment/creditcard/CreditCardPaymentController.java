package com.maya_yagan.sms.payment.creditcard;

import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.service.PaymentService;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class CreditCardPaymentController implements Initializable {
    @FXML WebView webView;

    private PaymentResultListener paymentResultListener;
    private Receipt receipt;
    private BigDecimal paidAmount;
    private Warehouse warehouse;

    private final PaymentService paymentService = new PaymentService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Dotenv dotenv = Dotenv.load();
        String pk = dotenv.get("STRIPE_PUBLISHABLE_KEY");

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                String js = String.format(
                        // define the var and immediately call the function
                        "window.PUBLISHABLE_KEY = '%s';" +
                                "if (typeof initStripe === 'function') initStripe(window.PUBLISHABLE_KEY);",
                        pk.replace("'", "\\'")
                );
                webView.getEngine().executeScript(js);
            }
        });
    }

    public void paymentSucceeded(BigDecimal paidAmount) {
        System.out.println("im the first");
        try {
            System.out.println("JS says: payment succeeded!");
            System.out.println("Paid amount from JS: " + paidAmount);
            System.out.println("Receipt total cost: " + receipt.getTotalCost());
            this.paidAmount = paidAmount;
            receipt.setPaidAmount(paidAmount);
            paymentService.completeCashPayment(receipt, warehouse, paidAmount);

            Platform.runLater(() -> {
                if (paymentResultListener != null) {
                    paymentResultListener.onPaymentSucceeded();
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void paymentSucceeded(double amount) {
        System.out.println("im the second");
        paymentSucceeded(BigDecimal.valueOf(amount));
    }

    public void paymentSucceeded(String amountStr) {
        System.out.println("im the third");
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            paymentSucceeded(amount);
        } catch (NumberFormatException e) {
            System.err.println("Invalid payment amount from JS: " + amountStr);
            e.printStackTrace();
        }
    }

    public void setPaymentResultListener(PaymentResultListener paymentResultListener) {
        this.paymentResultListener = paymentResultListener;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
        paidAmount = receipt.getPaidAmount();
        paymentService.completeCreditCardPayment(webView, receipt, this);
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }
}
