package com.maya_yagan.sms.homepage.dao;

import com.maya_yagan.sms.homepage.model.Notification;
import com.maya_yagan.sms.util.HibernateUtil;
import java.util.Collections;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.hibernate.query.Query;

import java.util.HashSet;
import java.util.Set;

public class NotificationDAO {
    public void insertNotification(Notification notification){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.save(notification);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    public Set<Notification> getNotifications(){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Notification> query = session.createQuery("SELECT DISTINCT n FROM Notification n", Notification.class);
            return new HashSet<>(query.getResultList());
        } catch(Exception e){
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    public void deleteNotification(int id){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Notification notification = session.get(Notification.class, id);
            if(notification != null) session.delete(notification);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
