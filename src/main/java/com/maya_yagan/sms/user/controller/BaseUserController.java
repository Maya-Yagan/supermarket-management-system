package com.maya_yagan.sms.user.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.user.service.UserService;
import com.maya_yagan.sms.util.DateUtil;
import com.maya_yagan.sms.util.ValidationService;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

/**
 * 
 * @author Maya Yagan
 */
public abstract class BaseUserController implements Initializable {

    @FXML protected TextField firstNameField, lastNameField, emailField, phoneNumberField, tcNumberField, salaryField;
    @FXML protected DatePicker birthDatePicker;
    @FXML protected PasswordField passwordField;
    @FXML protected MenuButton  positionMenuButton, employmentTypeMenu, genderMenuButton;
    @FXML protected CheckMenuItem managerCheckMenuItem, accountantCheckMenuItem, depotEmployeeCheckMenuItem, cashierCheckMenuItem;
    @FXML protected MenuItem partTimeMenuItem, fullTimeMenuItem, maleMenuItem, femaleMenuItem;
    
    protected Runnable onCloseAction;
    protected ModalPane modalPane;
    protected String selectedEmploymentType;
    protected String selectedGender;
    protected final Set<String> selectedPositions = new HashSet<>();
    protected final UserService userService = new UserService();
    protected final ValidationService validationService = new ValidationService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DateUtil.applyDateFormat(birthDatePicker);
        initializeGenderMenu();
        initializeEmploymentTypeMenu();
        initializePositionMenu();
        addFieldValidation();
    }

    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    private void initializeGenderMenu() {
        maleMenuItem.setOnAction(e -> {
            selectedGender = "Male";
            genderMenuButton.setText(selectedGender);
        });
        femaleMenuItem.setOnAction(e -> {
            selectedGender = "Female";
            genderMenuButton.setText(selectedGender);
        });
    }

    private void initializeEmploymentTypeMenu() {
        partTimeMenuItem.setOnAction(e -> {
            selectedEmploymentType = "Part time";
            employmentTypeMenu.setText(selectedEmploymentType);
        });
        fullTimeMenuItem.setOnAction(e -> {
            selectedEmploymentType = "Full time";
            employmentTypeMenu.setText(selectedEmploymentType);
        });
    }

    private void initializePositionMenu() {
        managerCheckMenuItem.selectedProperty()
                .addListener((obs, oldVal, newVal) -> updateSelectedPositions());
        accountantCheckMenuItem.selectedProperty()
                .addListener((obs, oldVal, newVal) -> updateSelectedPositions());
        depotEmployeeCheckMenuItem.selectedProperty()
                .addListener((obs, oldVal, newVal) -> updateSelectedPositions());
        cashierCheckMenuItem.selectedProperty()
                .addListener((obs, oldVal, newVal) -> updateSelectedPositions());
    }

    private void updateSelectedPositions() {
        if (managerCheckMenuItem.isSelected()) selectedPositions.add("manager");
        if (accountantCheckMenuItem.isSelected()) selectedPositions.add("accountant");
        if (depotEmployeeCheckMenuItem.isSelected()) selectedPositions.add("warehouse worker");
        if (cashierCheckMenuItem.isSelected()) selectedPositions.add("cashier");
    }
    
    protected void setPositionMenuSelection() {
        managerCheckMenuItem.setSelected(selectedPositions.contains("manager"));
        accountantCheckMenuItem.setSelected(selectedPositions.contains("accountant"));
        depotEmployeeCheckMenuItem.setSelected(selectedPositions.contains("warehouse worker"));
        cashierCheckMenuItem.setSelected(selectedPositions.contains("cashier"));
        String displayText = selectedPositions.isEmpty() ?
                "Select" : String.join(", ", selectedPositions);
        positionMenuButton.setText(displayText);
    }

    private void addFieldValidation() {
        emailField.textProperty().addListener((obs, old, newVal) -> {
            if (validationService.isValidEmail(newVal))
                emailField.setStyle("-fx-border-color: green;");
            else
                emailField.setStyle("-fx-border-color: red;");
        });

        salaryField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!validationService.isValidSalary(newValue))
                salaryField.setText(oldValue);
        });
    }

    @FXML protected abstract void handleSave();

    @FXML protected void close() {
        if (modalPane != null) modalPane.hide();
    }
}
