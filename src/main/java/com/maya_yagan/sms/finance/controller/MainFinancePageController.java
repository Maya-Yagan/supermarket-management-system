package com.maya_yagan.sms.finance.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.finance.service.FinanceService;
import com.maya_yagan.sms.util.DateUtil;
import com.maya_yagan.sms.util.ViewUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.maya_yagan.sms.util.MoneyUtil.formatMoney;

/**
 * @author Maya Yagan
 */
public class MainFinancePageController implements Initializable {
    @FXML private Label incomeLabel, profitLabel, outcomeLabel;
    @FXML private LineChart<Number, Number> lineChart;
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
        thisMonthFilter.setSelected(true);
        selectedPeriodKind = SummaryTableController.PeriodKind.MONTH;

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
            case MONTH -> rows = financeService.getMonth(LocalDate.now().getYear(), LocalDate.now().getMonthValue());
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

        updateChart(rows);
    }

    private void updateChart(FinanceService.SeparatedRows rows){
        lineChart.getData().clear();

        Map<LocalDate, BigDecimal> incomeMap = financeService.groupByDate(rows.receipts());
        Map<LocalDate, BigDecimal> outcomeMap = financeService.groupByDate(rows.expenses());
        Map<LocalDate, BigDecimal> refundMap = financeService.groupByDate(rows.refunds());

        for (Map.Entry<LocalDate, BigDecimal> refund : refundMap.entrySet()) {
            outcomeMap.merge(refund.getKey(), refund.getValue(), BigDecimal::add);
        }

        XYChart.Series<Number, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");

        XYChart.Series<Number, Number> outcomeSeries = new XYChart.Series<>();
        outcomeSeries.setName("Outcome");

        incomeMap.forEach((date, amount) ->
                incomeSeries.getData().add(new XYChart.Data<>(date.toEpochDay(), amount))
        );

        outcomeMap.forEach((date, amount) ->
                outcomeSeries.getData().add(new XYChart.Data<>(date.toEpochDay(), amount))
        );

        lineChart.getData().addAll(incomeSeries, outcomeSeries);
        applySeriesColors();
        formatXAxisAsDates();
    }

    private void applySeriesColors() {
        Platform.runLater(() -> {
            for (int i = 0; i < lineChart.getData().size(); i++) {
                XYChart.Series<?, ?> series = lineChart.getData().get(i);
                String color = (i == 0) ? "green" : "red";

                Node line = series.getNode().lookup(".chart-series-line");
                if (line != null) {
                    line.setStyle("-fx-stroke: " + color + ";");
                }

                for (XYChart.Data<?, ?> data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle("-fx-background-color: " + color + ", white;");
                    }
                }

                Set<Node> legendItems = lineChart.lookupAll(".chart-legend-item");
                int finalI = i;
                int[] count = {0}; // Helper to track series index
                legendItems.forEach(item -> {
                    if (count[0] == finalI) {
                        Node symbol = item.lookup(".chart-legend-item-symbol");
                        if (symbol != null) {
                            symbol.setStyle("-fx-background-color: " + color + ", white;");
                        }
                    }
                    count[0]++;
                });
            }
        });
    }


    private void formatXAxisAsDates() {
        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

        // Collect all x-values (epoch days) from the data
        long minEpoch = Long.MAX_VALUE;
        long maxEpoch = Long.MIN_VALUE;

        for (XYChart.Series<Number, Number> series : lineChart.getData()) {
            for (XYChart.Data<Number, Number> data : series.getData()) {
                long epoch = data.getXValue().longValue();
                if (epoch < minEpoch) minEpoch = epoch;
                if (epoch > maxEpoch) maxEpoch = epoch;
            }
        }

        if (minEpoch == Long.MAX_VALUE || maxEpoch == Long.MIN_VALUE) return; // no data

        // Set axis bounds and tick unit
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(minEpoch);
        xAxis.setUpperBound(maxEpoch);
        xAxis.setTickUnit(1); // one tick per day

        // Set formatter to convert epoch day to date string
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return LocalDate.ofEpochDay(object.longValue()).toString();
            }

            @Override
            public Number fromString(String string) {
                return LocalDate.parse(string).toEpochDay();
            }
        });
    }
}