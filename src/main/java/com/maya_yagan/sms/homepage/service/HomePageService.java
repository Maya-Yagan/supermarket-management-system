package com.maya_yagan.sms.homepage.service;

import com.maya_yagan.sms.homepage.dao.NotificationDAO;
import com.maya_yagan.sms.homepage.model.Notification;
import com.maya_yagan.sms.login.service.AuthenticationService;
import com.maya_yagan.sms.login.service.LoginService;
import com.maya_yagan.sms.user.model.Attendance;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.AttendanceService;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.DateUtil;

import java.time.Duration;
import java.time.LocalTime;

public class HomePageService {
    private final AttendanceService attendanceService = new AttendanceService();
    private final AuthenticationService authService = new AuthenticationService();
    private final LoginService loginService = new LoginService();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public Attendance getTodayAttendance(User user) {
        return attendanceService.getAttendanceForUserToday(user);
    }

    public String getElapsedTime(LocalTime checkIn) {
        if(checkIn == null) return "-";
        Duration elapsed = Duration.between(checkIn, LocalTime.now());
        return DateUtil.formatDuration(elapsed);
    }

    public void checkOut(User user, String password) throws CustomException {
        authService.authenticate(user, user.getEmail(), password);
        attendanceService.checkOut(user);
        loginService.logout();
    }

    public String getTimeUntilCheckout(Attendance attendance){
        if(attendance == null || attendance.getCheckIn() == null)
            return "-";

        Duration total = Duration.ofHours(attendance.getUser().getWorkHours());
        Duration elapsed = Duration.between(attendance.getCheckIn(), LocalTime.now());
        Duration left = total.minus(elapsed).isNegative() ? Duration.ZERO : total.minus(elapsed);
        if(!elapsed.minus(total).isNegative())
            return "You can check out now";
        else
            return DateUtil.formatDuration(left);
    }

    public void notify(String message){
        notificationDAO.insertNotification(new Notification(message));
    }
}
