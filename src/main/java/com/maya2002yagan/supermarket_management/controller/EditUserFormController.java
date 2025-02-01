package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import com.maya2002yagan.supermarket_management.model.Role;
import com.maya2002yagan.supermarket_management.dao.RoleDAO;
import com.maya2002yagan.supermarket_management.model.User;
import com.maya2002yagan.supermarket_management.dao.UserDAO;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;

/**
 * Controller class for the "Edit User" form in the supermarket management system.
 * Handles loading existing user information, editing user details, and saving changes.
 * Provides functionality for setting user fields, managing position and employment options,
 * and saving or deleting user information in the database.
 * 
 * @author Maya Yagan
 */
public class EditUserFormController implements Initializable {
    
    @FXML
    private TextField firstNameField, lastNameField, emailField, phoneNumberField, tcNumberField, salaryField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private MenuButton genderMenuButton, employmentTypeMenu;
    @FXML
    private MenuItem maleMenuItem, femaleMenuItem, partTimeMenuItem, fullTimeMenuItem;
    @FXML
    private CheckMenuItem managerCheckMenuItem;
    @FXML
    private CheckMenuItem accountantCheckMenuItem;
    @FXML
    private CheckMenuItem depotEmployeeCheckMenuItem;
    @FXML
    private CheckMenuItem cashierCheckMenuItem;
    @FXML
    private PasswordField passwordField;
    
    private Runnable onCloseAction;
    private ModalPane modalPane;
    private String selectedEmploymentType;
    private String selectedGender;
    private List<Role> selectedPositions = new ArrayList<>();

    private User user;
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
     * Sets the current user to be edited and populates form fields with the user's data.
     *
     * @param user The user whose information is to be loaded and edited.
     */
    public void setUser(User user){
        this.user = user;
        populateFields();
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
     * Initializes the position menu, setting up checkboxes for different roles
     * and updating the selected positions list based on user choices.
     */
    private void initializePositionMenu(){
        managerCheckMenuItem.setOnAction(e -> toggleRoleSelection(managerCheckMenuItem, "Manager"));
        accountantCheckMenuItem.setOnAction(e -> toggleRoleSelection(accountantCheckMenuItem, "Accountant"));
        depotEmployeeCheckMenuItem.setOnAction(e -> toggleRoleSelection(depotEmployeeCheckMenuItem, "Depot Employee"));
        cashierCheckMenuItem.setOnAction(e -> toggleRoleSelection(cashierCheckMenuItem, "Cashier"));
    }
 
    /**
     * Toggles the selection of a role in the selected positions list based on the 
     * checkbox state. Adds or removes the role depending on whether the checkbox is selected.
     *
     * @param menuItem The checkbox item corresponding to a role.
     * @param roleName The name of the role associated with the checkbox.
     */
    private void toggleRoleSelection(CheckMenuItem menuItem, String roleName){
        Role role = roleDAO.getRoleByName(roleName);
        if(menuItem.isSelected()){
            if(!selectedPositions.contains(role)) selectedPositions.add(role);
        }
        else selectedPositions.remove(role);
    }
    
    /**
     * Populates the form fields with the current user's data, including personal
     * information, gender, employment type, and roles.
     */
    private void populateFields(){
        if(user == null) return;
        
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        phoneNumberField.setText(user.getPhoneNumber());
        tcNumberField.setText(user.getTcNumber());
        salaryField.setText(String.valueOf(user.getSalary()));
        birthDatePicker.setValue(user.getBirthDate());
        
        //Gender
        selectedGender = user.getGender();
        genderMenuButton.setText(selectedGender);
        
         // Employment type
        selectedEmploymentType = user.getEmploymentType();
        employmentTypeMenu.setText(selectedEmploymentType);

        // Positions
        selectedPositions = new ArrayList<>(user.getRoles());
        setPositionMenuSelection();
    }
    
    /**
     * Updates the position menu checkboxes based on the roles associated with the current user.
     */
    private void setPositionMenuSelection(){
        managerCheckMenuItem.setSelected(selectedPositions.stream().anyMatch(r -> r.getPosition().equals("manager")));
        accountantCheckMenuItem.setSelected(selectedPositions.stream().anyMatch(r -> r.getPosition().equals("accountant")));
        depotEmployeeCheckMenuItem.setSelected(selectedPositions.stream().anyMatch(r -> r.getPosition().equals("depot_worker")));
        cashierCheckMenuItem.setSelected(selectedPositions.stream().anyMatch(r -> r.getPosition().equals("cashier")));
    }
    
    /**
     * Saves the edited user information to the database. Updates the user's personal details,
     * employment type, roles, and password if specified. Closes the form upon successful save.
     */
    @FXML
    private void handleSave(){
        if(user == null) return;

        user.setFirstName(firstNameField.getText());
        user.setLastName(lastNameField.getText());
        user.setEmail(emailField.getText());
        user.setPhoneNumber(phoneNumberField.getText());
        user.setTcNumber(tcNumberField.getText());
        user.setSalary(Float.parseFloat(salaryField.getText()));
        user.setBirthDate(birthDatePicker.getValue());
        user.setGender(selectedGender);
        if ("Full time".equals(selectedEmploymentType)) {
            user.setIsFullTime(true);
            user.setIsPartTime(false);
        } else if ("Part time".equals(selectedEmploymentType)) {
            user.setIsFullTime(false);
            user.setIsPartTime(true);
        }
         user.getRoles().clear();
         user.getRoles().addAll(selectedPositions);
         
        if(!passwordField.getText().isEmpty())
            user.setPasswordWithoutHashing(passwordField.getText());
        userDAO.updateUser(user);
        if(onCloseAction != null) onCloseAction.run();
        closeForm();
    }
    
    /**
     * Handles the deletion of the current user by confirming the action with the user,
     * and if confirmed, deletes the user record from the database.
     */
    @FXML
    private void handleDelete(){
        ShowAlert.showDeleteConfirmation(user,
                "Delete User",
                "Are you sure you want to delete this user?",
                "This action cannot be undone.",
                (User u) -> {
                    userDAO.deleteUser(u.getId());
                    if(onCloseAction != null) onCloseAction.run();
                    closeForm();
                });
    }
    
    /**
     * Cancels the operation and closes the form without saving changes.
     */
    @FXML
    private void handleCancel(){
        closeForm();
    }
    
    /**
     * Closes the current form window.
     */
    private void closeForm(){
        if(modalPane != null) modalPane.hide();
    }
}
