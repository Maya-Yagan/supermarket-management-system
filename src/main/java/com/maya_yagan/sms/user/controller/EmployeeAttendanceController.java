package com.maya_yagan.sms.user.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.user.model.Attendance;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.AttendanceService;
import com.maya_yagan.sms.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class EmployeeAttendanceController extends AbstractTableController<Attendance> {

    @FXML private TableColumn<Attendance, LocalDate>    dateColumn;
    @FXML private TableColumn<Attendance, String>       dayColumn;
    @FXML private TableColumn<Attendance, Boolean>      absentColumn;
    @FXML private TableColumn<Attendance, String>    checkInColumn;
    @FXML private TableColumn<Attendance, String>    checkOutColumn;
    @FXML private TableColumn<Attendance, String>     workingHoursColumn;
    @FXML private TableColumn<Attendance, String>     overtimeHoursColumn;
    @FXML private TableColumn<Attendance, String>       notesColumn;
    @FXML private Label startDateLabel;
    @FXML private Label totalAbsencesLabel;
    @FXML private Label totalHoursLabel;
    @FXML private Label employeeNameLabel;
    @FXML private MenuButton yearMenuButton, monthMenuButton;
    @FXML private Button closeButton, exportPDFButton;
    @FXML private StackPane stackPane;

    private ModalPane modalPane;
    private Runnable onCloseAction;
    private User user;
    private Integer selectedYear, selectedMonth;
    private final AttendanceService attendanceService = new AttendanceService();

    @Override
    protected void configureColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        absentColumn.setCellValueFactory(new PropertyValueFactory<>("absent"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        checkInColumn.setCellValueFactory(
            absentAwareFormatter(a -> DateUtil.formatTimeOrDefault(a.getCheckIn(), "-"))
        );

        checkOutColumn.setCellValueFactory(
                absentAwareFormatter(Attendance::getFormattedCheckOut)
        );

        overtimeHoursColumn.setCellValueFactory(
                absentAwareFormatter(Attendance::getFormattedOvertime)
        );

        workingHoursColumn.setCellValueFactory(
                absentAwareFormatter(Attendance::getFormattedWorkingHours)
        );

        dayColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(DateUtil.formatDayName(cellData.getValue().getDate()))
        );
    }

    @Override
    protected Collection<Attendance> fetchData() { return List.of(); }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<Attendance>> menuItemsFor(Attendance attendance) {
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Edit record", (item, row) -> handleEditAction(item))
        );
    }

    @Override
    protected void postInit(){
        setupEventHandlers();
        setupFiltering();
    }

    private void setupFiltering(){
        if(user == null) return;
        MenuButtonUtil.populateMenuButton(
                yearMenuButton,
                () -> attendanceService.getSelectableYears(user),
                String::valueOf,
                y -> { selectedYear = y; tryFilter(); },
                "Select Year",
                () -> { selectedYear = null; clearTable(); }
        );

        MenuButtonUtil.populateMenuButton(
                monthMenuButton,
                attendanceService::getSelectableMonths,
                String::valueOf,
                month -> { selectedMonth = month; tryFilter(); },
                "Select Month",
                () -> { selectedMonth = null; clearTable();}
        );

        selectedYear = null;
        selectedMonth = null;
    }

    private void clearTable() {
        tableView.getItems().clear();
        totalAbsencesLabel.setText("0");
        totalHoursLabel.setText("0h 0m");
    }

    private void tryFilter(){
        if (user == null || selectedYear == null || selectedMonth == null) return;
        var data = attendanceService.getAttendances(user, selectedYear, selectedMonth);
        tableView.getItems().setAll(data);
        long absences = attendanceService.countAbsences(user, selectedYear, selectedMonth);
        Duration total = attendanceService.getTotalWorkingTime(user, selectedYear, selectedMonth);

        totalAbsencesLabel.setText(String.valueOf(absences));
        totalHoursLabel.setText(DateUtil.formatDuration(total));
    }

    public void refreshUI(){
        if(user != null){
            employeeNameLabel.setText(user.getFirstName() + " " + user.getLastName());
            startDateLabel.setText(DateUtil.formatDate(user.getStartDate()));
        }
    }

    private void setupEventHandlers(){
        closeButton.setOnAction(event -> close());
        exportPDFButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Attendance Report");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            fileChooser.setInitialFileName("attendance-report.pdf");

            File file = fileChooser.showSaveDialog(exportPDFButton.getScene().getWindow());
            if (file != null) {
                PdfExportUtil.exportNodeToPdf(stackPane, file);
            }
        });
    }

    private void handleEditAction(Attendance attendance){
        try{
            boolean saved = ViewUtil.showDialog(
                    "/view/user/EditAttendance.fxml",
                    "Edit Attendance Record",
                    (EditAttendanceController controlelr) -> {
                        controlelr.setAttendance(attendance);
                        controlelr.setOnCloseAction(this::tryFilter);
                    },
                    EditAttendanceController::save
            );
            if(saved) refreshUI();
        } catch (IOException e) {
            ExceptionHandler.handleException(
                    new CustomException("Error loading view: " + e.getMessage(), "IO_ERROR"));
        }
    }

    private Callback<TableColumn.CellDataFeatures<Attendance,String>, ObservableValue<String>>
    absentAwareFormatter(Function<Attendance,String> formatter) {
        return cellData -> {
            Attendance att = cellData.getValue();
            String text = att.getAbsent()
                    ? "Absent"
                    : formatter.apply(att);
            return new SimpleStringProperty(text);
        };
    }

    private void close(){
        if(modalPane != null) modalPane.hide();
        if(onCloseAction != null) onCloseAction.run();
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    public void setUser(User user) {
        this.user = user;
        refresh();
        refreshUI();
        setupFiltering();
    }
}
