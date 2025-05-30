package com.maya_yagan.sms.finance.controller;

import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.finance.model.FinancialRecord;
import com.maya_yagan.sms.util.ContextMenuUtil;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ToggleButton;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public class MainFinancePageController extends AbstractTableController<FinancialRecord> {
    @FXML private TableColumn<FinancialRecord, ?> amountColumn;
    @FXML private TableColumn<FinancialRecord, LocalDateTime> dateColumn;
    @FXML private TableColumn<FinancialRecord, String> descriptionColumn, categoryColumn;

    @FXML private Label incomeLabel, profitLabel, outcomeLabel;
    @FXML private LineChart<?, ?> lineChart;
    @FXML private ToggleButton todayFilterButton, thisMonthFilter, customDateFilter;


    @Override
    protected void configureColumns() {

    }

    @Override
    protected Collection<FinancialRecord> fetchData() {
        return List.of();
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<FinancialRecord>> menuItemsFor(FinancialRecord item) {
        return List.of();
    }
}
