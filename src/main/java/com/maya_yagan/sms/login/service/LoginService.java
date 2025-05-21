package com.maya_yagan.sms.login.service;

import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.AttendanceService;
import com.maya_yagan.sms.user.service.UserService;
import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.common.ValidationService;

public class LoginService {
    private final AttendanceService attendanceService = new AttendanceService();
    private final UserService userService = new UserService();
    private final ValidationService validationService = new ValidationService();

    public void login(String email, String password){
        validationService.validateLogin(email, password);
        User user = userService.getUserByEmail(email);
        UserSession.getInstance().setCurrentUser(user);
        attendanceService.checkIn(user);
    }

    public void logout(){
        UserSession session = UserSession.getInstance();
        User currentUser = session.getCurrentUser();
        if(currentUser == null) return;
        session.clear();
    }
}
