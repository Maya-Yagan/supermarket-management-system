package com.maya_yagan.sms.login.service;

import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.CustomException;

/**
 * Service class responsible for authenticating users based on their email and password.
 * 
 * @author Maya Yagan
 */
public class AuthenticationService {
    public void authenticate(User user, String email, String password){
        if(email == null || email.isBlank())
            throw new CustomException("Email must not be empty.", "INVALID_FIELD");
        if(password == null || password.isBlank())
            throw new CustomException("Password must not be empty.", "INVALID_FIELD");
        if (user == null)
            throw new CustomException("No user found with this email.", "NOT_FOUND");
        if(!user.getEmail().equals(email))
            throw new CustomException("Wrong Email.\nPlease check and try again.", "INVALID_FIELD");
        if(!user.checkPassword(password))
            throw new CustomException("Wrong password.\nPlease check and try again.", "INVALID_FIELD");
    }
}
