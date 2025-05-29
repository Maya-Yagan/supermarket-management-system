package com.maya_yagan.sms.homepage.service;

import atlantafx.base.theme.Styles;
import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.homepage.dao.NotificationDAO;
import com.maya_yagan.sms.homepage.model.Notification;
import com.maya_yagan.sms.login.service.AuthenticationService;
import com.maya_yagan.sms.login.service.LoginService;
import com.maya_yagan.sms.user.model.Attendance;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.AttendanceService;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.DateUtil;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

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

    public void notify(String message, boolean systemFlag, boolean isError){
        var icon = isError ? new FontIcon(Feather.X_CIRCLE) : new FontIcon(Feather.INFO);
        var styles = isError ? List.of(Styles.DANGER, Styles.ELEVATED_1)
                : List.of(Styles.ACCENT, Styles.ELEVATED_1);
        if(systemFlag){//there is no stack pane here
            //AlertUtil.showNotification(stackPane, message, icon, styles, javafx.util.Duration.seconds(4));
            notificationDAO.insertNotification(new Notification(message, null));
        }
        else{
            //AlertUtil.showNotification(stackPane, message, icon, styles, javafx.util.Duration.seconds(4));
            notificationDAO.insertNotification(new Notification(message, UserSession.getInstance().getCurrentUser()));
        }
    }

    public Set<Notification> getNotifications(){
        return notificationDAO.getNotifications();
    }

    public void deleteNotification(int id){
        notificationDAO.deleteNotification(id);
    }
}