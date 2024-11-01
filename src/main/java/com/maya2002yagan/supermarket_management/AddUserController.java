package com.maya2002yagan.supermarket_management;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddUserController {

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
    private Button saveButton;
    @FXML
    private MenuButton genderMenuButton;
    @FXML
    private MenuItem maleMenuItem;
    @FXML
    private MenuItem femaleMenuItem;
    @FXML
    private Label warningLabel;

    private String selectedEmploymentType;
    private String selectedGender;
    private List<Role> selectedPositions = new ArrayList<>();

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    public AddUserController() {}
    
    @FXML
    public void initialize() {
        initializeGenderMenu();
        initializeEmploymentTypeMenu();
        initializePositionMenu();
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
        selectedPositions.clear(); // Clear previous selections
        if (managerCheckMenuItem.isSelected()) selectedPositions.add(roleDAO.getRoleByName("manager"));
        if (accountantCheckMenuItem.isSelected()) selectedPositions.add(roleDAO.getRoleByName("accountant"));
        if (depotEmployeeCheckMenuItem.isSelected()) selectedPositions.add(roleDAO.getRoleByName("depot_worker"));
        if (cashierCheckMenuItem.isSelected()) selectedPositions.add(roleDAO.getRoleByName("cashier"));

        positionMenuButton.setText(selectedPositions.stream()
        .map(Role::getPosition)
        .reduce((first, second) -> first + ", " + second)
        .orElse("Select"));
    }
    
    @FXML
    public void handleSave() {
        warningLabel.setText("");

        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();
        String password = passwordField.getText();
        String tcNumber = tcNumberField.getText();
        LocalDate birthDate = birthDatePicker.getValue();
        String gender = selectedGender;
        List<Role> roles = selectedPositions;
        float salary;
        try {
            salary = Float.parseFloat(salaryField.getText());
        } catch (NumberFormatException e) {
            warningLabel.setText("Salary must be a valid number.");
            return;
        }
        try {
             if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                phoneNumber.isEmpty() || password.isEmpty() || tcNumber.isEmpty() ||
                birthDate == null || (selectedGender == null || selectedGender.isEmpty()) ||
                selectedPositions.isEmpty()) {
                warningLabel.setText("Please fill all fields.");
                return; 
            }
            boolean isPartTime = selectedEmploymentType.equals("Part time");
            boolean isFullTime = selectedEmploymentType.equals("Full time");
            User newUser = new User(firstName, lastName, tcNumber, birthDate, gender, email, phoneNumber, salary, hashPassword(password), roles, isPartTime, isFullTime);
            userDAO.insertUser(newUser);
            closeForm();
        } catch (Exception e) {
            warningLabel.setText("Please fill all fields.");
            return;
        }
    }
    
    @FXML
    public void handleCancel() {
        closeForm();
    }
    
    private void closeForm() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
