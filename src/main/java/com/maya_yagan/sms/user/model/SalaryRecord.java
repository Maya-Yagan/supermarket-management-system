package com.maya_yagan.sms.user.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Salary_Records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","year","month"}))
public class SalaryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false) private int year;
    @Column(nullable = false) private int month;

    @Column(nullable = false) private int totalDays;
    @Column(nullable = false) private BigDecimal salaryPerDay;
    @Column(nullable = false) private BigDecimal totalAdvance;
    @Column(nullable = false) private BigDecimal monthlyAdvance;
    @Column(nullable = false) private BigDecimal remainingAdvance;
    @Column(nullable = false) private BigDecimal grossSalary;
    @Column(nullable = false) private BigDecimal netSalary;

    public SalaryRecord(){}

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public BigDecimal getSalaryPerDay() {
        return salaryPerDay;
    }

    public void setSalaryPerDay(BigDecimal salaryPerDay) {
        this.salaryPerDay = salaryPerDay;
    }

    public BigDecimal getTotalAdvance() {
        return totalAdvance;
    }

    public void setTotalAdvance(BigDecimal totalAdvance) {
        this.totalAdvance = totalAdvance;
    }

    public BigDecimal getMonthlyAdvance() {
        return monthlyAdvance;
    }

    public void setMonthlyAdvance(BigDecimal monthlyAdvance) {
        this.monthlyAdvance = monthlyAdvance;
    }

    public BigDecimal getRemainingAdvance() {
        return remainingAdvance;
    }

    public void setRemainingAdvance(BigDecimal remainingAdvance) {
        this.remainingAdvance = remainingAdvance;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }
}
