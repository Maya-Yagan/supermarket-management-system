package com.maya_yagan.sms.user.model;

import com.maya_yagan.sms.util.DateUtil;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "Attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "attendance_date", nullable = false)
    private LocalDate date;
    @Column(name = "absent", nullable = false)
    private boolean absent;
    @Column(name = "check_in_time", columnDefinition = "TIME", nullable = true)
    private LocalTime checkIn;
    @Column(name = "check_out_time", columnDefinition = "TIME", nullable = true)
    private LocalTime checkOut;
    @Column(name = "overtime_hours")
    private Duration overtimeHours;
    @Column(name = "notes")
    private String notes;

    public Attendance() {}

    public Duration getWorkingHours() {
        if (checkIn != null && checkOut != null)
            return Duration.between(checkIn, checkOut);
        return Duration.ZERO;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean getAbsent() {
        return absent;
    }

    public void setAbsent(boolean absent) {
        this.absent = absent;
    }

    public LocalTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalTime checkOut) {
        this.checkOut = checkOut;
    }

    public Duration getOvertime() {
        Duration worked = getWorkingHours();
        Duration standard = Duration.ofHours(user.getWorkHours());
        return worked.minus(standard).isNegative()
                ? Duration.ZERO
                : worked.minus(standard);
    }

    public String getFormattedCheckOut() {
        return DateUtil.formatTimeOrDefault(checkOut, "Didn't check out");
    }

    public String getFormattedWorkingHours() {
        if (checkOut == null) {
            return "Didn't check out";
        }
        return DateUtil.formatDuration(getWorkingHours());
    }

    public String getFormattedOvertime() {
        if (checkOut == null) {
            return "Didn't check out";
        }
        return DateUtil.formatDuration(getOvertime());
    }

    public void setOvertimeHours(Duration overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "user=" + user +
                ", absent=" + absent +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Attendance that = (Attendance) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
