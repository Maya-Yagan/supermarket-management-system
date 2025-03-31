package com.maya_yagan.sms.user.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.UserService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.Initializable;

/**
 * Controller class for managing the "Add User" form in the supermarket management system.
 * 
 * @author Maya Yagan
 */
public class AddUserController implements Initializable {

    @FXML
    private TextField firstNameField, lastNameField, emailField, phoneNumberField, tcNumberField, salaryField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private PasswordField passwordField;
    @FXML
    private MenuButton positionMenuButton;
    @FXML
    private CheckMenuItem managerCheckMenuItem;
    @FXML
    private CheckMenuItem accountantCheckMenuItem;
    @FXML
    private CheckMenuItem depotEmployeeCheckMenuItem;
    @FXML
    private CheckMenuItem cashierCheckMenuItem;
    @FXML
    private MenuButton employmentTypeMenu;
    @FXML
    private MenuItem partTimeMenuItem;
    @FXML
    private MenuItem fullTimeMenuItem;
    @FXML
    private MenuButton genderMenuButton;
    @FXML
    private MenuItem maleMenuItem;
    @FXML
    private MenuItem femaleMenuItem;

    private Runnable onCloseAction;
    private ModalPane modalPane;
    private String selectedEmploymentType;
    private String selectedGender;
    private final List<String> selectedPositions = new ArrayList<>();
    private final UserService userService = new UserService();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeGenderMenu();
        initializeEmploymentTypeMenu();
        initializePositionMenu();
        addFieldValidation();
    }
    
    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
    
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
    
    private void initializeGenderMenu(){
        maleMenuItem.setOnAction(e -> {
            selectedGender = "Male";
            genderMenuButton.setText(selectedGender);
        });
        femaleMenuItem.setOnAction(e ->{
            selectedGender = "Female";
            genderMenuButton.setText(selectedGender);
        });
    }
    
    private void initializeEmploymentTypeMenu(){
        partTimeMenuItem.setOnAction(e -> {
            selectedEmploymentType = "Part time";
            employmentTypeMenu.setText(selectedEmploymentType);
        });
        fullTimeMenuItem.setOnAction(e -> {
            selectedEmploymentType = "Full time";
            employmentTypeMenu.setText(selectedEmploymentType);
        });
    }
    
    private void initializePositionMenu(){
        managerCheckMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedPositions());
        accountantCheckMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedPositions());
        depotEmployeeCheckMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedPositions());
        cashierCheckMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedPositions());
    }
    
    private void updateSelectedPositions() {
        selectedPositions.clear();
        if (managerCheckMenuItem.isSelected()) selectedPositions.add("manager");
        if (accountantCheckMenuItem.isSelected()) selectedPositions.add("accountant");
        if (depotEmployeeCheckMenuItem.isSelected()) selectedPositions.add("warehouse worker");
        if (cashierCheckMenuItem.isSelected()) selectedPositions.add("cashier");

        String displayText = selectedPositions.isEmpty() ? "Select" : String.join(", ", selectedPositions);     
        positionMenuButton.setText(displayText);
    }
    
    private void addFieldValidation(){
        emailField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-z]{2,}$")) 
                emailField.setStyle("-fx-border-color: green;"); // Valid input
            else 
                emailField.setStyle("-fx-border-color: red;");   // Invalid input
        });
        salaryField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*"))
                salaryField.setText(oldValue);
        });
    }
    
    @FXML
    public void handleSave() {
        User newUser = userService.createUser(
            firstNameField.getText(),
            lastNameField.getText(),
            tcNumberField.getText(),
            birthDatePicker.getValue(),
            selectedGender,
            emailField.getText(),
            phoneNumberField.getText(),
            passwordField.getText(),
            salaryField.getText(),
            selectedPositions,
            "Part time".equals(selectedEmploymentType),
            "Full time".equals(selectedEmploymentType)
        );
        
        if(newUser != null){
            if(userService.addUser(newUser)){
                if(onCloseAction != null) onCloseAction.run();
                closeForm();
            }
        }
    }
    
    @FXML
    public void handleCancel() {
        closeForm();
    }
    
    private void closeForm() {
        if(modalPane != null) modalPane.hide();
    }
}
