package com.maya_yagan.sms.user.controller;

import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.ExceptionHandler;
import com.maya_yagan.sms.util.CustomException;
import javafx.fxml.FXML;

/**
 * Controller class to edit the User in the supermarket management system.
 * 
 * @author Maya Yagan
 */
public class EditUserController extends BaseUserController {
    
    private User user;

    public void setUser(User user) {
        this.user = user;
        populateFields();
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
        selectedGender = user.getGender();
        genderMenuButton.setText(selectedGender);
        selectedEmploymentType = user.getEmploymentType();
        employmentTypeMenu.setText(selectedEmploymentType);
        selectedPositions.clear();
        user.getRoles().forEach(role -> selectedPositions.add(role.getName()));
        setPositionMenuSelection();
        selectedWorkHours = user.getWorkHours();
        workHoursMenubutton.setText(String.valueOf(selectedWorkHours));
    }
  
    @FXML 
    @Override
    protected void handleSave(){
        if(user == null) return;
        try{
            userService.updateUserData(user, 
                firstNameField.getText(), 
                lastNameField.getText(), 
                emailField.getText(), 
                phoneNumberField.getText(),
                tcNumberField.getText(),
                salaryField.getText(),
                birthDatePicker.getValue(),
                selectedGender,
                selectedEmploymentType,
                selectedPositions,
                passwordField.getText(),
                selectedWorkHours
            );
            
            if(onCloseAction != null) onCloseAction.run();
            close();
        } catch(CustomException e){
            ExceptionHandler.handleException(e);
        }        
    }
}
