package com.maya_yagan.sms.util;

import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.Product;
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
    
    public boolean isValidExpirationDate(LocalDate expiryDate, LocalDate productionDate){
        return expiryDate != null && !expiryDate.isAfter(productionDate); 
    }
    
    public void validateUser(User user){
        if (user.getFirstName().isEmpty() || user.getLastName().isEmpty() ||
            user.getEmail().isEmpty() || user.getPhoneNumber().isEmpty() ||
            user.getPassword().isEmpty() || user.getTcNumber().isEmpty() ||
            user.getBirthDate() == null || user.getGender() == null ||
            user.getRoles().isEmpty() || 
           (!user.getIsPartTime() && !user.getIsFullTime()))
            throw new CustomException("Please fill all fields", "EMPTY_FIELDS");
        if (!isValidEmail(user.getEmail()))
            throw new CustomException("Please enter a valid email", "INVALID_EMAIL");
        if(!isValidDate(user.getBirthDate().format(DATE_FORMATTER))){
            throw new CustomException("Invalid date format.\nPlease follow this format: DD.MM.YYYY", "INVALID_DATE");
        }
    }
    
    public void validateProduct(Product product){
        if(product.getName().isEmpty() || product.getPrice() == 0 || 
           product.getProductionDate() == null || 
           product.getCategory() == null || product.getUnit() == null)
            throw new CustomException("Please fill all fields", "EMPTY_FIELDS");
        if(isValidExpirationDate(product.getExpirationDate(), product.getProductionDate()))
            throw new CustomException("Invalid Expiration date.\nExpiration date can't be before production date", "INVALID_DATE");
        if(!isValidDate(product.getExpirationDate().format(DATE_FORMATTER)) || !isValidDate(product.getProductionDate().format(DATE_FORMATTER)))
            throw new CustomException("Invalid date format.\nPlease follow this format: DD.MM.YYYY", "INVALID_DATE");
    }
    
    public float parseAndValidateFloat(String input, String fieldName){
        try{
            return Float.parseFloat(input);
        } catch(NumberFormatException e){
            throw new CustomException("Invalid " + fieldName + " format.\nPlease Enter a valid " + fieldName, "INVALID_FLOAT");
        }
    }
    
    public void validateCategory(Category category){
        if(category.getName() == null || category.getName().trim().isEmpty())
            throw new CustomException("Please fill the name", "EMPTY_FIELDS");
    }
}
