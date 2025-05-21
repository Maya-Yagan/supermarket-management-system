package com.maya_yagan.sms.user.service;

import com.maya_yagan.sms.common.ValidationService;
import com.maya_yagan.sms.user.dao.AttendanceDAO;
import com.maya_yagan.sms.user.model.Attendance;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.DateUtil;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AttendanceService {
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ValidationService validationService = new ValidationService();
    private final UserService userService = new UserService();

    public Attendance getAttendanceForUserToday(User user){
        return attendanceDAO.getAttendanceForUserOnDate(user, LocalDate.now());
    }

    public Set<Attendance> getAttendances(User user, int year, int month){
        return attendanceDAO.getAttendancesForUserByYearAndMonth(user, year, month);
    }

    public long countAbsences(User user, int year, int month){
        return getAttendances(user, year, month)
                .stream().filter(Attendance::getAbsent)
                .count();
    }

    public Duration getTotalWorkingTime(User user, int year, int month){
        return getAttendances(user, year, month)
                .stream().filter(a -> a.getCheckOut() != null)
                .map(Attendance::getWorkingHours)
                .reduce(Duration.ZERO, Duration::plus);
    }

    public Set<Integer> getSelectableYears(User user) {
        int start = user.getStartDate().getYear();
        int current = LocalDate.now().getYear();
        return IntStream.rangeClosed(start, current)
                .boxed()
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }

    public Set<Integer> getSelectableMonths() {
        return IntStream.rangeClosed(1, 12)
                .boxed()
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }

    public Set<Attendance> getAttendancesForUserByYearAndMonth(User user, int year, int month) {
        return attendanceDAO.getAttendancesForUserByYearAndMonth(user, year, month);
    }

    public void updateAttendanceRecord(Attendance attendance, String notes, Boolean absent, String checkInStr, String checkOutStr){
        LocalTime in = null, out = null;
        if(!absent){
            in = validationService.parseAndValidateTime(checkInStr, "Check-In");
            if(checkOutStr != null && !checkOutStr.trim().equals("-") && !checkOutStr.trim().isEmpty())
                out = validationService.parseAndValidateTime(checkOutStr, "Check-Out");
        }
        validationService.validateAttendanceEdit(notes, absent, in, out);
        attendance.setNotes(notes);
        attendance.setAbsent(absent);
        attendance.setCheckIn(in);
        attendance.setCheckOut(out);
        attendanceDAO.updateAttendance(attendance);
    }

    public void checkIn(User user){
        Attendance today  = getAttendanceForUserToday(user);
        if(today != null && today.getCheckOut() != null){
            String when = DateUtil.formatTime(today.getCheckOut());
            throw new CustomException(
                    "You have already checked out today at " + when +
                            ". If this is a mistake, please contact your manager to reset it.",
                    "CHECK_OUT"
            );
        }
        if(today  != null && today.getCheckIn() == null){
            today.setAbsent(false);
            today.setCheckIn(LocalTime.now());
            today.setNotes("Checked in via login");
            attendanceDAO.updateAttendance(today);
            return;
        }

        if (today == null) {
            Attendance attendance = new Attendance();
            attendance.setUser(user);
            attendance.setDate(LocalDate.now());
            attendance.setAbsent(false);
            attendance.setCheckIn(LocalTime.now());
            attendance.setNotes("Checked in via login");
            attendanceDAO.insertAttendance(attendance);
        }

    }

    public void checkOut(User user){
        Attendance existing = getAttendanceForUserToday(user);
        existing.setCheckOut(LocalTime.now());
        attendanceDAO.updateAttendance(existing);
    }

    public void createBlankAttendancesForToday(){
        LocalDate today = LocalDate.now();
        List<User> all = userService.getAllUsers();
        for(User user : all){
            Attendance existing = getAttendanceForUserToday(user);
            if(existing == null){
                Attendance blank = new Attendance();
                blank.setUser(user);
                blank.setDate(today);
                blank.setAbsent(true);
                blank.setCheckIn(null);
                blank.setCheckOut(null);
                blank.setNotes("No check in yet.");
                attendanceDAO.insertAttendance(blank);
            }
        }
    }
}
