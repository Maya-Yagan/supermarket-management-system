package com.maya2002yagan.supermarket_management.dao;

import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.Supplier;
import com.maya2002yagan.supermarket_management.model.SupplierProduct;
import com.maya2002yagan.supermarket_management.util.HibernateUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the Supplier entity.
 * This class provides methods for preforming CRUD operations on Supplier data
 * in the database, including inserting, retrieving, updating and deleting suppliers.
 * 
 * @author Maya Yagan
 */
public class SupplierDAO {
    /**
     * Inserts a new supplier into the database
     * 
     * @param supplier The supplier to be inserted
     */
    public void insertSupplier(Supplier supplier){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.save(supplier);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves a supplier by its unique identifier
     * 
     * @param id The unique identifier of the supplier
     * @return The supplier with the specified id, or null if not found
     */
    public Supplier getSupplierById(int id){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Supplier> query = session.createQuery("FROM Supplier s WHERE s.id = :id", Supplier.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves a list of products provided by a specific supplier.
     * 
     * @param supplier The supplier that offers the products
     * @return List of products provided by a supplier
     */
    public List<Product> getSupplierProducts(Supplier supplier){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Product> query = session.createQuery("SELECT p FROM Product p JOIN FETCH p.supplierProducts sp WHERE sp.supplier = :supplier", Product.class);
            query.setParameter("supplier", supplier);
            return query.getResultList();
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    } 
    
    /**
     * Retrieves all distinct suppliers from the database
     * 
     * @return A set of all suppliers
     */
    public Set<Supplier> getSuppliers(){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Supplier> query = session.createQuery("SELECT DISTINCT s FROM Supplier s", Supplier.class);
            return new HashSet<>(query.getResultList());
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Updates an existing supplier in the database
     * 
     * @param supplier The supplier with updated data 
     */
    public void updateSupplier(Supplier supplier){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Supplier s = session.get(Supplier.class, supplier.getId());
            s.setEmail(supplier.getEmail());
            s.setName(supplier.getName());
            s.setPhoneNumber(supplier.getPhoneNumber());
            session.update(s);
            transaction.commit();
        } catch(Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes a supplier from the database using the unique identifier.
     * 
     * @param id The unique identifier of the supplier to be deleted
     */
    public void deleteSupplier(int id){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Supplier supplier = session.get(Supplier.class, id);
            if(supplier != null) session.delete(supplier);
            transaction.commit();
        } catch(Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
