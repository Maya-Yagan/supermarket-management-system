package com.maya2002yagan.supermarket_management;

public class AuthenticationService {
    private String userPassword;
    private String userEmail;

    public AuthenticationService(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword; // This is the plain text password provided by the user
    }
    
    public boolean authenticate(User user){
        if(user == null)
            return false;
        return user.getEmail().equals(userEmail) && user.checkPassword(userPassword);
    }
}
