package com.maya_yagan.sms.payment.service;

import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.payment.model.PaymentMethod;
import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.model.ReceiptItem;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.settings.service.SettingsService;
import com.maya_yagan.sms.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PaymentService {

    private final SettingsService settingsService = new SettingsService();

    public String getCurrentEmployeeName() {
        return UserSession.getInstance().getCurrentUser().getFullName();
    }

    public String generateReceiptNumber() {
        return "RC-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    public float calculateDiscountedPrice(Product product){
        float price = product.getPrice();
        float discountPercent = product.getDiscount();
        return price * (1 - discountPercent / 100f);
    }

    /** Adds up all line totals (amount × discounted price). */
    public float calculateSubtotal(Map<Product, Double> items) {
        return (float) items.entrySet()
                .stream()
                .mapToDouble(e ->
                 e.getValue() * calculateDiscountedPrice(e.getKey()))
                .sum();
    }

    public String generateSimpleBarcodeData(String receiptNumber) {
        return receiptNumber.replaceAll("[^A-Za-z0-9]", ""); // remove dashes or special chars
    }

    public float calculateTotalTax(Map<Product, Double> items) {
        return (float) items.entrySet().stream()
                .mapToDouble(e -> {
                    Product p = e.getKey();
                    double amount = e.getValue();
                    float price = calculateDiscountedPrice(p);
                    float taxRate = p.getTaxPercentage();
                    return amount * price * taxRate;
                }).sum();
    }

    public float calculateTotalCost(Map<Product, Double> items) {
        float subtotal = calculateSubtotal(items);
        float tax = calculateTotalTax(items);
        return subtotal + tax;
    }

    public Receipt createReceipt(Map<Product, Double> basket,
                                 String code,
                                 PaymentMethod method) {
        User cashier = UserSession.getInstance().getCurrentUser();
        Receipt receipt = new Receipt(code, LocalDateTime.now(), cashier, method);

        List<ReceiptItem> items = new ArrayList<>();
        for (var entry : basket.entrySet()) {
            Product p = entry.getKey();
            double qty = entry.getValue();

            ReceiptItem item = new ReceiptItem();
            item.setReceipt(receipt);
            item.setProduct(p);
            item.setProductName(p.getName());
            item.setQuantity(qty);
            item.setUnitPrice(BigDecimal.valueOf(calculateDiscountedPrice(p)));
            item.setDiscount(BigDecimal.valueOf(p.getDiscount()));
            item.calcLineTotal();
            items.add(item);
        }
        receipt.setTotalCost(BigDecimal.valueOf(calculateTotalCost(basket)));
        receipt.setItems(items);
        return receipt;
    }

    public String formatMoney(BigDecimal amount){
        String unit = settingsService.getSettings().getMoneyUnit();
        return String.format("%.2f %s", amount, unit);
    }
}
