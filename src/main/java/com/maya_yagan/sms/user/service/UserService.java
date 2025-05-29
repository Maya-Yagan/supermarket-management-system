package com.maya_yagan.sms.user.service;

import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.user.dao.RoleDAO;
import com.maya_yagan.sms.user.dao.UserDAO;
import com.maya_yagan.sms.user.model.Role;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.common.ValidationService;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Maya Yagan
 */
public class UserService {
    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private final ValidationService validationService = new ValidationService();
    
    public boolean createUser(String firstName, String lastName, String tcNumber, LocalDate birthDate, String gender,
                       String email, String phoneNumber, String password, String salaryText, 
                       Set<String> selectedPositions, boolean isPartTime, boolean isFullTime, int workHours) {
        float salary = validationService.parseAndValidateFloat(salaryText, "salary");
        Set<Role> roles = getRolesByNames(selectedPositions);
        User newUser = new User(firstName, lastName, tcNumber, birthDate, gender, email, phoneNumber, salary, password, roles, isPartTime, isFullTime);
        newUser.setWorkHours(workHours);
        validationService.validateUser(newUser);
        return addUser(newUser);
    }
    
    public void updateUserData(User user,
                                String firstName,
                                String lastName,
                                String email,
                                String phoneNumber,
                                String tcNumber,
                                String salaryText,
                                LocalDate birthDate,
                                String gender,
                                String employmentType,
                                Set<String> selectedPositions,
                                String password,
                               int workHours) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setTcNumber(tcNumber);
        user.setBirthDate(birthDate);
        user.setSalary(validationService.parseAndValidateFloat(salaryText, "salary"));
        user.setGender(gender);
        user.setWorkHours(workHours);
        if("Full time".equals(employmentType)){
            user.setIsFullTime(true);
            user.setIsPartTime(false);
        }
        else if("Part time".equals(employmentType)){
            user.setIsFullTime(false);
            user.setIsPartTime(true);
        }
        user.getRoles().clear();
        user.getRoles().addAll(getRolesByNames(selectedPositions));
        if(password != null && !password.isEmpty())
            user.setPasswordWithoutHashing(password);
        validationService.validateUser(user);
        updateUser(user);
    }
    
    public List<User> getAllUsers(){
        return userDAO.getUsers();
    }
    
    public boolean addUser(User user){
        return userDAO.insertUser(user);
    }
    
    public void updateUser(User user){
        userDAO.updateUser(user);
    }
    
    public void deleteUser(int id){
        userDAO.deleteUser(id);
    }
    
    public Set<Role> getRolesByNames(Set<String> roleNames){
        return roleNames.stream()
                .map(roleDAO::getRoleByName)
                .collect(Collectors.toSet());
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
    
    private int determinePrivilegeLevel(String roleName){
        return switch(roleName){
            case "cashier" -> 0;
            case "warehouse worker" -> 1;
            case "accountant" -> 2;
            case "manager" -> 3;
            default -> -1;
        };
    }

    public User getUserByEmail(String email){
        return userDAO.getUserByEmail(email);
    }

    public String getCurrentEmployeeName() {
        return UserSession.getInstance().getCurrentUser().getFullName();
    }
}
