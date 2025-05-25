package com.maya_yagan.sms.settings.dao;

import com.maya_yagan.sms.settings.model.Settings;
import com.maya_yagan.sms.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the Settings entity.
 * Because the application is designed to have a single Settings record,
 * the DAO provides helper methods to fetch or update “the one and only” row.
 *
 * @author Maya Yagan
 */
public class SettingsDAO {

    public boolean insertSettings(Settings settings) {
        // Defensive check: don’t let a second row be created accidentally.
        if (getSettings() != null) {
            return false;
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(settings);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public Settings getSettings() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Settings> q = session.createQuery(
                    "SELECT DISTINCT s FROM Settings s", Settings.class);
            return q.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Settings getSettingsById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Settings.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateSettings(Settings newValues) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Settings existing = getSettings();
            if (existing == null) {
                // First-time save
                session.save(newValues);
            } else {
                existing.setMarketName(newValues.getMarketName());
                existing.setPhone(newValues.getPhone());
                existing.setMoneyUnit(newValues.getMoneyUnit());
                existing.setAddress(newValues.getAddress());
                session.update(existing);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }
}
