package com.maya_yagan.sms.supplier.model;

import com.maya_yagan.sms.product.model.Product;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Represents the association between a supplier and a product in the supermarket management system.
 * Each entry indicates the products offered by a specific supplier.
 * 
 * This class models a many-to-one relationship with both the Supplier and Product entities.
 * @author Maya Yagan
 */
@Entity
@Table(name = "Supplier_Product")
public class SupplierProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "price")
    private float price;
    
    /**
     * Default constructor
     * Needed for Hibernate to function properly.
     */
    public SupplierProduct(){}

    /**
     * Constructor with product, supplier and price
     * 
     * @param product Product related to a supplier
     * @param supplier Supplier that offers a product
     * @param price Price of the product by the supplier
     */
    public SupplierProduct(Product product, Supplier supplier, float price) {
        this.product = product;
        this.supplier = supplier;
        this.price = price;
    }

    /**
     * Returns the unique identifier of the supplier-product entry.
     * 
     * @return the id of the supplier-product entry
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the product offered by the supplier
     * 
     * @return the offered product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Returns the supplier
     * 
     * @return the supplier
     */
    public Supplier getSupplier() {
        return supplier;
    }

    /**
     * Returns the price of a product offered by a supplier
     * 
     * @return the price of a product offered by a supplier
     */
    public float getPrice() {
        return price;
    }

    /**
     * Sets the product offered by a supplier
     * 
     * @param product the product to be set
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Sets a supplier
     * 
     * @param supplier the supplier to be set
     */
    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    /**
     * Sets the price of a product
     * 
     * @param price the price of the product to be set
     */
    public void setPrice(float price) {
        this.price = price;
    }

    /**
     * Returns a string representation of the supplier-product entry,
     * including the product name, supplier name, and price.
     * 
     * @return string representation of the supplier-product entry
     */
    @Override
    public String toString() {
        return "SupplierProduct{" + 
                "product=" + product.getName() +
                ", supplier=" + supplier.getName() + 
                ", price=" + price + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplierProduct)) return false;
        SupplierProduct other = (SupplierProduct) o;

        // 1) If both have DB ids, compare by id
        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        // 2) Otherwise compare by business key (supplier + product)
        return Objects.equals(supplier, other.supplier)
                && Objects.equals(product,  other.product);
    }

    @Override
    public int hashCode() {
        return (id != null)
                ? id.hashCode()                         // persisted entity
                : Objects.hash(supplier, product);      // transient entity
    }
}
