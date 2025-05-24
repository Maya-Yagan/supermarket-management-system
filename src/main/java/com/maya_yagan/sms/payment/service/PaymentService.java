package com.maya_yagan.sms.payment.service;

import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.homepage.service.HomePageService;

import java.util.Map;
import java.util.UUID;

public class PaymentService {

    private final ProductService productService = new ProductService();
    private final HomePageService homePageService = new HomePageService();

    public String getCurrentEmployeeName() {
        return UserSession.getInstance().getCurrentUser().getFullName();
    }

    public String generateReceiptNumber() {
        return "RC-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    /** Unit-price to show in the GridPane (discount already applied). */
    public float getDisplayPrice(Product p) {
        return productService.calculateDiscountedPrice(p);
    }

    /** Adds up all line totals (amount × discounted price). */
    public float calculateSubtotal(Map<Product, Double> items) {
        return (float) items.entrySet()
                .stream()
                .mapToDouble(e ->
                        e.getValue() * getDisplayPrice(e.getKey()))
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
                    float price = getDisplayPrice(p);
                    float taxRate = p.getTaxPercentage();
                    return amount * price * taxRate;
                }).sum();
    }

    public float calculateTotalCost(Map<Product, Double> items) {
        float subtotal = calculateSubtotal(items);
        float tax = calculateTotalTax(items);
        return subtotal + tax;
    }
}
