package com.maya_yagan.sms.finance.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.finance.service.FinanceService;
import com.maya_yagan.sms.util.DateUtil;
import com.maya_yagan.sms.util.ViewUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.maya_yagan.sms.util.MoneyUtil.formatMoney;

public class MainFinancePageController implements Initializable {
    @FXML private Label incomeLabel, profitLabel, outcomeLabel;
    @FXML private LineChart<?, ?> lineChart;
    @FXML private ToggleButton todayFilterButton, thisMonthFilter, customDateFilter, rangeFilter;
    @FXML private Button soldProductsButton, expenseButton, soldReceiptsButton, refundedButton;
    @FXML private StackPane stackPane;
    @FXML private ToggleGroup filterToggleGroup;

    private boolean revertingToggle = false;
    private LocalDateTime rangeFrom, rangeTo;
    private LocalDate selectedDate;
    private ModalPane modalPane;
    private final FinanceService financeService = new FinanceService();
    private SummaryTableController.PeriodKind selectedPeriodKind = SummaryTableController.PeriodKind.TODAY;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modalPane = ViewUtil.initializeModalPane(stackPane);

        filterToggleGroup = new ToggleGroup();
        todayFilterButton.setToggleGroup(filterToggleGroup);
        thisMonthFilter.setToggleGroup(filterToggleGroup);
        customDateFilter.setToggleGroup(filterToggleGroup);
        rangeFilter.setToggleGroup(filterToggleGroup);

