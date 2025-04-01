package com.maya_yagan.sms.util;

import com.maya_yagan.sms.user.model.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Maya Yagan
 */
public class ValidationService {
    private static final String DATE_PATTERN = "dd.MM.yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    
    public boolean isValidEmail(String email) {
        return email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-z]{2,}$");
    }

    public boolean isValidSalary(String salary) {
        return salary.matches("\\d*\\.?\\d*");
    }
    
    public boolean isValidDate(String date){
        if(date == null || date.trim().isEmpty()) return false;
        try{
            LocalDate.parse(date, DATE_FORMATTER);
            return true;
        } catch(DateTimeParseException e){
            return false;
        }
    }
    
    public void validateUser(User user){
        if (user.getFirstName().isEmpty() || user.getLastName().isEmpty() ||
            user.getEmail().isEmpty() || user.getPhoneNumber().isEmpty() ||
            user.getPassword().isEmpty() || user.getTcNumber().isEmpty() ||
            user.getBirthDate() == null || user.getGender() == null ||
            user.getRoles().isEmpty() || 
           (user.getIsPartTime() == false && user.getIsFullTime() == false))
            throw new ExceptionUtil("Please fill all fields", "EMPTY_FIELDS");
        if (!isValidEmail(user.getEmail()))
            throw new ExceptionUtil("Please enter a valid email", "INVALID_EMAIL");
        if(!isValidDate(user.getBirthDate().format(DATE_FORMATTER))){
            throw new ExceptionUtil("Invalid date format.\nPlease follow this format: DD.MM.YYYY", "INVALID_DATE");
        }
    }
}
