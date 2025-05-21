package com.maya_yagan.sms.user.controller;

import com.maya_yagan.sms.util.ExceptionHandler;
import com.maya_yagan.sms.util.CustomException;
import javafx.fxml.FXML;

/**
 * Controller class for adding a User in the supermarket management system.
 * 
 * @author Maya Yagan
 */
public class AddUserController extends BaseUserController {
    
    @FXML 
    @Override
    public void handleSave() {
        try{
            if(userService.createUser(
               firstNameField.getText(),
                lastNameField.getText(),
                tcNumberField.getText(),
               birthDatePicker.getValue(),
                 selectedGender,
                  emailField.getText(),
              phoneNumberField.getText(),
                passwordField.getText(),
              salaryField.getText(), selectedPositions,
              "Part time".equals(selectedEmploymentType),
              "Full time".equals(selectedEmploymentType),
                selectedWorkHours))
            {
                if(onCloseAction != null) onCloseAction.run();
                close();
            }
        } catch(CustomException e){
            ExceptionHandler.handleException(e);
        }
    }
}
