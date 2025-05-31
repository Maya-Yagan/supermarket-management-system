package com.maya_yagan.sms.finance.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.finance.model.FinanceRow;
import com.maya_yagan.sms.finance.service.FinanceService;
import com.maya_yagan.sms.util.DateUtil;
import com.maya_yagan.sms.util.MoneyUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import javax.annotation.Nullable;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class SummaryTableController implements Initializable {
    @FXML private TableColumn<FinanceRow, String> amountColumn;
    @FXML private TableColumn<FinanceRow, String> dateColumn;
    @FXML private TableColumn<FinanceRow, String> descriptionColumn, categoryColumn;
    @FXML private TableColumn<FinanceRow, String> personColumn;
    @FXML TableView<FinanceRow> tableView;
    @FXML private Label tableNameLabel;
    @FXML private Button closeButton;

    private FinanceService financeService;
    private TableKind tableKind;
    private PeriodKind  periodKind;
    private int year, month;
    private LocalDate date;
    private LocalDateTime from, to;
    private ModalPane modalPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureColumns();
        closeButton.setOnAction(event -> {if(modalPane != null) modalPane.hide();});
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
    }

    private void configureColumns() {
        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(DateUtil.formatDateTime(cellData.getValue().getDateTime()))
        );
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(MoneyUtil.formatMoney(cellData.getValue().getAmount()))
        );
        personColumn.setCellValueFactory(new PropertyValueFactory<>("person"));
    }

    public void init(FinanceService financeService,
                TableKind      tableKind,
                PeriodKind     periodKind,
                @Nullable Integer year,
                @Nullable Integer month,
                @Nullable LocalDate date,
                @Nullable LocalDateTime from,
                @Nullable LocalDateTime to,
                ModalPane      modalPane){
        this.financeService = financeService;
        this.tableKind  = tableKind;
        this.periodKind = periodKind;
        this.year  = year  == null ? 0 : year;
        this.month = month == null ? 0 : month;
        this.date  = date;
        this.from  = from;
        this.to    = to;
        this.modalPane = modalPane;

        tableNameLabel.setText(tableKind.getHumanTitle());
        populateTable();
    }

    private void populateTable() {
        FinanceService.SeparatedRows rows = switch (periodKind) {
            case TODAY  -> financeService.getToday();
            case MONTH  -> financeService.getMonth(year, month);
            case DATE   -> financeService.getDate(date);
            case RANGE  -> financeService.getRange(from, to);
        };

        ObservableList<FinanceRow> data = switch (tableKind) {
            case SOLD_PRODUCTS -> FXCollections.observableArrayList(rows.soldProducts());
            case RECEIPTS      -> FXCollections.observableArrayList(rows.receipts());
            case EXPENSES      -> FXCollections.observableArrayList(rows.expenses());
            case REFUNDS       -> FXCollections.observableArrayList(rows.refunds());
        };

        tableView.setItems(data);
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    public enum TableKind {
        SOLD_PRODUCTS("Sold products"),
        RECEIPTS("Manual receipts"),
        EXPENSES("Supplier payments"),
        REFUNDS("Refunds");

        private final String humanTitle;
        TableKind(String s){ humanTitle = s; }
        public String getHumanTitle(){ return humanTitle; }
    }

    public enum PeriodKind { TODAY, MONTH, DATE, RANGE }
}