        updateTotalsForCurrentFilter();
        setupEventHandlers();
        setupFilterListener();
    }

    private void setupEventHandlers(){
        soldProductsButton.setOnAction(event -> onSoldToday());
        expenseButton.setOnAction(event -> onExpense());
        soldReceiptsButton.setOnAction(event -> onSoldReceipt());
        refundedButton.setOnAction(event -> onRefunded());
    }

    private void setupFilterListener() {
        filterToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if(revertingToggle) return;

            if (newToggle == null) {
                selectedPeriodKind = null;
                return;
            } else if (newToggle == todayFilterButton) {
                selectedPeriodKind = SummaryTableController.PeriodKind.TODAY;
                updateTotalsForCurrentFilter();
                return;
            } else if (newToggle == thisMonthFilter) {
                selectedPeriodKind = SummaryTableController.PeriodKind.MONTH;
                updateTotalsForCurrentFilter();
                return;
            } else if (newToggle == customDateFilter) {
                LocalDate userDate = showDateDialog();
                if(userDate != null){
                    selectedDate = userDate;
                    selectedPeriodKind = SummaryTableController.PeriodKind.DATE;
                    updateTotalsForCurrentFilter();
                }
                else{
                    revertingToggle = true;
                    filterToggleGroup.selectToggle(oldToggle);
                    revertingToggle = false;
                }
                return;
            } else if (newToggle == rangeFilter) {
                LocalDateTime[] range = showRangeDialog();
                if(range != null && range.length == 2 && range[0] != null && range[1] != null){
                    rangeFrom = range[0];
                    rangeTo = range[1];
                    selectedPeriodKind = SummaryTableController.PeriodKind.RANGE;
                    updateTotalsForCurrentFilter();
                }
                else {
                    revertingToggle = true;
                    filterToggleGroup.selectToggle(oldToggle);
                    revertingToggle = false;
                }
            }
        });
    }


    private void onSoldToday() {
        LocalDate date = (selectedPeriodKind == SummaryTableController.PeriodKind.DATE && selectedDate != null)
                ? selectedDate : LocalDate.now();

        loadSummary(SummaryTableController.TableKind.SOLD_PRODUCTS,
                selectedPeriodKind, LocalDate.now().getYear(), LocalDate.now().getMonthValue(),
                date, rangeFrom, rangeTo);
    }

    private void onExpense(){
        LocalDate date = (selectedPeriodKind == SummaryTableController.PeriodKind.DATE && selectedDate != null)
                ? selectedDate : LocalDate.now();

        loadSummary(SummaryTableController.TableKind.EXPENSES,
                selectedPeriodKind, LocalDate.now().getYear(), LocalDate.now().getMonthValue(),
                date, rangeFrom, rangeTo);
    }

    private void onSoldReceipt(){
        LocalDate date = (selectedPeriodKind == SummaryTableController.PeriodKind.DATE && selectedDate != null)
                ? selectedDate : LocalDate.now();

        loadSummary(SummaryTableController.TableKind.RECEIPTS,
                selectedPeriodKind, LocalDate.now().getYear(), LocalDate.now().getMonthValue(),
                date, rangeFrom, rangeTo);
    }

    private void onRefunded(){
        LocalDate date = (selectedPeriodKind == SummaryTableController.PeriodKind.DATE && selectedDate != null)
                ? selectedDate : LocalDate.now();

        loadSummary(SummaryTableController.TableKind.REFUNDS,
                selectedPeriodKind, LocalDate.now().getYear(), LocalDate.now().getMonthValue(),
                date, rangeFrom, rangeTo);
    }

    private void loadSummary(SummaryTableController.TableKind tableKind,
                             SummaryTableController.PeriodKind periodKind,
                             Integer year, Integer month,
                             LocalDate date,
                             LocalDateTime from, LocalDateTime to)
    {
        ViewUtil.displayModalPaneView(
                "/view/finance/SummaryTable.fxml",
                (SummaryTableController controller) -> {
                    controller.init(financeService,
                            tableKind,
                            periodKind,
                            year, month, date, from, to,
                            modalPane);
                }, modalPane
        );
    }

    private LocalDate showDateDialog(){
        DatePicker datePicker = new DatePicker();
        Label prompt = new Label("Please enter a date:");
        VBox content = new VBox(10, prompt, datePicker);
        content.setPadding(new Insets(15));
        DateUtil.applyDateFormat(datePicker);
        Optional<ButtonType> result =
                ViewUtil.showCustomDialog("Enter Date", "Date input required", content);
        if(result.isPresent() && result.get() == ButtonType.OK){
            return datePicker.getValue();
        }
        return null;
    }

    private LocalDateTime[] showRangeDialog(){
        DatePicker fromPicker = new DatePicker();
        DatePicker toPicker = new DatePicker();

        Label fromLabel = new Label("From:");
        Label toLabel = new Label("To:");
        toLabel.setPadding(new Insets(0.0, 0.0, 0.0, 15.0));

        VBox content = new VBox(10,
            new HBox(10, fromLabel, fromPicker),
            new HBox(10, toLabel, toPicker)
        );
        content.setPadding(new Insets(15));

        DateUtil.applyDateFormat(fromPicker);
        DateUtil.applyDateFormat(toPicker);

        fromPicker.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.ENTER) {
                event.consume();
                toPicker.requestFocus();
            }
        });

        Optional<ButtonType> result = ViewUtil.showCustomDialog(
                "Select Date Range",
                "Please enter a start and end date:",
                content);

        if (result.isPresent() && result.get() == ButtonType.OK) {
            LocalDate fromDate = fromPicker.getValue();
            LocalDate toDate   = toPicker.getValue();
            if (fromDate != null && toDate != null && !fromDate.isAfter(toDate)) {
                return new LocalDateTime[]{
                        fromDate.atStartOfDay(),
                        toDate.atTime(23, 59, 59)
                };
            } else return null;
        }
        return null;
    }

    private void updateTotalsForCurrentFilter() {
        FinanceService.SeparatedRows rows;

        switch (selectedPeriodKind) {
            case TODAY -> rows = financeService.getToday();
            case MONTH -> rows = financeService.getMonth(2025, 5);
            case DATE -> {
                if(selectedDate != null)
                    rows = financeService.getDate(selectedDate);
                else return;
            }
            case RANGE -> {
                if(rangeFrom != null && rangeTo != null)
                    rows = financeService.getRange(rangeFrom, rangeTo);
                else return;
            }
            default -> {
                return;
            }
        }
        FinanceService.Totals totals = financeService.computeTotals(rows);

        incomeLabel.setText(formatMoney(totals.income()));
        outcomeLabel.setText(formatMoney(totals.outcome()));
        profitLabel.setText(formatMoney(totals.profit()));
    }
}
