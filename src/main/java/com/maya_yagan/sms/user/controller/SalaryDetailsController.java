package com.maya_yagan.sms.user.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.user.dao.SalaryDetailsDTO;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.AdvanceService;
import com.maya_yagan.sms.user.service.AttendanceService;
import com.maya_yagan.sms.util.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SalaryDetailsController extends AbstractTableController<SalaryDetailsDTO> {

    @FXML private Label startDateLabel;
    @FXML private Label positionLabel;
    @FXML private Label fullNameLabel;
    @FXML private MenuButton yearMenuButton;
    @FXML private MenuButton monthMenuButton;
    @FXML private Button closeButton;
    @FXML private Button exportPDFButton;
    @FXML private StackPane stackPane;

    @FXML private TableColumn<SalaryDetailsDTO, Integer> totalDaysColumn;
    @FXML private TableColumn<SalaryDetailsDTO, String> dailySalaryColumn;
    @FXML private TableColumn<SalaryDetailsDTO, String> totalAdvanceColumn;
    @FXML private TableColumn<SalaryDetailsDTO, String> monthlyAdvanceColumn;
    @FXML private TableColumn<SalaryDetailsDTO, String> remainingAdvanceColumn;
    @FXML private TableColumn<SalaryDetailsDTO, String> grossSalaryColumn;
    @FXML private TableColumn<SalaryDetailsDTO, String> netSalaryColumn;

    private ModalPane modalPane;
    private User user;
    private Runnable onCloseAction;
    private Integer selectedYear, selectedMonth;

    private final AttendanceService attendanceService = new AttendanceService();
    private final AdvanceService advanceService = new AdvanceService();

    @Override
    protected void configureColumns() {
        totalDaysColumn.setCellValueFactory(new PropertyValueFactory<>("totalDays"));
        dailySalaryColumn.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(MoneyUtil.formatMoney(cellData.getValue().getSalaryPerDay()))
        );
        totalAdvanceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(MoneyUtil.formatMoney(cellData.getValue().getTotalAdvance()))
        );
        monthlyAdvanceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(MoneyUtil.formatMoney(cellData.getValue().getMonthlyAdvance()))
        );
        remainingAdvanceColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(MoneyUtil.formatMoney(cellData.getValue().getRemainingAdvance()))
        );
        grossSalaryColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(MoneyUtil.formatMoney(cellData.getValue().getGrossSalary()))
        );
        netSalaryColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(MoneyUtil.formatMoney(cellData.getValue().getNetSalary()))
        );
    }

    @Override
    protected Collection<SalaryDetailsDTO> fetchData() {
        if (user == null || selectedYear == null || selectedMonth == null)
            return List.of();
        int totalDays = attendanceService
                .countPresentDays(user, selectedYear, selectedMonth);

        SalaryDetailsDTO dto = advanceService
                .buildSalaryDetails(user, selectedYear, selectedMonth, totalDays);

        return List.of(dto);
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<SalaryDetailsDTO>> menuItemsFor(SalaryDetailsDTO salaryDetailsDTO) {
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Add Advance", (row, item) -> handleAddAdvance(row)),
                new ContextMenuUtil.MenuItemConfig<>("Edit Advance", (row, item) -> handleEditAdvance(row)),
                new ContextMenuUtil.MenuItemConfig<>("Delete Advance", (row, item) -> handleDeleteAdvance(row))
        );
    }

    private void handleAddAdvance(SalaryDetailsDTO row) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        Label totalLabel = new Label("Total Advance:");
        TextField totalField = new TextField();
        totalField.setPromptText("Total Advance");

        Label monthlyLabel = new Label("Monthly Advance:");
        TextField monthlyField = new TextField();
        monthlyField.setPromptText("Monthly Advance");

        grid.add(totalLabel, 0, 0);
        grid.add(totalField, 1, 0);
        grid.add(monthlyLabel, 0, 1);
        grid.add(monthlyField, 1, 1);

        Optional<ButtonType> result = ViewUtil.showCustomDialog(
                "Add Advance",
                "Enter total and monthly advance amounts",
                grid
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double total = Double.parseDouble(totalField.getText());
                double monthly = Double.parseDouble(monthlyField.getText());
                LocalDate startDate = LocalDate.of(selectedYear, selectedMonth, 1);
                advanceService.addAdvance(user, total, monthly, startDate);
                refresh();
            } catch (CustomException e) {
                ExceptionHandler.handleException(e);
            }
        }
    }

    private void handleEditAdvance(SalaryDetailsDTO row) {
        ViewUtil.showFloatInputDialog(
                "Edit Monthly Advance",
                "Monthly Advance",
                "Enter new monthly advance value:",
                row.getMonthlyAdvance().floatValue(),
                "Advance",
                newValue -> {
                    advanceService.updateMonthlyAdvance(user, selectedYear, selectedMonth, newValue);
                    refresh();
                }
        );
    }

    private void handleDeleteAdvance(SalaryDetailsDTO row){
        AlertUtil.showDeleteConfirmation(row,
                "Delete Advance",
                "Are you sure you want to delete this advance?",
                "This action cannot be undone.",
                u -> {
                    advanceService.deleteLatestAdvance(user, selectedYear, selectedMonth);
                    refresh();
                }
        );
    }

    @Override
    protected void postInit() {
        setupEventHandlers();
        setupFiltering();
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setUser(User user) {
        this.user = user;
        refreshUI();
        setupFiltering();
        refresh();
    }

    private void refreshUI() {
        if (user != null) {
            fullNameLabel.setText(user.getFullName());
            if (user.getRoles() != null) {
                String roles = user.getRoles().stream()
                        .map(r -> r.getName() != null ? r.getName() : "")
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");
                positionLabel.setText(roles);
            }
            startDateLabel.setText(DateUtil.formatDate(user.getStartDate()));
        }
    }

    private void setupEventHandlers() {
        closeButton.setOnAction(e -> close());
        exportPDFButton.setOnAction(e -> handleExportPDF());
    }

    private void close() {
        if (modalPane != null) modalPane.hide();
        if (onCloseAction != null) onCloseAction.run();
    }

    private void setupFiltering() {
        if (user == null) return;

        MenuButtonUtil.populateMenuButton(
                yearMenuButton,
                () -> attendanceService.getSelectableYears(user),
                String::valueOf,
                y -> {
                    selectedYear = y;
                    refresh();
                },
                "Select Year",
                () -> {
                    selectedYear = null;
                    clearTable();
                }
        );

        MenuButtonUtil.populateMenuButton(
                monthMenuButton,
                attendanceService::getSelectableMonths,
                String::valueOf,
                m -> {
                    selectedMonth = m;
                    refresh();
                },
                "Select Month",
                () -> {
                    selectedMonth = null;
                    clearTable();
                }
        );
        selectedYear = null;
        selectedMonth = null;
    }

    private void clearTable() {
        tableView.getItems().clear();
    }

    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Salary Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName("salary-report.pdf");

        File file = fileChooser.showSaveDialog(exportPDFButton.getScene().getWindow());
        if (file != null) {
            PdfExportUtil.exportNodeToPdf(stackPane, file);
        }
    }
}