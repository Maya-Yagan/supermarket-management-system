package com.maya2002yagan.supermarket_management.dao;

import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.util.HibernateUtil;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
/**
 * Data Access Object (DAO) for the Category entity.
 * 
 * @author Maya Yagan
 */
public class CategoryDAO {
    public void insertCategory(Category category){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.save(category);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    public Category getCategoryById(int id){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Category> query = session.createQuery("FROM Category c WHERE c.id = :id", Category.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Category getCategoryByName(String category){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Category> query = session.createQuery("FROM Category WHERE name = :name", Category.class);
            query.setParameter("name", category);
            return query.uniqueResult();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public Set<Category> getCategories(){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Category> query = session.createQuery("SELECT DISTINCT c FROM Category c", Category.class);
            return new HashSet<>(query.getResultList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void updateCategory(Category category){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Category c = session.get(Category.class, category.getId());
            c.setName(category.getName());
            session.update(c);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
          e.printStackTrace();
        }
    }
    
    public void deleteCategory(int id){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Category category = session.get(Category.class, id);
            if(category != null) session.delete(category);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
          e.printStackTrace();
        }
    }
}
