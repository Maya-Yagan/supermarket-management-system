package com.maya_yagan.sms.payment.dao;

import com.maya_yagan.sms.payment.model.CashBox;
import com.maya_yagan.sms.payment.model.CashBoxStatus;
import com.maya_yagan.sms.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) for the CashBox entity.
 * This class handles CRUD operations for managing cash box records.
 * A cash box is assumed to be a container for financial transactions.
 * @author Maya Yagan
 */
public class CashBoxDAO {

    /**
     * Inserts a new CashBox into the database.
     *
     * @param cashBox The cash box to insert.
     * @return true if inserted successfully, false otherwise.
     */
    public boolean insertCashBox(CashBox cashBox) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(cashBox);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the currently open CashBox (if any).
     *
     * @return The open cash box or null.
     */
    public CashBox getOpenCashBox() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<CashBox> query = session.createQuery("FROM CashBox WHERE status = :status", CashBox.class);
            query.setParameter("status", CashBoxStatus.OPEN);
            List<CashBox> result = query.getResultList();
            return result.isEmpty() ? null : result.get(0);
        }
    }

    /**
     * Updates an existing cash box.
     *
     * @param cashBox The updated cash box instance.
     */
    public boolean updateCashBox(CashBox cashBox) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(cashBox);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a CashBox by its ID.
     *
     * @param id The ID of the CashBox.
     * @return The CashBox or null if not found.
     */
    public CashBox getCashBoxById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(CashBox.class, id);
        }
    }

    /**
     * Retrieves all cash boxes.
     *
     * @return List of all cash boxes.
     */
    public List<CashBox> getAllCashBoxes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM CashBox", CashBox.class).list();
        }
    }

    /**
     * Returns the most recently CLOSED cash box, or null if none exist.
     */
    public CashBox getLastClosedCashBox() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM CashBox WHERE status = :st ORDER BY closedAt DESC",
                            CashBox.class)
                    .setParameter("st", CashBoxStatus.CLOSED)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }
}
