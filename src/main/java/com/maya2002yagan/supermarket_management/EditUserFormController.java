package com.maya2002yagan.supermarket_management;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author maya2
 */
public class EditUserFormController implements Initializable {
    
    @FXML
    private TextField firstNameField, lastNameField, emailField, phoneNumberField, tcNumberField, salaryField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private MenuButton genderMenuButton, employmentTypeMenu, positionMenuButton;
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
    private Button saveButton, cancelButton, deleteButton;
    @FXML
    private PasswordField passwordField;
    
    private String selectedEmploymentType;
    private String selectedGender;
    private List<Role> selectedPositions = new ArrayList<>();


    private User user;
    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeGenderMenu();
        initializeEmploymentTypeMenu();
        initializePositionMenu();
    }
    
    public void setUser(User user){
        this.user = user;
        populateFields();
    }
    
    @FXML
    private void handleDelete(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Are you sure you want to delete this user?");
        alert.setContentText("This action cannot be undone.");
        alert.showAndWait().ifPresent(response -> {
            if(response == ButtonType.OK){
                userDAO.deleteUser(user.getId());
                closeForm();
            }
        });
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
        managerCheckMenuItem.setOnAction(e -> toggleRoleSelection(managerCheckMenuItem, "Manager"));
        accountantCheckMenuItem.setOnAction(e -> toggleRoleSelection(accountantCheckMenuItem, "Accountant"));
        depotEmployeeCheckMenuItem.setOnAction(e -> toggleRoleSelection(depotEmployeeCheckMenuItem, "Depot Employee"));
        cashierCheckMenuItem.setOnAction(e -> toggleRoleSelection(cashierCheckMenuItem, "Cashier"));
    }
 
    private void toggleRoleSelection(CheckMenuItem menuItem, String roleName){
        Role role = roleDAO.getRoleByName(roleName);
        if(menuItem.isSelected()){
            if(!selectedPositions.contains(role)) selectedPositions.add(role);
        }
        else selectedPositions.remove(role);
    }
    
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
    
    private void setPositionMenuSelection(){
        managerCheckMenuItem.setSelected(selectedPositions.stream().anyMatch(r -> r.getPosition().equals("manager")));
        accountantCheckMenuItem.setSelected(selectedPositions.stream().anyMatch(r -> r.getPosition().equals("accountant")));
        depotEmployeeCheckMenuItem.setSelected(selectedPositions.stream().anyMatch(r -> r.getPosition().equals("depot_worker")));
        cashierCheckMenuItem.setSelected(selectedPositions.stream().anyMatch(r -> r.getPosition().equals("cashier")));
    }
    
    @FXML
    private void handleSave(){
        if(user == null) return;
        System.out.println("Selected Positions: " + selectedPositions);

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
         //user.setRoles(selectedPositions);
         
        if(!passwordField.getText().isEmpty())
            user.setPassword(passwordField.getText());
        userDAO.updateUser(user);
        closeForm();
    }
    
    @FXML
    private void handleCancel(){
        closeForm();
    }
    
    private void closeForm(){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
