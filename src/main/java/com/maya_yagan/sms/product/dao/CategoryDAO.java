package com.maya_yagan.sms.product.dao;

import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.util.HibernateUtil;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the Category entity.
 * This class provides methods for performing CRUD operations on Category data
 * in the database, including inserting, retrieving, updating, and deleting categories.
 * 
 * @author Maya Yagan
 */
public class CategoryDAO {
    /**
     * Inserts a new category into the database.
     * 
     * @param category The category to be inserted
     */
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
    
    /**
     * Retrieves a category by its unique identifier.
     * 
     * @param id The unique identifier of the category
     * @return The category with the specified id, or null if not found
     */
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
    
    /**
     * Retrieves a category by its name.
     * 
     * @param category The name of the category to be retrieved
     * @return The category with the specified name, or null if not found
     */
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
    
    /**
     * Retrieves all distinct categories from the database.
     * 
     * @return A set of all categories
     */
    public Set<Category> getCategories(){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Category> query = session.createQuery("SELECT DISTINCT c FROM Category c", Category.class);
            return new HashSet<>(query.getResultList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Updates an existing category in the database.
     * 
     * @param category The category with updated data
     */
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
    
    /**
     * Deletes a category from the database using its unique identifier.
     * 
     * @param id The unique identifier of the category to be deleted
     */
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
