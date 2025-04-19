package com.maya_yagan.sms.warehouse.dao;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.util.HibernateUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the Warehouse entity.
 * Provides methods to perform CRUD operations on Warehouse data.
 * 
 * @author Maya Yagan
 */
public class WarehouseDAO {
    /**
     * Inserts a new warehouse into the database.
     * 
     * @param warehouse The warehouse to be inserted
     */
    public void insertWarehouse(Warehouse warehouse){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.save(warehouse);
            transaction.commit();
        } catch (Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves a warehouse by its unique identifier.
     * 
     * @param id The unique identifier of the warehouse
     * @return The warehouse with the specified id, or null if not found
     */
    public Warehouse getWarehouseById(int id){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Warehouse> query = session.createQuery("FROM Warehouse w LEFT JOIN FETCH w.productWarehouses WHERE w.id = :id", Warehouse.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves a list of products stored in a specific warehouse.
     * 
     * @param warehouse The warehouse from which to retrieve products
     * @return List of products in the specified warehouse
     */
    public List<Product> getProductWithWarehouse(Warehouse warehouse){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Product> query = session.createQuery("SELECT p FROM Product p JOIN FETCH p.productWarehouses pw WHERE pw.warehouse = :warehouse", Product.class);
            query.setParameter("warehouse", warehouse);
            return query.getResultList();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves a list of all warehouses.
     * 
     * @return List of all warehouses
     */
    public List<Warehouse> getWarehouses(){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Warehouse> query = session.createQuery("SELECT DISTINCT w FROM Warehouse w LEFT JOIN FETCH w.productWarehouses", Warehouse.class);
            return query.getResultList();
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Updates an existing warehouse in the database.
     * 
     * @param warehouse The warehouse with updated data
     */
    public void updateWarehouse(Warehouse warehouse) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Warehouse managedWarehouse = session.get(Warehouse.class, warehouse.getId());
            if (managedWarehouse != null) {
                managedWarehouse.setName(warehouse.getName());
                managedWarehouse.setCapacity(warehouse.getCapacity());
                Hibernate.initialize(managedWarehouse.getProductWarehouses());

                // Synchronize product warehouses
                Map<Product, ProductWarehouse> existingMap = managedWarehouse.getProductWarehouses().stream()
                        .collect(Collectors.toMap(ProductWarehouse::getProduct, pw -> pw));

                for(ProductWarehouse pw : warehouse.getProductWarehouses()){
                    ProductWarehouse existingPw = existingMap.get(pw.getProduct());
                    if(existingPw != null)
                        existingPw.setAmount(pw.getAmount());
                    else 
                        managedWarehouse.getProductWarehouses().add(
                                new ProductWarehouse(managedWarehouse, pw.getProduct(), pw.getAmount()));
                }
                session.merge(managedWarehouse);
            }
            session.flush();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes a product from a specific warehouse.
     * 
     * @param warehouse The warehouse from which the product will be deleted
     * @param product The product to be deleted from the warehouse
     */
    public void deleteProductFromWarehouse(Warehouse warehouse, Product product){
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Query<ProductWarehouse> query = session.createQuery(
                    "FROM ProductWarehouse pw WHERE pw.warehouse = :warehouse AND pw.product = :product",
                    ProductWarehouse.class
            );
            query.setParameter("warehouse", warehouse);
            query.setParameter("product", product);
            ProductWarehouse productWarehouse = query.uniqueResult();
            if(productWarehouse != null) session.delete(productWarehouse);
            transaction.commit();
        } catch(Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Deletes a warehouse by its unique identifier.
     * 
     * @param id The unique identifier of the warehouse to be deleted
     */
    public void deleteWarehouse(int id){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Warehouse warehouse = session.get(Warehouse.class, id);
            if(warehouse != null) session.delete(warehouse);
            transaction.commit();
        } catch(Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
