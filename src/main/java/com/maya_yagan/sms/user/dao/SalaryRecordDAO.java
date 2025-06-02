package com.maya_yagan.sms.user.dao;

import com.maya_yagan.sms.user.model.SalaryRecord;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

public class SalaryRecordDAO {
    public void insertSalaryRecord(SalaryRecord record) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(record);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public SalaryRecord getRecordForUserAndMonth(User user, int year, int month) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SalaryRecord> query = session.createQuery(
                    "FROM SalaryRecord s WHERE s.user = :user AND s.year = :year AND s.month = :month",
                    SalaryRecord.class
            );
            query.setParameter("user", user);
            query.setParameter("year", year);
            query.setParameter("month", month);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<SalaryRecord> getSalaryRecordsForUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SalaryRecord> query = session.createQuery(
                    "FROM SalaryRecord s WHERE s.user = :user ORDER BY s.year DESC, s.month DESC",
                    SalaryRecord.class
            );
            query.setParameter("user", user);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<SalaryRecord> getAllSalaryRecords() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SalaryRecord> query = session.createQuery(
                    "FROM SalaryRecord s ORDER BY s.year DESC, s.month DESC",
                    SalaryRecord.class
            );
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void updateSalaryRecord(SalaryRecord record) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(record);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void deleteSalaryRecord(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            SalaryRecord record = session.get(SalaryRecord.class, id);
            if (record != null) session.delete(record);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}
