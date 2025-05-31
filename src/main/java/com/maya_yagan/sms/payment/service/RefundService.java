package com.maya_yagan.sms.payment.service;

import com.maya_yagan.sms.finance.service.CashBoxService;
import com.maya_yagan.sms.payment.dao.ReceiptDAO;
import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.model.ReceiptItem;
import com.maya_yagan.sms.payment.model.ReceiptStatus;
import com.maya_yagan.sms.finance.model.TransactionType;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.warehouse.service.WarehouseService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class RefundService {
    private final ReceiptDAO receiptDAO = new ReceiptDAO();
    private final WarehouseService warehouseService = new WarehouseService();
    private final CashBoxService cashBoxService = new CashBoxService();

    public Receipt getReceiptByCode(String code){
        return receiptDAO.getReceiptByCode(code);
    }

    public BigDecimal calculateRefundTotal(Map<ReceiptItem, Double> basket) {
        return basket.entrySet().stream()
                .map(e -> {
                    ReceiptItem item = e.getKey();
                    double quantity = e.getValue();

                    BigDecimal unitPrice = item.getUnitPrice();
                    float taxPercentage = item.getProduct().getTaxPercentage();
                    BigDecimal taxMultiplier = BigDecimal.valueOf(1 + taxPercentage);

                    return unitPrice
                            .multiply(taxMultiplier)
                            .multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void completeRefund(Receipt             receipt,
                               Warehouse           warehouse,
                               Map<ReceiptItem,Double> basket,
                               BigDecimal          refundAmount) {

        boolean ok = cashBoxService.recordTransaction(
                refundAmount, TransactionType.REFUND, "Refund for receipt: " + receipt.getCode());
        if (!ok) {
            throw new CustomException(
                    "Cannot record expense. No open cash box.",
                    "NO_OPEN_CASH_BOX");
        }

        for (var entry : basket.entrySet()) {
            ReceiptItem item = entry.getKey();
            int         qty  = entry.getValue().intValue();

            warehouseService.findProductWarehouseByName(warehouse, item.getProduct().getName())
                    .ifPresentOrElse(
                            pw -> {
                                int newQty = pw.getAmount() + qty;
                                warehouseService.updateProductStock(warehouse, pw, newQty);
                            },
                            () -> warehouseService.addProductToWarehouse(warehouse,
                                    item.getProduct(),
                                    qty)
                    );
        }

        receipt.setStatus(ReceiptStatus.REFUNDED);
        receipt.setChangeGiven(refundAmount.negate());
        receiptDAO.markAsRefunded(receipt.getId(), refundAmount.negate());
    }
}
