package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import com.maya2002yagan.supermarket_management.model.Role;
import com.maya2002yagan.supermarket_management.dao.RoleDAO;
import com.maya2002yagan.supermarket_management.model.User;
import com.maya2002yagan.supermarket_management.dao.UserDAO;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.Initializable;

/**
 * Controller class for managing the "Add User" form in the supermarket management system.
 * Provides functionality for initializing form controls, handling user input, and
 * saving new user information to the database.
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
    @FXML
    private Label warningLabel;

    private Runnable onCloseAction;
    private ModalPane modalPane;
    private String selectedEmploymentType;
    private String selectedGender;
    private List<Role> selectedPositions = new ArrayList<>();

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    
    /**
     * Initializes the form by setting up gender, employment type, and position selection menus.
     *
     * @param location  The location of the FXML resource.
     * @param resources The resources to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeGenderMenu();
        initializeEmploymentTypeMenu();
        initializePositionMenu();
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
    
    /**
     * Sets the current modal pane.
     *
     * @param modalPane The modal pane to be set.
     */
    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
    
    /**
     * Sets the current close action.
     *
     * @param onCloseAction The close action to be set.
     */
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
    
    /**
     * Sets up event handlers for gender selection. Updates the displayed gender label 
     * based on the selected item.
     */
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
    
    /**
     * Configures employment type options as either "Part time" or "Full time."
     * Updates the employment type label when an option is selected.
     */
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
    
    /**
     * Initializes position selection checkboxes, adding listeners to track selected positions.
     */
    private void initializePositionMenu(){
        managerCheckMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedPositions());
        accountantCheckMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedPositions());
        depotEmployeeCheckMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedPositions());
        cashierCheckMenuItem.selectedProperty().addListener((obs, oldVal, newVal) -> updateSelectedPositions());
    }
    
    /**
     * Updates the list of selected roles based on the checked menu items.
     * Also updates the display text to show selected roles.
     */
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
    
    /**
     * Handles the action when the save button is clicked. Validates the user input,
     * creates a new User object, and saves it to the database if all fields are valid.
     * Updates the warning label in case of missing or invalid input.
     */
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
        float salary = Float.parseFloat(salaryField.getText());
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
            User newUser = new User(firstName, lastName, tcNumber, birthDate, gender, email, phoneNumber, salary, password, roles, isPartTime, isFullTime);
            userDAO.insertUser(newUser);
            if(onCloseAction != null) onCloseAction.run();
            closeForm();
        } catch (Exception e) {
            warningLabel.setText("Please fill all fields.");
            return;
        }
    }
    
    /**
     * Cancels the operation and closes the form without saving changes.
     */
    @FXML
    public void handleCancel() {
        closeForm();
    }
    
    /**
     * Closes the current form window.
     */
    private void closeForm() {
        if(modalPane != null) modalPane.hide();
    }
}
