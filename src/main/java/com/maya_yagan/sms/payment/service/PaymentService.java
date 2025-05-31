package com.maya_yagan.sms.payment.service;

import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.finance.model.CashBox;
import com.maya_yagan.sms.finance.model.TransactionType;
import com.maya_yagan.sms.finance.service.CashBoxService;
import com.maya_yagan.sms.payment.creditcard.StripeService;
import com.maya_yagan.sms.payment.dao.ReceiptDAO;
import com.maya_yagan.sms.payment.model.*;
import com.maya_yagan.sms.settings.service.SettingsService;
import com.maya_yagan.sms.user.dao.UserDAO;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.warehouse.service.WarehouseService;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PaymentService {

    private final WarehouseService warehouseService = new WarehouseService();
    private final CashBoxService cashBoxService = new CashBoxService();
    private final ReceiptDAO receiptDAO = new ReceiptDAO();

    public String generateReceiptNumber() {
        return "RC-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    public BigDecimal  calculateDiscountedPrice(ProductWarehouse productWarehouse){
        BigDecimal  price = BigDecimal.valueOf(productWarehouse.getProduct().getPrice())
                            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountP = BigDecimal.valueOf(productWarehouse.getProduct().getDiscount())
                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return price.multiply(BigDecimal.ONE.subtract(discountP))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateSubtotal(Map<ProductWarehouse, Double> items) {
        return items.entrySet().stream()
                .map(e -> calculateDiscountedPrice(e.getKey())
                        .multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public String generateSimpleBarcodeData(String receiptNumber) {
        return receiptNumber.replaceAll("[^A-Za-z0-9]", "");
    }

    public BigDecimal calculateTotalTax(Map<ProductWarehouse, Double> items) {
        return items.entrySet().stream()
                .map(e -> {
                    BigDecimal price = calculateDiscountedPrice(e.getKey());
                    BigDecimal qty   = BigDecimal.valueOf(e.getValue());
                    BigDecimal rate  = BigDecimal.valueOf(e.getKey()
                            .getProduct()
                            .getTaxPercentage());
                    return price.multiply(qty)
                            .multiply(rate)
                            .setScale(2, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalCost(Map<ProductWarehouse, Double> items) {
        return calculateSubtotal(items)
                .add(calculateTotalTax(items))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Receipt createReceipt(Map<ProductWarehouse, Double> basket,
                                 String code,
                                 PaymentMethod method) {
        User cashier = UserSession.getInstance().getCurrentUser();
        cashier = new UserDAO().getUserById(cashier.getId());
        Receipt receipt = new Receipt(code, LocalDateTime.now(), cashier, method);
        CashBox current = cashBoxService.getCurrentOpenCashBox();
        if (current == null)
            throw new CustomException("No open cash box.\nPlease open the cash box first.", "NO_OPEN_CASH_BOX");

        List<ReceiptItem> items = new ArrayList<>();
        for (var entry : basket.entrySet()) {
            ProductWarehouse pw = entry.getKey();
            double qty = entry.getValue();

            ReceiptItem item = new ReceiptItem();
            item.setReceipt(receipt);
            item.setProduct(pw.getProduct());
            item.setProductName(pw.getProduct().getName());
            item.setQuantity(qty);
            item.setUnitPrice(calculateDiscountedPrice(pw));
            item.setDiscount(BigDecimal.valueOf(pw.getProduct().getDiscount()));
            item.calcLineTotal();
            items.add(item);
        }
        receipt.setTotalCost(calculateTotalCost(basket));
        receipt.setItems(items);
        receipt.setCashBox(current);
        return receipt;
    }

    public void completeCashPayment(Receipt receipt, Warehouse warehouse, BigDecimal paidAmount){
        if (paidAmount == null)
            throw new CustomException("Received cash amount is required", "MISSING_CASH_RECEIVED");

        BigDecimal totalCost = receipt.getTotalCost();
        if (paidAmount.compareTo(totalCost) < 0)
            throw new CustomException("Received amount is less than the total payable", "INSUFFICIENT_CASH");

        receipt.setPaidAmount(paidAmount);
        receipt.setChangeGiven(paidAmount.subtract(totalCost));

        receipt.setStatus(ReceiptStatus.COMPLETED);
        boolean ok = cashBoxService.recordTransaction(receipt.getTotalCost(), TransactionType.INCOME, "Sale completed for receipt: " + receipt.getCode());
        if(!ok)
            throw new CustomException("Cannot record income. No open cash box.", "NO_OPEN_CASH_BOX");

        warehouseService.decreaseReceiptItemFromWarehouse(receipt, warehouse);
        receiptDAO.insertReceipt(receipt);
    }

    public void completeCreditCardPayment(WebView webView, Receipt receipt, Object javaBridge){
        try {
            BigDecimal totalDollars = receipt.getTotalCost();
            long amountInCents = totalDollars
                    .movePointRight(2)
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();

            String clientSecret = StripeService.createPaymentIntentClientSecret(amountInCents, "usd");
            WebEngine engine = webView.getEngine();
            URL url = getClass().getResource("/credit_card.html");
            assert url != null;
            engine.load(url.toExternalForm() + "?clientSecret=" + clientSecret);

            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaConnector", javaBridge);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
