package com.maya_yagan.sms.user.service;

import com.maya_yagan.sms.user.dao.RoleDAO;
import com.maya_yagan.sms.user.dao.UserDAO;
import com.maya_yagan.sms.user.model.Role;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.ShowAlert;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;

/**
 *
 * @author Maya Yagan
 */
public class UserService {
    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    
    public User createUser(String firstName, String lastName, String tcNumber, LocalDate birthDate, String gender,
                       String email, String phoneNumber, String password, String salaryText, 
                       List<String> selectedPositions, boolean isPartTime, boolean isFullTime) {
        float salary = parseSalary(salaryText);
        if(salary < 0) return null;
        
        List<Role> roles = getRolesByNames(selectedPositions);
        User newUser = new User(firstName, lastName, tcNumber, birthDate, gender, email, phoneNumber, salary, password, roles, isPartTime, isFullTime);
        
        if(!validateUser(newUser)) return null;
        return newUser;
    }
    
    public List<User> getAllUsers(){
        return userDAO.getUsers();
    }
    
    public boolean addUser(User user){
        return userDAO.insertUser(user);
    }
    
    public boolean validateUser(User user){
        if (user.getFirstName().isEmpty() || user.getLastName().isEmpty() ||
            user.getEmail().isEmpty() || user.getPhoneNumber().isEmpty() ||
            user.getPassword().isEmpty() || user.getTcNumber().isEmpty() ||
            user.getBirthDate() == null || user.getGender() == null ||
            user.getRoles().isEmpty()){
            ShowAlert.showAlert(Alert.AlertType.WARNING,
                    "Empty Field Warning",
                   "Please fill all fields.");
            return false;
        }
        if (!user.getEmail().matches("^[\\w.%+-]+@[\\w.-]+\\.[a-z]{2,}$")){ 
            ShowAlert.showAlert(Alert.AlertType.WARNING,
                    "Invalid Email",
                   "Please enter valid email format");
            return false;
        }
        return true;
    }
    
    public List<Role> getRolesByNames(List<String> roleNames){
        return roleNames.stream()
                .map(roleDAO::getRoleByName)
                .collect(Collectors.toList());
    }
    
    public void initializeRole(){
        List<String> predefinedRoles = List.of("cashier", "warehouse worker", "accountant", "manager");
        for(String roleName : predefinedRoles){
            if(roleDAO.getRoleByName(roleName) == null){
                Role newRole = new Role();
                newRole.setName(roleName);
                newRole.setPrivilegeLevel(determinePrivilegeLevel(roleName));
                roleDAO.insertRole(newRole);
            }
        }
    }
    
    private float parseSalary(String salaryText){
        try{
            return Float.parseFloat(salaryText);
        } catch(NumberFormatException e){
            ShowAlert.showAlert(Alert.AlertType.WARNING,
                    "Invalid Salary",
                   "Please enter a valid number for salary.");
            return -1f;
        }
    }
    
    private int determinePrivilegeLevel(String roleName){
        return switch(roleName){
            case "cashier" -> 0;
            case "warehouse worker" -> 1;
            case "accountant" -> 2;
            case "manager" -> 3;
            default -> -1;
        };
    }
}
