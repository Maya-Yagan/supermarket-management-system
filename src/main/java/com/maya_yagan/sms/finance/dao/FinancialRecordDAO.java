package com.maya_yagan.sms.finance.dao;

import com.maya_yagan.sms.finance.model.CashBox;
import com.maya_yagan.sms.finance.model.FinancialRecord;
import com.maya_yagan.sms.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Data Access Object (DAO) for the FinancialRecord entity.
 * Handles CRUD operations for financial transaction records,
 * including linking to a specific CashBox.
 * @author Maya Yagan
 */
public class FinancialRecordDAO {

    /**
     * Inserts a new FinancialRecord into the database.
     *
     * @param record The financial record to insert.
     * @return true if successful, false otherwise.
     */
    public boolean insertFinancialRecord(FinancialRecord record) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(record);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all financial records for a specific cash box.
     *
     * @param cashBox The cash box to filter records by.
     * @return List of financial records linked to that cash box.
     */
    public List<FinancialRecord> getRecordsByCashBox(CashBox cashBox) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<FinancialRecord> query = session.createQuery(
                    "FROM FinancialRecord WHERE cashBox = :cashBox", FinancialRecord.class);
            query.setParameter("cashBox", cashBox);
            return query.getResultList();
        }
    }

    /**
     * Retrieves all financial records in the system.
     *
     * @return List of all financial records.
     */
    public List<FinancialRecord> getAllRecords() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM FinancialRecord", FinancialRecord.class).list();
        }
    }

    /**
     * Deletes a financial record.
     *
     * @param record The record to delete.
     */
    public void deleteRecord(FinancialRecord record) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.delete(record);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
