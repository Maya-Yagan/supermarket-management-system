package com.maya_yagan.sms.homepage.dao;

import com.maya_yagan.sms.homepage.model.Notification;
import com.maya_yagan.sms.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

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

}
