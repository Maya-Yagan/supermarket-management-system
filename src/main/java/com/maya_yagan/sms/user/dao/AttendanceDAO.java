package com.maya_yagan.sms.user.dao;

import com.maya_yagan.sms.user.model.Attendance;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Maya Yagan
 */
public class AttendanceDAO {

    public void insertAttendance(Attendance attendance) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(attendance);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                try {
                    transaction.rollback();
                } catch (Exception rbEx) {
                    rbEx.printStackTrace();
                }
            }
            e.printStackTrace();
        }
    }

    public Attendance getAttendanceById(Long id){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Attendance.class, id);
        }
    }

    public Set<Attendance> getAttendancesForUser(User user){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            var list = session.createQuery(
                            "FROM Attendance a WHERE a.user = :u", Attendance.class)
                    .setParameter("u", user)
                    .list();
            return new HashSet<>(list);
        }
    }

    public void updateAttendance(Attendance attendance){
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.merge(attendance);
            transaction.commit();
        } catch(Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public Attendance getAttendanceForUserOnDate(User user, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Attendance a WHERE a.user = :u AND a.date = :d", Attendance.class)
                    .setParameter("u", user)
                    .setParameter("d", date)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<Attendance> getAttendancesForUserByYearAndMonth(User user, int year, int month){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var list = session.createQuery(
                            "FROM Attendance a WHERE a.user = :u AND year(a.date) = :y AND month(a.date) = :m",
                            Attendance.class)
                    .setParameter("u", user)
                    .setParameter("y", year)
                    .setParameter("m", month)
                    .list();
            return new HashSet<>(list);
        }
    }
}
