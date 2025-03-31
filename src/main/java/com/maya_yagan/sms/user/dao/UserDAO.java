package com.maya_yagan.sms.user.dao;

import com.maya_yagan.sms.util.HibernateUtil;
import com.maya_yagan.sms.user.model.User;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the User entity.
 * 
 * UserDAO provides methods for performing CRUD (Create, Read, Update, Delete) operations on User entities.
 * It interacts with the database using Hibernate for session management.
 * 
 * @author Maya Yagan
 */
public class UserDAO {
    /**
     * Inserts a new user into the database.
     *
     * @param user the User object to be inserted
     * @return true if the insertion was successful, false otherwise
     */
    public boolean insertUser(User user){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if(transaction != null) transaction.rollback();
             e.printStackTrace();
             return false;
        }
    }
    
    /**
     * Retrieves a user from the database by their ID.
     *
     * @param id the ID of the user to retrieve
     * @return the User object with the specified ID, or null if not found
     */
    public User getUserById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id", User.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves a list of all users from the database.
     *
     * @return a List of User objects
     */
    public List<User> getUsers(){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles", User.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Updates the information of an existing user in the database.
     *
     * @param user the User object containing updated information
     */
    public void updateUser(User user){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            User u = session.get(User.class, user.getId());
            u.setFirstName(user.getFirstName());
            u.setLastName(user.getLastName());
            u.setEmail(user.getEmail());
            u.setPhoneNumber(user.getPhoneNumber());
            u.setTcNumber(user.getTcNumber());
            u.setSalary(user.getSalary());
            u.setBirthDate(user.getBirthDate());
            u.setGender(user.getGender());
            u.setIsFullTime(user.getIsFullTime());
            u.setIsPartTime(user.getIsPartTime());
            u.getRoles().clear();
            u.getRoles().addAll(user.getRoles());
            if (user.getPassword() != null && !user.getPassword().isEmpty() && user.getPassword().length() != 60)
                u.setPassword(user.getPassword());
            session.update(u);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
          e.printStackTrace();
        }
    }
    
    /**
     * Deletes a user from the database by their ID.
     *
     * @param id the ID of the user to delete
     */
    public void deleteUser(int id){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if(user != null) session.delete(user);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
          e.printStackTrace();
        }
    }
}

