package com.maya_yagan.sms.product.dao;

import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.util.HibernateUtil;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the Product entity.
 * This class provides methods for performing CRUD operations on Product data
 * in the database, including inserting, retrieving, updating, and deleting products.
 *
 * @author Maya Yagan
 */
public class ProductDAO {
    /**
     * Inserts a new product into the database.
     *
     * @param product The product to be inserted
     */
    public boolean insertProduct(Product product){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.save(product);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id The unique identifier of the product
     * @return The product with the specified id, or null if not found
     */
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

    /**
     * Retrieves a set of products that belong to a specific category.
     *
     * @param category The category for which to retrieve products
     * @return A set of products in the specified category
     */
    public Set<Product> getProductsByCategory(Category category) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Product> query = session.createQuery("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.supplierProducts WHERE p.category = :category", Product.class);
            query.setParameter("category", category);
            return new HashSet<>(query.getResultList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all distinct products from the database.
     *
     * @return A set of all products
     */
    public Set<Product> getProducts(){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Product> query = session.createQuery("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.supplierProducts", Product.class);
            return new HashSet<>(query.getResultList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates an existing product in the database.
     *
     * @param product The product with updated data
     */
    public void updateProduct(Product product){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Product p = session.get(Product.class, product.getId());
            p.setName(product.getName());
            p.setPrice(product.getPrice());
            p.setDiscount(product.getDiscount());
            p.setProductionDate(product.getProductionDate());
            p.setExpirationDate(product.getExpirationDate());
            p.setCategory(product.getCategory());
            p.setUnit(product.getUnit());
            p.setBarcode(product.getBarcode());
            session.update(p);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Deletes a product from the database using its unique identifier.
     *
     * @param id The unique identifier of the product to be deleted
     */
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