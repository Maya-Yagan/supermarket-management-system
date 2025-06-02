package com.maya_yagan.sms.user.service;

import com.maya_yagan.sms.finance.model.TransactionType;
import com.maya_yagan.sms.finance.service.CashBoxService;
import com.maya_yagan.sms.user.dao.AdvanceDAO;
import com.maya_yagan.sms.user.dao.SalaryDetailsDTO;
import com.maya_yagan.sms.user.dao.SalaryRecordDAO;
import com.maya_yagan.sms.user.model.Advance;
import com.maya_yagan.sms.user.model.SalaryRecord;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.CustomException;
import javafx.scene.control.Alert;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AdvanceService {

    private final AdvanceDAO advanceDAO = new AdvanceDAO();
    private final CashBoxService cashBoxService = new CashBoxService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final SalaryRecordDAO salaryRecordDAO = new SalaryRecordDAO();

    public SalaryDetailsDTO buildSalaryDetails(User user,
                                               int year,
                                               int month,
                                               int totalDays) {

        BigDecimal salaryPerDay = BigDecimal.valueOf(user.getSalary());
        BigDecimal grossSalary  = salaryPerDay.multiply(BigDecimal.valueOf(totalDays));

        // Defaults when the employee has never taken an advance
        BigDecimal totalAdvance     = BigDecimal.ZERO;
        BigDecimal monthlyAdvance   = BigDecimal.ZERO;
        BigDecimal remainingAdvance = BigDecimal.ZERO;
        BigDecimal netSalary        = grossSalary;

        Advance latest = advanceDAO.getLatestAdvanceForUser(user);
        if (latest != null) {
            LocalDate targetDate = LocalDate.of(year, month, 1);
            LocalDate firstMonth = latest.getStartDate().withDayOfMonth(1);
            boolean isAdvanceGrantMonth = targetDate.equals(firstMonth);
            double deduction = latest.getMonthlyDeduction(targetDate);
            double remaining = latest.getRemainingAdvance(targetDate);

            if (isAdvanceGrantMonth || deduction > 0) {
                totalAdvance     = BigDecimal.valueOf(latest.getTotalAdvance());
                monthlyAdvance   = BigDecimal.valueOf(latest.getMonthlyAdvance());
                remainingAdvance = BigDecimal.valueOf(remaining);

                if (isAdvanceGrantMonth) {
                    netSalary = grossSalary.add(totalAdvance);  // first month → add advance
                } else {
                    netSalary = grossSalary.subtract(BigDecimal.valueOf(deduction));  // deduct repayment
                }
            } else {
                // Fully paid off in prior month
                totalAdvance     = BigDecimal.ZERO;
                monthlyAdvance   = BigDecimal.ZERO;
                remainingAdvance = BigDecimal.ZERO;
                netSalary        = grossSalary;
            }
        }

        return new SalaryDetailsDTO(
                totalDays,
                salaryPerDay,
                totalAdvance,
                monthlyAdvance,
                remainingAdvance,
                grossSalary,
                netSalary
        );
    }

    public void addAdvance(User user, double total, double monthly, LocalDate startDate) {
        Advance advance = new Advance(user, startDate, total, monthly);
        boolean ok = cashBoxService.recordTransaction(
                BigDecimal.valueOf(total),
                TransactionType.EXPENSE,
                "Advance payment for employee: " + user.getFullName()
        );
        if (!ok)
            throw new CustomException("Could not record advance disbursement.", "GENERAL");
        advanceDAO.insertAdvance(advance);
    }

    public void paySalary(User user, int year, int month) {
        SalaryRecord existing = salaryRecordDAO.getRecordForUserAndMonth(user, year, month);
        if(existing != null)
            throw new CustomException("Salary already paid for this employee in " + month + "/" + year, "GENERAL");
        int totalDays = attendanceService.countPresentDays(user, year, month);
        SalaryDetailsDTO salaryDetails = buildSalaryDetails(user, year, month, totalDays);

        boolean ok = cashBoxService.recordTransaction(
                salaryDetails.getNetSalary(),
                TransactionType.EXPENSE,
                "Salary payment for employee: " + user.getFullName() + " for " + year + "/" + month
        );

        if (!ok) {
            throw new CustomException("Failed to pay salary. Cash box might be closed.", "GENERAL");
        } else {
            SalaryRecord record = new SalaryRecord();
            record.setUser(user);
            record.setYear(year);
            record.setMonth(month);
            record.setTotalDays(totalDays);
            record.setSalaryPerDay(salaryDetails.getSalaryPerDay());
            record.setGrossSalary(salaryDetails.getGrossSalary());
            record.setNetSalary(salaryDetails.getNetSalary());
            record.setTotalAdvance(salaryDetails.getTotalAdvance());
            record.setMonthlyAdvance(salaryDetails.getMonthlyAdvance());
            record.setRemainingAdvance(salaryDetails.getRemainingAdvance());

            salaryRecordDAO.insertSalaryRecord(record);
            AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "Salary paid successfully.");
        }
    }

    public void updateMonthlyAdvance(User user, int year, int month, double newMonthlyAdvance) {
        Advance latest = advanceDAO.getLatestAdvanceForUser(user);
        if (latest != null && latest.getStartDate().getYear() == year && latest.getStartDate().getMonthValue() == month) {
            latest.setMonthlyAdvance(newMonthlyAdvance);
            advanceDAO.updateAdvance(latest);
        }
    }

    public void deleteLatestAdvance(User user, int year, int month) {
        Advance latest = advanceDAO.getLatestAdvanceForUser(user);
        if (latest != null && latest.getStartDate().getYear() == year && latest.getStartDate().getMonthValue() == month) {
            advanceDAO.deleteAdvance(latest.getId());
        }
    }
}

