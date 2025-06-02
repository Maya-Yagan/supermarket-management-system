package com.maya_yagan.sms.user.dao;

import java.math.BigDecimal;

public class SalaryDetailsDTO {
    private int totalDays;
    private BigDecimal salaryPerDay;
    private BigDecimal totalAdvance;
    private BigDecimal monthlyAdvance;
    private BigDecimal remainingAdvance;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;

    public SalaryDetailsDTO(int totalDays, BigDecimal salaryPerDay, BigDecimal totalAdvance, BigDecimal monthlyAdvance, BigDecimal remainingAdvance, BigDecimal grossSalary, BigDecimal netSalary) {
        this.totalDays = totalDays;
        this.salaryPerDay = salaryPerDay;
        this.totalAdvance = totalAdvance;
        this.monthlyAdvance = monthlyAdvance;
        this.remainingAdvance = remainingAdvance;
        this.grossSalary = grossSalary;
        this.netSalary = netSalary;
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