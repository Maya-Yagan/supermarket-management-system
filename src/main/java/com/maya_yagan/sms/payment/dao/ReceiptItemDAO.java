package com.maya_yagan.sms.payment.dao;

import com.maya_yagan.sms.payment.model.ReceiptItem;
import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.util.HibernateUtil;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Data Access Object (DAO) for the ReceiptItem entity.
 * Provides CRUD operations for ReceiptItem objects in the database.
 *
 * @author Maya Yagan
 */
public class ReceiptItemDAO {

    /**
     * Inserts a new receipt item into the database.
     *
     * @param receiptItem The receipt item to be inserted
     * @return true if insertion successful, false otherwise
     */
    public boolean insertReceiptItem(ReceiptItem receiptItem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(receiptItem);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a receipt item by its unique identifier.
     *
     * @param id The unique identifier of the receipt item
     * @return The receipt item with the specified id, or null if not found
     */
    public ReceiptItem getReceiptItemById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ReceiptItem> query = session.createQuery("FROM ReceiptItem ri WHERE ri.id = :id", ReceiptItem.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all receipt items associated with a specific receipt.
     *
     * @param receipt The receipt for which to retrieve items
     * @return List of receipt items belonging to the receipt
     */
    public List<ReceiptItem> getReceiptItemsByReceipt(Receipt receipt) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ReceiptItem> query = session.createQuery("FROM ReceiptItem ri WHERE ri.receipt = :receipt", ReceiptItem.class);
            query.setParameter("receipt", receipt);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates an existing receipt item in the database.
     *
     * @param receiptItem The receipt item with updated data
     */
    public void updateReceiptItem(ReceiptItem receiptItem) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ReceiptItem ri = session.get(ReceiptItem.class, receiptItem.getId());
            if (ri != null) {
                ri.setReceipt(receiptItem.getReceipt());
                ri.setProduct(receiptItem.getProduct());
                ri.setProductName(receiptItem.getProductName());
                ri.setUnitPrice(receiptItem.getUnitPrice());
                ri.setQuantity(receiptItem.getQuantity());
                ri.setDiscount(receiptItem.getDiscount());
                ri.setLineTotal(receiptItem.getLineTotal());
                session.update(ri);
                transaction.commit();
            }
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }

    /**
     * Deletes a receipt item from the database by its unique identifier.
     *
     * @param id The unique identifier of the receipt item to be deleted
     */
    public void deleteReceiptItem(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ReceiptItem receiptItem = session.get(ReceiptItem.class, id);
            if (receiptItem != null) {
                session.delete(receiptItem);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }
}
