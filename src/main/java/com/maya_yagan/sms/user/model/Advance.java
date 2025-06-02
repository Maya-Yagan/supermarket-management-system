package com.maya_yagan.sms.user.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "Advances")
public class Advance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private double totalAdvance;

    @Column(nullable = false)
    private double monthlyAdvance;

    public Advance() {}

    public Advance(User user, LocalDate startDate, double totalAdvance, double monthlyAdvance) {
        this.user = user;
        this.startDate = startDate;
        this.totalAdvance = totalAdvance;
        this.monthlyAdvance = monthlyAdvance;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public double getTotalAdvance() {
        return totalAdvance;
    }

    public void setTotalAdvance(double totalAdvance) {
        this.totalAdvance = totalAdvance;
    }

    public double getMonthlyAdvance() {
        return monthlyAdvance;
    }

    public void setMonthlyAdvance(double monthlyAdvance) {
        this.monthlyAdvance = monthlyAdvance;
    }

    public double getRemainingAdvance(LocalDate targetDate) {
        int monthsPassed = (int) ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), targetDate.withDayOfMonth(1));
        double remaining = totalAdvance - (monthlyAdvance * monthsPassed);
        return Math.max(0.0, remaining);
    }

    public double getMonthlyDeduction(LocalDate targetDate) {
        LocalDate firstMonth = startDate.withDayOfMonth(1);

        if (!targetDate.isAfter(firstMonth))
            return 0d;

        double balanceBefore = getRemainingAdvance(targetDate.minusMonths(1));

        if (balanceBefore <= 0)
            return 0d;

        return Math.min(monthlyAdvance, balanceBefore);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Advance advance = (Advance) o;
        return Objects.equals(id, advance.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
