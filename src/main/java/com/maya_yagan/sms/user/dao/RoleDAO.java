package com.maya_yagan.sms.user.dao;

import com.maya_yagan.sms.util.HibernateUtil;
import com.maya_yagan.sms.user.model.Role;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the Role entity.
 * 
 * This class provides methods to interact with the Role 
 * data in the database.
 * 
 * @author Maya Yagan
 */
public class RoleDAO {
    /**
     * Retrieves a Role object from the database based on its name (position).
     * 
     * @param role the name of the role to search for
     * @return the Role object if found, or null if not found or an error occurs
     */
    public Role getRoleByName(String role){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Role> query = session.createQuery("FROM Role WHERE name = :name", Role.class);
            query.setParameter("name", role);
            return query.uniqueResult();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public void insertRole(Role role) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(role);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
