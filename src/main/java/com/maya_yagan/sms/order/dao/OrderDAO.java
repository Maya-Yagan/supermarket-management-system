package com.maya_yagan.sms.order.dao;

import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.order.model.OrderProduct;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.util.HibernateUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the Order entity.
 * Provides methods to perform CRUD operations on Order data.
 * 
 * @author Maya Yagan
 */
public class OrderDAO {
    /**
     * Inserts a new order into the database.
     * 
     * @param order The order to be inserted
     */
    public void insertOrder(Order order){
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.save(order);
            transaction.commit();
        } catch(Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    /**
     * Retrieves an order by its unique identifier.
     * 
     * @param id The unique identifier of the order
     * @return The order with the specified id 
     */
    public Order getOrderById(int id){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Order> query = session.createQuery(
                    "FROM Order o " +
                    "LEFT JOIN FETCH o.orderProducts " + 
                    "LEFT JOIN FETCH o.supplier s " +
                    "LEFT JOIN FETCH s.supplierProducts " +
                    "WHERE o.id = :id",
                    Order.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Retrieves all distinct orders from the database.
     * 
     * @return A set of all orders
     */
    public Set<Order> getOrders(){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Order> query = session.createQuery(
                    "SELECT DISTINCT o FROM Order o " + 
                    "LEFT JOIN FETCH o.orderProducts " + 
                    "LEFT JOIN FETCH o.supplier s " +
                    "LEFT JOIN FETCH s.supplierProducts",
                    Order.class);
            return new HashSet<>(query.getResultList());
        } catch(Exception e){
            e.printStackTrace();
            return Collections.emptySet();
        }
    }
    
    /**
     * Updates an existing order in the database
     * 
     * @param order The order to be updated 
     */
    public void updateOrder(Order order){
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.merge(order);
            transaction.commit();
        } catch(Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    public void deleteProductFromOrder(Order order, Product product){
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Query<OrderProduct> query = session.createQuery(
                    "FROM OrderProduct op WHERE op.order = :order AND op.product = :product",
                    OrderProduct.class
            );
            query.setParameter("order", order);
            query.setParameter("product", product);
            OrderProduct orderProduct = query.uniqueResult();
            if(orderProduct != null) session.delete(orderProduct);
            transaction.commit();
        } catch(Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes an order by its unique identifier 
     * 
     * @param id The unique identifier of the order to be deleted
     */
    public void deleteOrder(int id){
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Order order = session.get(Order.class, id);
            if(order != null) session.delete(order);
            transaction.commit();
        } catch(Exception e){
            if(transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
