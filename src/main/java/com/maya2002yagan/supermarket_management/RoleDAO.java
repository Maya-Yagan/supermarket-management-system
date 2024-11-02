package com.maya2002yagan.supermarket_management;

import org.hibernate.Session;
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
            Query<Role> query = session.createQuery("FROM Role WHERE position = :position", Role.class);
            query.setParameter("position", role);
            return query.uniqueResult();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
