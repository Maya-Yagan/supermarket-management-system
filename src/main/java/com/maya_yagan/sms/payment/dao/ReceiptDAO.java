package com.maya_yagan.sms.payment.dao;

import com.maya_yagan.sms.finance.model.CashBox;
import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.model.ReceiptItem;
import com.maya_yagan.sms.payment.model.ReceiptStatus;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.HibernateUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the Receipt entity.
 * Provides CRUD operations for Receipt objects in the database.
 *
 * @author Maya Yagan
 */
public class ReceiptDAO {

    /**
     * Inserts a new receipt into the database.
     *
     * @param receipt The receipt to be inserted
     * @return true if insertion successful, false otherwise
     */
    public boolean insertReceipt(Receipt receipt) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(receipt);
            transaction.commit();
            return true;
        } catch (Exception ex) {
            // ⬇ print the root cause first
            ex.printStackTrace();             // <-- real reason the flush failed
            if (transaction != null) {
                try {
                    transaction.rollback();            // may throw “connection closed”
                } catch (Exception ignore) {}
            }
            return false;
        }
    }

    /**
     * Retrieves a receipt by its unique identifier.
     *
     * @param id The unique identifier of the receipt
     * @return The receipt with the specified id, or null if not found
     */
    public Receipt getReceiptById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Receipt> query = session.createQuery("FROM Receipt r WHERE r.id = :id", Receipt.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves a receipt by its unique code.
     *
     * @param code The unique code of the receipt
     * @return The receipt with the specified code, or null if not found
     */
    public Receipt getReceiptByCode(String code) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT r " +
                                    "FROM Receipt r " +
                                    "LEFT JOIN FETCH r.items i " +
                                    "LEFT JOIN FETCH i.product " +
                                    "WHERE r.code = :code", Receipt.class)
                    .setParameter("code", code)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all receipts created by a specific cashier.
     *
     * @param cashier The User who created the receipts
     * @return List of receipts created by the cashier
     */
    public List<Receipt> getReceiptsByCashier(User cashier) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Receipt> query = session.createQuery("FROM Receipt r WHERE r.cashier = :cashier", Receipt.class);
            query.setParameter("cashier", cashier);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all receipts from the database.
     *
     * @return List of all receipts
     */
    public List<Receipt> getAllReceipts() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Receipt> query = session.createQuery("FROM Receipt", Receipt.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates an existing receipt in the database.
     *
     * @param receipt The receipt with updated data
     */
    public void updateReceipt(Receipt receipt) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Receipt r = session.get(Receipt.class, receipt.getId());
            if (r != null) {
                r.setCode(receipt.getCode());
                r.setDateTime(receipt.getDateTime());
                r.setCashier(receipt.getCashier());
                r.setPaymentMethod(receipt.getPaymentMethod());
                r.setPaidAmount(receipt.getPaidAmount());
                r.setChangeGiven(receipt.getChangeGiven());
                r.setStatus(receipt.getStatus());
                r.getItems().clear();
                r.getItems().addAll(receipt.getItems());
                session.update(r);
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Deletes a receipt from the database by its unique identifier.
     *
     * @param id The unique identifier of the receipt to be deleted
     */
    public void deleteReceipt(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Receipt receipt = session.get(Receipt.class, id);
            if (receipt != null) {
                session.delete(receipt);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public void markAsRefunded(long receiptId, BigDecimal changeGiven) {

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Receipt managed = session.get(Receipt.class, receiptId);
            if (managed != null) {
                managed.setStatus(ReceiptStatus.REFUNDED);
                managed.setChangeGiven(changeGiven);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public List<Receipt> getReceiptsForPeriod(LocalDateTime from, LocalDateTime to) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
            SELECT DISTINCT r FROM Receipt r
            LEFT JOIN FETCH r.items i
            LEFT JOIN FETCH i.product
            WHERE r.dateTime BETWEEN :from AND :to
            """, Receipt.class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .list();
        }
    }
}
