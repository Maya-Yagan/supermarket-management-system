package com.maya_yagan.sms.user.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.user.model.Attendance;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.AttendanceService;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.DateUtil;
import com.maya_yagan.sms.util.ExceptionHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class EditAttendanceController implements Initializable {
    @FXML private CheckBox absneceCheckbox;
    @FXML private TextField checkinField;
    @FXML private TextField checkoutField;
    @FXML private TextField notesField;
    @FXML private StackPane stackPane;

    private Attendance attendance;
    private ModalPane modalPane;
    private Runnable onCloseAction;

    private final AttendanceService attendanceService = new AttendanceService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        absneceCheckbox.selectedProperty().addListener(
                (obs, oldVal, isAbsent) -> toggleAbsentState(isAbsent)
        );
    }

    private void populateFields(){
        if(attendance != null){
            checkinField.setText(DateUtil.formatTimeOrDefault(attendance.getCheckIn(), "-"));
            checkoutField.setText(DateUtil.formatTimeOrDefault(attendance.getCheckOut(), "-"));
            notesField.setText(attendance.getNotes());
            if (attendance.getAbsent()) {
                checkinField.setText("Absent");
                checkoutField.setText("Absent");
                checkinField.setDisable(true);
                checkoutField.setDisable(true);
                absneceCheckbox.setSelected(true);
            }
            else {
                checkinField.setText(DateUtil.formatTimeOrDefault(attendance.getCheckIn(), "-"));
                checkoutField.setText(DateUtil.formatTimeOrDefault(attendance.getCheckOut(), "-"));
                checkinField.setDisable(false);
                checkoutField.setDisable(false);
            }
        }
    }

    private void toggleAbsentState(boolean isAbsent) {
        if (isAbsent) {
            checkinField.setText("Absent");
            checkoutField.setText("Absent");
            checkinField.setDisable(true);
            checkoutField.setDisable(true);
        } else {
            checkinField.clear();
            checkoutField.clear();
            checkinField.setDisable(false);
            checkoutField.setDisable(false);
        }
    }

    public boolean save(){
        try {
            String notes = notesField.getText().trim();
            boolean isAbsent = absneceCheckbox.isSelected();
            String inText = checkinField.getText().trim();
            String outText = checkoutField.getText().trim();

            attendanceService.updateAttendanceRecord(
                    attendance,
                    notes,
                    isAbsent,
                    inText,
                    outText
            );
            close();
            return true;
        } catch(CustomException e){
            ExceptionHandler.handleException(e);
            return false;
        }
    }

    private void close(){
        if(modalPane != null) modalPane.hide();
        if(onCloseAction != null) onCloseAction.run();
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
        populateFields();
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
}
