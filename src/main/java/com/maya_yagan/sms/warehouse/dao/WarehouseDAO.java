package com.maya_yagan.sms.warehouse.dao;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.util.CustomException;
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

    public void addProductToWarehouse(int warehouseId, int productId, int amount) {

        if (amount <= 0) {
            throw new CustomException("Amount must be greater than 0", "INVALID_AMOUNT");
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Warehouse warehouse = session.get(Warehouse.class, warehouseId);
            if (warehouse == null) {
                throw new CustomException("Warehouse not found", "NOT_FOUND");
            }

            Product product = session.get(Product.class, productId);
            if (product == null) {
                throw new CustomException("Product not found", "NOT_FOUND");
            }

            Hibernate.initialize(warehouse.getProductWarehouses());

            int currentTotal = warehouse.getProductWarehouses()
                    .stream()
                    .mapToInt(ProductWarehouse::getAmount)
                    .sum();
            if (currentTotal + amount > warehouse.getCapacity()) {
                throw new CustomException(
                        "This warehouse doesn't have enough capacity.",
                        "INSUFFICIENT_CAPACITY");
            }

            // Either bump existing amount or create a new ProductWarehouse row
            ProductWarehouse pw = warehouse.getProductWarehouses()
                    .stream()
                    .filter(x -> x.getProduct().getId() == productId)
                    .findFirst()
                    .orElseGet(() -> {
                        ProductWarehouse newPw =
                                new ProductWarehouse(warehouse, product, 0);
                        warehouse.getProductWarehouses().add(newPw);
                        return newPw;
                    });

            pw.setAmount(pw.getAmount() + amount);

            session.merge(warehouse);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
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

    public void transferProduct(int productId, int amount,
                                int sourceWarehouseId, int targetWarehouseId) {

        if (sourceWarehouseId == targetWarehouseId)
            throw new CustomException("Source and destination warehouses are the same",
                    "INVALID_TARGET");

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Warehouse source = session.get(Warehouse.class, sourceWarehouseId);
            Warehouse target = session.get(Warehouse.class, targetWarehouseId);
            Product   product = session.get(Product.class, productId);

            if (source == null || target == null || product == null)
                throw new CustomException("Source/Target warehouse or product not found", "NOT_FOUND");

            Hibernate.initialize(source.getProductWarehouses());
            Hibernate.initialize(target.getProductWarehouses());

            // ---- remove from SOURCE ---------------------------------------------------------------
            ProductWarehouse sourcePw = source.getProductWarehouses().stream()
                    .filter(pw -> pw.getProduct().getId() == productId)
                    .findFirst()
                    .orElseThrow(() -> new CustomException("Product not in source warehouse",
                            "NOT_FOUND"));

            int newSourceAmount = sourcePw.getAmount() - amount;
            if (newSourceAmount == 0) {
                source.getProductWarehouses().remove(sourcePw);
                session.delete(sourcePw);
            } else {
                sourcePw.setAmount(newSourceAmount);
            }

            // ---- add to TARGET --------------------------------------------------------------------
            ProductWarehouse targetPw = target.getProductWarehouses().stream()
                    .filter(pw -> pw.getProduct().getId() == productId)
                    .findFirst()
                    .orElseGet(() -> {
                        ProductWarehouse pw = new ProductWarehouse(target, product, 0);
                        target.getProductWarehouses().add(pw);
                        return pw;
                    });
            targetPw.setAmount(targetPw.getAmount() + amount);

            session.merge(source);
            session.merge(target);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
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
