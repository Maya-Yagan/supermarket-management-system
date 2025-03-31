package com.maya_yagan.sms.warehouse.model;

import com.maya_yagan.sms.product.model.Product;
import javax.persistence.*;

/**
 * Represents the association between a product and a warehouse in the supermarket management system.
 * Each entry indicates the amount of a specific product available in a specific warehouse.
 * 
 * This class models a many-to-one relationship with both the Product and Warehouse entities.
 * @author Maya Yagan
 */
@Entity
@Table(name = "Product_Warehouse")
public class ProductWarehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "amount")
    private int amount;

    /**
     * Default constructor
     * Needed for Hibernate to function properly.
     */
    public ProductWarehouse() {}

    /**
     * Constructor with warehouse, product, and amount.
     * 
     * @param warehouse Warehouse where the product is stored
     * @param product Product stored in the warehouse
     * @param amount Amount of the product available
     */
    public ProductWarehouse(Warehouse warehouse, Product product, int amount) {
        this.warehouse = warehouse;
        this.product = product;
        this.amount = amount;
    }

    /**
     * Returns the unique identifier of the product-warehouse entry.
     * 
     * @return the id of the product-warehouse entry
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the warehouse where the product is stored.
     * 
     * @return the warehouse of the product
     */
    public Warehouse getWarehouse() {
        return warehouse;
    }

    /**
     * Sets the warehouse for the product-warehouse entry.
     * 
     * @param warehouse Warehouse to be set
     */
    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    /**
     * Returns the product stored in the warehouse.
     * 
     * @return the product stored
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Sets the product for the product-warehouse entry.
     * 
     * @param product Product to be set
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Returns the amount of the product available in the warehouse.
     * 
     * @return the amount of the product
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the amount of the product available in the warehouse.
     * 
     * @param amount Amount to be set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Returns a string representation of the product-warehouse entry,
     * including the warehouse name, product name, and amount.
     * 
     * @return string representation of the product-warehouse entry
     */
    @Override
    public String toString() {
        return "ProductWarehouse{" +
               "id=" + id +
               ", warehouse=" + warehouse.getName() +
               ", product=" + product.getName() +
               ", amount=" + amount +
               '}';
    }
}
