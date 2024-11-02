package com.maya2002yagan.supermarket_management.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Utility class for configuring and providing access to the Hibernate SessionFactory.
 * This class ensures a single instance of the SessionFactory is created and shared,
 * following the Singleton design pattern.
 * 
 * Note: This class initializes the SessionFactory once, at application startup 
 * and should be reused throughout the application lifecycle.
 * Any configuration errors during initialization will result in a runtime exception.
 * 
 * @author Maya Yagan
 */
public class HibernateUtil {
    /** Singleton instance of SessionFactory initialized once at application startup. */
    private static final SessionFactory sessionFactory = buildSessionFactory();
    
    /**
     * Builds and configures the SessionFactory instance.
     * 
     * This method uses Hibernate's Configuration class to load the
     * default configuration file (hibernate.cfg.xml) and create a
     * SessionFactory based on those settings.
     * 
     * @return A configured {@code SessionFactory} instance
     * @throws ExceptionInInitializerError if any configuration issues occur, preventing
     *         Hibernate from creating a {@code SessionFactory} instance
     */
    private static SessionFactory buildSessionFactory(){
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable error) {
            throw new ExceptionInInitializerError(error);
        }
    }
    
    /**
     * Provides access to the Singleton {@link SessionFactory} instance.
     * 
     * This method should be used to obtain the shared SessionFactory instance,
     * which is configured once at application startup.
     * 
     * @return The shared SessionFactory instance
     */
    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }
}
