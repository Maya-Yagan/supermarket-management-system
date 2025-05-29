package com.maya_yagan.sms.payment.service;

import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.payment.dao.CashBoxDAO;
import com.maya_yagan.sms.payment.dao.FinancialRecordDAO;
import com.maya_yagan.sms.payment.model.CashBox;
import com.maya_yagan.sms.payment.model.CashBoxStatus;
import com.maya_yagan.sms.payment.model.FinancialRecord;
import com.maya_yagan.sms.payment.model.TransactionType;
import com.maya_yagan.sms.user.dao.UserDAO;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ViewUtil;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;

/**
 * Service class for managing CashBox logic in the application.
 * Ensures only one active cash box is open at a time.
 * @author Maya Yagan
 */
public class CashBoxService {

    private final CashBoxDAO cashBoxDAO = new CashBoxDAO();
    private final FinancialRecordDAO financialRecordDAO = new FinancialRecordDAO();
    User currentUser = UserSession.getInstance().getCurrentUser();

    /**
     * Opens a new cash box. Fails if another one is already open.
     */
    public void openCashBox() {
        CashBox current = cashBoxDAO.getOpenCashBox();
        if (current != null)
            throw new CustomException("A cash box is already open.", "GENERAL");

        CashBox lastClosed = cashBoxDAO.getLastClosedCashBox();
        double previousBalance = lastClosed == null
                ? 0.0
                : lastClosed.getTotalBalance();

        ViewUtil.showFloatInputDialog(
                "Open Cash Box",
                "Previous closing balance: " + String.format("%,.2f", previousBalance),
                "Confirm or adjust the opening balance:",
                (float) previousBalance,
                "Opening Balance",
                val -> val >= 0,
                openingBalance -> {
                    CashBox newCashBox = new CashBox();
                    newCashBox.setOpenedAt(LocalDateTime.now());
                    newCashBox.setTotalBalance(openingBalance);
                    newCashBox.setStatus(CashBoxStatus.OPEN);
                    newCashBox.setOpenedBy(currentUser);

                    if (cashBoxDAO.insertCashBox(newCashBox)) {
                        AlertUtil.showAlert(
                                Alert.AlertType.INFORMATION,
                                "Cash Box Opened",
                                "Cash box started with balance "
                                        + String.format("%,.2f", previousBalance));
                    }
                });
    }

    /**
     * Closes the currently open cash box.
     */
    public void closeCashBox() {
        AlertUtil.showConfirmation(
                "Close Cash Box",
                "Confirm Close Cash Box",
                "Are you sure you want to close the cash box?",
                () -> {
                    CashBox current = cashBoxDAO.getOpenCashBox();
                    if (current == null)
                        throw new CustomException("No open cash box to close.", "NO_OPEN_CASH_BOX");

                    current.setClosedAt(LocalDateTime.now());
                    current.setStatus(CashBoxStatus.CLOSED);
                    current.setClosedBy(currentUser);
                    if (cashBoxDAO.updateCashBox(current))
                        AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Cash Box Closed", "The cash box has been closed successfully.");
                }
        );
    }

    /**
     * Records a financial transaction (income or expense).
     *
     * @param amount The transaction amount (positive for income, negative for expense).
     * @param type The type of the transaction (INCOME or EXPENSE).
     * @return true if the record was saved, false otherwise.
     */
    public boolean recordTransaction(double amount, TransactionType type) {
        CashBox current = cashBoxDAO.getOpenCashBox();
        if (current == null)
            throw new CustomException("No open cash box.\nPlease open the cash box first.", "NO_OPEN_CASH_BOX");

        FinancialRecord record = new FinancialRecord();
        record.setDateTime(LocalDateTime.now());
        record.setAmount(amount);
        record.setType(type);
        record.setCashBox(current);
        currentUser = new UserDAO().getUserById(currentUser.getId());
        record.setIssuedBy(currentUser);

        boolean saved = financialRecordDAO.insertFinancialRecord(record);
        if (!saved) return false;

        double newBalance = type == TransactionType.INCOME
                ? current.getTotalBalance() + amount
                : current.getTotalBalance() - amount;

        current.setTotalBalance(newBalance);
        cashBoxDAO.updateCashBox(current);
        return true;
    }

    /**
     * Gets the currently open cash box (if any).
     *
     * @return The open CashBox or null.
     */
    public CashBox getCurrentOpenCashBox() {
        return cashBoxDAO.getOpenCashBox();
    }

    public String getCashBoxStatus(){
        return getCurrentOpenCashBox() != null
                ? getCurrentOpenCashBox().getStatus().toString()
                : CashBoxStatus.CLOSED.name();
    }

    public boolean isCashBoxOpen(){
        return  "OPEN".equals(getCashBoxStatus());
    }
}
