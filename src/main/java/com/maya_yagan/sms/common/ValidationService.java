package com.maya_yagan.sms.util;

import com.maya_yagan.sms.login.AuthenticationService;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.UserService;

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
    
    public void validateSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().trim().isEmpty() ||
            supplier.getEmail() == null || supplier.getEmail().trim().isEmpty() ||
            supplier.getPhoneNumber() == null || supplier.getPhoneNumber().trim().isEmpty()) {
            throw new CustomException("Please fill all fields", "EMPTY_FIELDS");
        }
        
        if (!isValidEmail(supplier.getEmail())) {
            throw new CustomException("Please enter a valid email", "INVALID_EMAIL");
        }

        if (!supplier.getPhoneNumber().matches("\\d{10,15}")) {
            throw new CustomException("Invalid phone number", "INVALID_PHONE");
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

    public void validateWarehouse(String name, int newCapacity, int totalProducts){
        if(newCapacity <= 0)
            throw new CustomException("Please enter a valid number.", "INVALID_NUMBER");
        if(totalProducts > newCapacity)
            throw new CustomException("The new capacity cannot be less than the current stock.", "INVALID_CAPACITY");
        if(name.isEmpty())
            throw new CustomException("PLease fill all fields", "EMPTY_FIELDS");
    }

    public void validateWarehouse(String name, int capacity){
        if(name.isEmpty())
            throw new CustomException("PLease fill all fields", "EMPTY_FIELDS");
        if(capacity <= 0)
            throw new CustomException("Please enter a valid number.", "INVALID_NUMBER");
    }
    
    public float parseAndValidateFloat(String input, String fieldName){
        try{
            float number = Float.parseFloat(input.trim());
            if(number <= 0)
                throw new CustomException("The entered number must be greater than zero.", "INVALID_NUMBER");
            else return number;
        } catch(NumberFormatException e){
            throw new CustomException("Invalid " + fieldName + " format.\nPlease Enter a valid" + fieldName, "INVALID_NUMBER");
        }
    }

    public int parseAndValidateInt(String input, String fieldName){
        try{
            int number = Integer.parseInt(input.trim());
            if(number <= 0)
                throw new CustomException("The entered number must be greater than zero.", "INVALID_NUMBER");
            else return number;
        } catch(NumberFormatException e){
            throw new CustomException("Invalid " + fieldName + " format.\nPlease Enter a valid " + fieldName, "INVALID_NUMBER");
        }
    }
    
    public void validateCategory(Category category){
        if(category.getName() == null || category.getName().trim().isEmpty())
            throw new CustomException("Please fill the name", "EMPTY_FIELDS");
    }

    public void validateStockAmount(int newAmount, int remainingCapacity){
        if (newAmount < 0)
            throw new CustomException(
                    "The amount cannot be negative.",
                    "INVALID_NUMBER");

        if (remainingCapacity == 0)
            throw new CustomException(
                    "The warehouse is already full. No more products can be added.",
                    "INVALID_CAPACITY");

        if (newAmount > remainingCapacity)
            throw new CustomException(
                    "The entered amount exceeds the remaining warehouse capacity. "
                            + "Remaining capacity: " + remainingCapacity,
                    "INVALID_CAPACITY");
    }

    public void validateOrder(Order order){
        if(order.getName().isEmpty() || order.getOrderDate() == null)
            throw new CustomException("Please fill all fields", "EMPTY_FIELDS");
        if(!isValidDate(order.getOrderDate().format(DATE_FORMATTER)))
            throw new CustomException("Invalid date format.\nPlease follow this format: DD.MM.YYYY", "INVALID_DATE");
    }

    public void validateLogin(String email, String password){
        AuthenticationService auth = new AuthenticationService();
        if(email == null || email.isBlank() || password == null || password.isBlank())
            throw new CustomException("Please fill all fields", "EMPTY_FIELDS");
        UserService userService = new UserService();
        User user = userService.getUserByEmail(email);
        auth.authenticate(user, email, password);
    }
}
