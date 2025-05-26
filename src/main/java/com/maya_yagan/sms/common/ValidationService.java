package com.maya_yagan.sms.common;

import com.maya_yagan.sms.login.service.AuthenticationService;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.settings.model.Settings;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.UserService;
import com.maya_yagan.sms.util.CustomException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 *
 * @author Maya Yagan
 */
public class ValidationService {
    private static final String DATE_PATTERN = "dd.MM.yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final String TIME_PATTERN = "^([01]\\d|2[0-3]):[0-5]\\d$";

    public boolean isValidEmail(String email) {
        return email.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-z]{2,}$");
    }

    public boolean isValidSalary(String salary) {
        return salary.matches("\\d*\\.?\\d*");
    }

    public boolean isValidMoney(String money) {
        return money.matches("\\d*(\\.\\d{0,2})?");
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
        if(product == null ||
            product.getName().isEmpty() ||
            product.getPrice() == 0 ||
            product.getProductionDate() == null ||
            product.getCategory() == null ||
            product.getUnit() == null ||
            product.getBarcode() == null ||
            product.getBarcode().trim().isEmpty() ||
            product.getMinLimit() == 0
        )
            throw new CustomException("Please fill all fields", "EMPTY_FIELDS");

        if (!isValidDate(product.getProductionDate().format(DATE_FORMATTER)))
            throw new CustomException(
                    "Invalid date format.\nPlease follow this format: DD.MM.YYYY",
                    "INVALID_DATE");

        LocalDate expiry = product.getExpirationDate();
        if(expiry != null){
            if(!isValidDate(expiry.format(DATE_FORMATTER)))
                throw new CustomException(
                        "Invalid date format.\nPlease follow this format: DD.MM.YYYY",
                        "INVALID_DATE");

            if(expiry.isBefore(product.getProductionDate()))
                throw new CustomException(
                        "Invalid Expiration date.\nExpiration date can't be before production date",
                        "INVALID_DATE");

            if(product.getTaxPercentage() < 0)
                throw new CustomException("The tax value cannot be negative.", "INVALID_NUMBER");
        }
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
            if(number < 0)
                throw new CustomException("The entered number must be greater than zero.", "INVALID_NUMBER");
            else return number;
        } catch(NumberFormatException e){
            throw new CustomException("Invalid " + fieldName + " format.\nPlease Enter a valid" + fieldName, "INVALID_NUMBER");
        }
    }

    public int parseAndValidateInt(String input, String fieldName){
        try{
            int number = Integer.parseInt(input.trim());
            if(number < 0)
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

    public void validateAttendanceEdit(String notes, Boolean isAbsent, LocalTime in, LocalTime out){
        if(notes == null || notes.trim().isEmpty())
            throw new CustomException("Please fill empty fields", "EMPTY_FIELDS");
        if (!isAbsent && (in == null))
            throw new CustomException("Check-in time is required if not marked absent", "INVALID_FIELD");
        if(in != null && out != null && out.isBefore(in))
            throw new CustomException("Check‑out time (" + out + ") cannot be before check‑in time (" + in + ")", "INVALID_FORMAT");
    }

    public LocalTime parseAndValidateTime(String input, String fieldName) {
        if (input == null || input.isBlank()) {
            throw new CustomException(fieldName + " is required", "EMPTY_FIELDS");
        }
        if (!input.matches(TIME_PATTERN)) {
            throw new CustomException(
                    "Invalid " + fieldName + " format. Please use HH:mm",
                    "INVALID_FORMAT"
            );
        }
        return LocalTime.parse(input);
    }

    public void validateSettings(Settings settings){
        if(settings.getMoneyUnit() == null || settings.getAddress() == null
        || settings.getPhone() == null || settings.getMarketName() == null
        || settings.getMoneyUnit().trim().isEmpty() || settings.getAddress().trim().isEmpty()
        || settings.getPhone().trim().isEmpty() || settings.getMarketName().trim().isEmpty())
            throw new CustomException("Please fill all empty fields", "EMPTY_FIELDS");
    }
}