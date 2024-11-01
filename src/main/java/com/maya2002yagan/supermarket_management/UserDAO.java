package com.maya2002yagan.supermarket_management;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 *
 * CRUD create read update delete
 */
public class UserDAO {
    public void insertUser(User user){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(user);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null) transaction.rollback();
             e.printStackTrace();
        }
    }
    
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

    
    public List<User> getUsers(){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles", User.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
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
            // Update password if changed
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                u.setPassword(user.getPassword());
            }
            session.update(u);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
          e.printStackTrace();
        }
    }
    
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

