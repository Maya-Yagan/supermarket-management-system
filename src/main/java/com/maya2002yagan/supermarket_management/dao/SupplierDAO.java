package com.maya2002yagan.supermarket_management.dao;

import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.Supplier;
import com.maya2002yagan.supermarket_management.model.SupplierProduct;
import com.maya2002yagan.supermarket_management.util.HibernateUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
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
            Query<Supplier> query = session.createQuery("FROM Supplier s LEFT JOIN FETCH s.supplierProducts WHERE s.id = :id", Supplier.class);
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
     * Retrieves a list of supplier-product association for a specific supplier.
     * 
     * @param supplier The supplier for which to retrieve product associations
     * @return List of SupplierProduct entries associated with the specified supplier
     */
    public List<SupplierProduct> getSupplierProductPairs(Supplier supplier){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<SupplierProduct> query = session.createQuery(
                    "FROM SupplierProduct sp WHERE sp.supplier = :supplier",
                    SupplierProduct.class
            );
            query.setParameter("supplier", supplier);
            return query.getResultList();
        } catch(Exception e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Retrieves all distinct suppliers from the database
     * 
     * @return A set of all suppliers
     */
    public Set<Supplier> getSuppliers(){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Supplier> query = session.createQuery("SELECT DISTINCT s FROM Supplier s LEFT JOIN FETCH s.supplierProducts", Supplier.class);
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
            Supplier managedSupplier = session.get(Supplier.class, supplier.getId());
            if(managedSupplier != null){
                managedSupplier.setName(supplier.getName());
                managedSupplier.setEmail(supplier.getEmail());
                managedSupplier.setPhoneNumber(supplier.getPhoneNumber());
                Hibernate.initialize(managedSupplier.getSupplierProducts());
                Map<Product, SupplierProduct> existingMap = managedSupplier.getSupplierProducts().stream()
                        .collect(Collectors.toMap(SupplierProduct::getProduct, sp -> sp));
                for(SupplierProduct sp : supplier.getSupplierProducts()){
                    SupplierProduct existingSp = existingMap.get(sp.getProduct());
                    if(existingSp != null)
                        existingSp.setPrice(sp.getPrice());
                    else
                        managedSupplier.getSupplierProducts().add(
                                new SupplierProduct(sp.getProduct(), managedSupplier, sp.getPrice())
                        );
                }
                session.merge(managedSupplier);
            }
            session.flush();
            transaction.commit();
        } catch(Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes a single product from a specific supplier
     * 
     * @param supplier The supplier from which the product will be deleted
     * @param product The product to be deleted from the supplier
     */
    public void deleteProductFromSupplier(Supplier supplier, Product product){
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Query<SupplierProduct> query = session.createQuery(
                    "FROM SupplierProduct sp WHERE sp.supplier = :supplier AND sp.product = :product",
                    SupplierProduct.class
            );
            query.setParameter("supplier", supplier);
            query.setParameter("product", product);
            SupplierProduct supplierProduct = query.uniqueResult();
            if(supplierProduct != null) session.delete(supplierProduct);
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
