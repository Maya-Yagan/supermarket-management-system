package com.maya2002yagan.supermarket_management;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();
    
    private static SessionFactory buildSessionFactory(){
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable error) {
            throw new ExceptionInInitializerError(error);
        }
    }
    
    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }
}
