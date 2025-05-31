package com.maya_yagan.sms.finance.service;

import com.maya_yagan.sms.finance.dao.FinancialRecordDAO;
import com.maya_yagan.sms.finance.model.FinanceRow;
import com.maya_yagan.sms.finance.model.FinancialRecord;
import com.maya_yagan.sms.payment.dao.ReceiptDAO;
import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.model.ReceiptItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FinanceService {
    private final FinancialRecordDAO financialRecordDAO = new FinancialRecordDAO();
    private final ReceiptDAO receiptDAO = new ReceiptDAO();

    public SeparatedRows getToday() {
        LocalDate today = LocalDate.now();
        return getSeparatedRows(today.atStartOfDay(), today.atTime(LocalTime.MAX),
                PeriodKind.TODAY);
    }

    public SeparatedRows getMonth(int year, int month) {
        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate last  = first.plusMonths(1).minusDays(1);
        return getSeparatedRows(first.atStartOfDay(), last.atTime(LocalTime.MAX),
                PeriodKind.MONTH);
    }

    public SeparatedRows getDate(LocalDate date) {
        return getSeparatedRows(date.atStartOfDay(), date.atTime(LocalTime.MAX),
                PeriodKind.DATE);
    }

    public SeparatedRows getRange(LocalDateTime from, LocalDateTime to) {
        return getSeparatedRows(from, to, PeriodKind.RANGE);
    }

    public Totals computeTotals(SeparatedRows rows) {
        BigDecimal income = sumAmount(rows.receipts());
        BigDecimal outcome = sumAmount(rows.expenses());
        BigDecimal refund  = sumAmount(rows.refunds());

        BigDecimal totalOutcome = outcome.add(refund);
        BigDecimal profit = income.subtract(totalOutcome);

        return new Totals(income, totalOutcome, profit);
    }

    private BigDecimal sumAmount(List<FinanceRow> list) {
        return list.stream()
                .map(FinanceRow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private SeparatedRows getSeparatedRows(LocalDateTime from,
                                           LocalDateTime to,
                                           PeriodKind  kind) {
        List<FinancialRecord> records  =
                financialRecordDAO.getRecordsForPeriod(from, to);

        List<Receipt> receiptsEntities =
                receiptDAO.getReceiptsForPeriod(from, to);

        List<ReceiptItem> receiptItems = receiptsEntities.stream()
                .flatMap(r -> r.getItems().stream())
                .toList();

        List<FinanceRow> soldProducts = new ArrayList<>(receiptItems.size());
        List<FinanceRow> receipts     = new ArrayList<>();
        List<FinanceRow> expenses     = new ArrayList<>();
        List<FinanceRow> refunds      = new ArrayList<>();

        for (ReceiptItem ri : receiptItems) {
            soldProducts.add(new FinanceRow(
                    ri.getReceipt().getDateTime(),
                    ri.getProductName(),
                    "INCOME",
                    ri.getLineTotal(),
                    ri.getReceipt().getCashier().getFullName()
            ));
        }

        for (FinancialRecord fr : records) {
            FinanceRow row = new FinanceRow(
                    fr.getDateTime(),
                    fr.getDescription(),
                    fr.getType().name(),
                    fr.getAmount(),
                    fr.getIssuedBy().getFullName()
            );

            switch (fr.getType()) {
                case INCOME  -> receipts.add(row);
                case EXPENSE -> expenses.add(row);
                case REFUND  -> refunds.add(row);
                default      -> throw new IllegalStateException(
                        "Unhandled record type " + fr.getType());
            }
        }

        sortNewestFirst(soldProducts);
        sortNewestFirst(receipts);
        sortNewestFirst(expenses);
        sortNewestFirst(refunds);

        return new SeparatedRows(
                List.copyOf(soldProducts),
                List.copyOf(receipts),
                List.copyOf(expenses),
                List.copyOf(refunds)
        );
    }

    private static void sortNewestFirst(List<FinanceRow> list) {
        list.sort(Comparator.comparing(FinanceRow::getDateTime).reversed());
    }

    public record Totals(BigDecimal income,
                         BigDecimal outcome,
                         BigDecimal profit) {}

    public record SeparatedRows(
            List<FinanceRow> soldProducts,
            List<FinanceRow> receipts,
            List<FinanceRow> expenses,
            List<FinanceRow> refunds) {
    }

    public enum PeriodKind { TODAY, MONTH, DATE, RANGE }
}
