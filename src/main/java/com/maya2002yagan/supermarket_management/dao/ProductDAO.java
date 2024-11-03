package com.maya2002yagan.supermarket_management.dao;

import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.util.HibernateUtil;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the Product entity.
 * 
 * @author Maya Yagan
 */
public class ProductDAO {
    public void insertProduct(Product product){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.save(product);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    public Product getProductById(int id){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Product> query = session.createQuery("FROM Product p WHERE p.id = :id", Product.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Set<Product> getProductsByCategory(Category category) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Product> query = session.createQuery("FROM Product p WHERE p.category = :category", Product.class);
            query.setParameter("category", category);
            return new HashSet<>(query.getResultList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    public Set<Product> getProducts(){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Product> query = session.createQuery("SELECT DISTINCT p FROM Product p", Product.class);
            return new HashSet<>(query.getResultList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void updateProduct(Product product){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Product p = session.get(Product.class, product.getId());
            p.setName(product.getName());
            p.setPrice(product.getPrice());
            p.setProductionDate(product.getProductionDate());
            p.setExpirationDate(product.getExpirationDate());
            p.setCategory(product.getCategory());
            session.update(p);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
          e.printStackTrace();
        }
    }
    
    public void deleteProduct(int id){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Product product = session.get(Product.class, id);
            if(product != null) session.delete(product);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
          e.printStackTrace();
        }
    }
}
