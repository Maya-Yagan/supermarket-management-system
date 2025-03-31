package com.maya_yagan.sms.login;

import com.maya_yagan.sms.user.model.User;

/**
 * Service class responsible for authenticating users based on their email and password.
 * It compares provided credentials with those of an existing User instance.
 * 
 * @author Maya Yagan
 */
public class AuthenticationService {
    private String userPassword;
    private String userEmail;

    /**
     * Constructs an AuthenticationService instance with the specified email and password.
     * 
     * @param userEmail    The email of the user attempting to authenticate.
     * @param userPassword The password of the user attempting to authenticate.
     */
    public AuthenticationService(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }
    
    /**
     * Authenticates the given user by verifying that the provided email and password
     * match the credentials stored in the User object.
     * 
     * @param user The user to be authenticated.
     * @return {@code true} if the user's email and password match the provided credentials;
     *         {@code false} if the user is null or the credentials do not match.
     */
    public boolean authenticate(User user){
        if(user == null)
            return false;
        return user.getEmail().equals(userEmail) && user.checkPassword(userPassword);
    }
}
