package com.maya_yagan.sms.payment.service;

import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.payment.dao.ReceiptDAO;
import com.maya_yagan.sms.payment.model.*;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.settings.service.SettingsService;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.warehouse.service.WarehouseService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PaymentService {

    private final WarehouseService warehouseService = new WarehouseService();
    private final SettingsService settingsService = new SettingsService();
    private final CashBoxService cashBoxService = new CashBoxService();
    private final ReceiptDAO receiptDAO = new ReceiptDAO();

    public String getCurrentEmployeeName() {
        return UserSession.getInstance().getCurrentUser().getFullName();
    }

    public String generateReceiptNumber() {
        return "RC-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    public float calculateDiscountedPrice(ProductWarehouse productWarehouse){
        float price = productWarehouse.getProduct().getPrice();
        float discountPercent = productWarehouse.getProduct().getDiscount();
        return price * (1 - discountPercent / 100f);
    }

    /** Adds up all line totals (amount × discounted price). */
    public float calculateSubtotal(Map<ProductWarehouse, Double> items) {
        return (float) items.entrySet()
                .stream()
                .mapToDouble(e ->
                 e.getValue() * calculateDiscountedPrice(e.getKey()))
                .sum();
    }

    public String generateSimpleBarcodeData(String receiptNumber) {
        return receiptNumber.replaceAll("[^A-Za-z0-9]", "");
    }

    public float calculateTotalTax(Map<ProductWarehouse, Double> items) {
        return (float) items.entrySet().stream()
                .mapToDouble(e -> {
                    ProductWarehouse pw = e.getKey();
                    double amount = e.getValue();
                    float price = calculateDiscountedPrice(pw);
                    float taxRate = pw.getProduct().getTaxPercentage();
                    return amount * price * taxRate;
                }).sum();
    }

    public float calculateTotalCost(Map<ProductWarehouse, Double> items) {
        float subtotal = calculateSubtotal(items);
        float tax = calculateTotalTax(items);
        return subtotal + tax;
    }

    public Receipt createReceipt(Map<ProductWarehouse, Double> basket,
                                 String code,
                                 PaymentMethod method) {
        User cashier = UserSession.getInstance().getCurrentUser();
        Receipt receipt = new Receipt(code, LocalDateTime.now(), cashier, method);

        List<ReceiptItem> items = new ArrayList<>();
        for (var entry : basket.entrySet()) {
            ProductWarehouse pw = entry.getKey();
            double qty = entry.getValue();

            ReceiptItem item = new ReceiptItem();
            item.setReceipt(receipt);
            item.setProduct(pw.getProduct());
            item.setProductName(pw.getProduct().getName());
            item.setQuantity(qty);
            item.setUnitPrice(BigDecimal.valueOf(calculateDiscountedPrice(pw)));
            item.setDiscount(BigDecimal.valueOf(pw.getProduct().getDiscount()));
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

    public void completeCashPayment(Receipt receipt, Warehouse warehouse){
        receipt.setStatus(ReceiptStatus.COMPLETED);

        boolean ok = cashBoxService.recordTransaction(receipt.getTotalCost().doubleValue(), TransactionType.INCOME);
        if(!ok){
            throw new CustomException(
                    "Cannot record income. No open cash box.",
                    "NO_OPEN_CASH_BOX"
            );
        }

        for (ReceiptItem item : receipt.getItems()) {
            Product product = item.getProduct();
            double quantity = item.getQuantity();

            warehouseService.findProductWarehouseByName(warehouse, product.getName())
                    .ifPresentOrElse(pw -> {
                        int currentAmount = pw.getAmount();
                        int newAmount = currentAmount - (int) quantity;
                        if (newAmount < 0) {
                            throw new CustomException("Insufficient stock for product: " + product.getName(), "INSUFFICIENT_STOCK");
                        }
                        warehouseService.updateProductStock(warehouse, pw, newAmount);
                    }, () -> {
                        throw new CustomException("Product not found in warehouse: " + product.getName(), "PRODUCT_NOT_FOUND");
                    });
        }
        receiptDAO.insertReceipt(receipt);
    }
}
