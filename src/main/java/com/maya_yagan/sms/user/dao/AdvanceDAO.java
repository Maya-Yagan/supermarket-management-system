package com.maya_yagan.sms.user.dao;

import com.maya_yagan.sms.user.model.Advance;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class AdvanceDAO {
    public void insertAdvance(Advance advance) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(advance);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public Advance getAdvanceById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Advance.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Latest (most-recent startDate) advance for a user,
     or null if they have none. */
    public Advance getLatestAdvanceForUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Advance> q = session.createQuery(
                    "FROM Advance a WHERE a.user = :user "
                            + "ORDER BY a.startDate DESC", Advance.class);
            q.setParameter("user", user);
            q.setMaxResults(1);
            return q.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** All advances ever taken by a user (oldest → newest). */
    public List<Advance> getAdvancesForUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Advance> q = session.createQuery(
                    "FROM Advance a WHERE a.user = :user "
                            + "ORDER BY a.startDate ASC", Advance.class);
            q.setParameter("user", user);
            return q.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /** Advances that are still being repaid in a given Year-Month */
    public List<Advance> getActiveAdvancesForUser(User user, int year, int month) {
        LocalDate target = LocalDate.of(year, month, 1);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Advance> q = session.createQuery(
                    "FROM Advance a WHERE a.user = :user "
                            + "AND a.totalAdvance > 0 "
                            + "AND (a.totalAdvance - "
                            +     "(MONTHS_BETWEEN(:target, a.startDate) * a.monthlyAdvance)) > 0",
                    Advance.class);
            q.setParameter("user", user);
            q.setParameter("target", target);
            return q.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void updateAdvance(Advance advance) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(advance);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void deleteAdvance(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Advance adv = session.get(Advance.class, id);
            if (adv != null) session.delete(adv);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}