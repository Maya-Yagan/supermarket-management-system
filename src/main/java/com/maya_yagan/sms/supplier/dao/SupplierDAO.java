package com.maya_yagan.sms.supplier.dao;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.util.HibernateUtil;
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
 * 
 * @author Maya Yagan
 */

public class SupplierDAO {
   
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
    
    public Set<Supplier> getSuppliers(){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Supplier> query = session.createQuery("SELECT DISTINCT s FROM Supplier s LEFT JOIN FETCH s.supplierProducts", Supplier.class);
            return new HashSet<>(query.getResultList());
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
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
