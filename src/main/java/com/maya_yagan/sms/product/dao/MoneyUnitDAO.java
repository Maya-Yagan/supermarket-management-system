package com.maya_yagan.sms.product.dao;

import com.maya_yagan.sms.product.model.MoneyUnit;
import com.maya_yagan.sms.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Rahaf Alaa
 */
public class MoneyUnitDAO {

    public void insertMoneyUnit(MoneyUnit moneyUnit) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            MoneyUnit existing = session.get(MoneyUnit.class, moneyUnit.getCode());
            if (existing == null) {
                session.save(moneyUnit);
            } else {
                existing.setName(moneyUnit.getName());
                existing.setSymbol(moneyUnit.getSymbol());
                session.update(existing);
            }
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            ex.printStackTrace();
        }
    }

    public MoneyUnit getMoneyUnitByCode(String code) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(MoneyUnit.class, code);
        }
    }

    public void deleteAllMoneyUnits() {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query<?> query = session.createQuery("DELETE FROM MoneyUnit");
            query.executeUpdate();
            tx.commit();
        } catch (Exception ex) {
            if (tx != null) tx.rollback();
            ex.printStackTrace();
        }
    }

    public Set<MoneyUnit> getAllMoneyUnits() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<MoneyUnit> units = session.createQuery("FROM MoneyUnit", MoneyUnit.class).list();
            return new HashSet<>(units);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptySet();
        }
    }
}
