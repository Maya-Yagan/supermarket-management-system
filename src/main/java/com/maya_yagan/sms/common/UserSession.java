package com.maya_yagan.sms.common;

import com.maya_yagan.sms.user.model.User;

public class UserSession {

    private static UserSession session = new UserSession();
    private User currentUser;

    private UserSession() {}
    public static UserSession getInstance() {
        if (session == null) {
            synchronized (UserSession.class) {
                session = new UserSession();
            }
        }
        return session;
    }
    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { currentUser = user; }
    public void clear() { currentUser = null; }
}
