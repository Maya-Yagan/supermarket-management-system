package com.maya_yagan.sms.warehouse.model;

import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.product.model.Product;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Represents a warehouse in the supermarket management system.
 * Each warehouse has a name, capacity, and can hold a collection of products.
 * 
 * The class manages the relationship between warehouses and products through
 * the ProductWarehouse entity.
 * 
 * @author Maya Yagan
 */
@Entity
@Table(name = "Warehouse")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "capacity")
    private int capacity;
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductWarehouse> productWarehouses;
    
    /**
     * Default constructor
     * Needed for Hibernate to function properly.
     */
    public Warehouse(){}
    
    /**
     * Constructor with warehouse details.
     * 
     * @param name Name of the warehouse
     * @param capacity Capacity of the warehouse
     * @param productWarehouses Set of product-warehouse associations
     */
    public Warehouse(String name, int capacity, Set<ProductWarehouse> productWarehouses){
        this.name = name;
        this.capacity = capacity;
        this.productWarehouses = productWarehouses;
    }

    /**
     * Returns the unique identifier of the warehouse.
     * 
     * @return the id of the warehouse
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of the warehouse.
     * 
     * @return the name of the warehouse
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the capacity of the warehouse.
     * 
     * @return the capacity of the warehouse
     */
    public int getCapacity() {
        return capacity;
    }
    
    /**
     * Returns a set of products stored in the warehouse.
     * 
     * @return set of products in the warehouse
     */
    public Set<Product> getProducts() {
        return productWarehouses.stream()
                                .map(ProductWarehouse::getProduct)
                                .collect(Collectors.toSet());
    }
    
    /**
     * Returns the set of product-warehouse associations.
     * 
     * @return set of product-warehouse associations
     */
    public Set<ProductWarehouse> getProductWarehouses() {
        return productWarehouses;
    }

    /**
     * Sets the name of the warehouse.
     * 
     * @param name Name to be set for the warehouse
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the capacity of the warehouse.
     * 
     * @param capacity Capacity to be set for the warehouse
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    
    /**
     * Sets the product-warehouse associations.
     * 
     * @param productWarehouses Set of product-warehouse associations to be set
     */
    public void setProductWarehouses(Set<ProductWarehouse> productWarehouses) {
        this.productWarehouses = productWarehouses;
    }
    
    /**
     * Sets the products for the warehouse, initializing them with a default quantity.
     * 
     * @param products Set of products to be added
     * @param defaultQuantity Default quantity for each product
     */
    public void setProducts(Set<Product> products, int defaultQuantity){
        if(this.productWarehouses == null) this.productWarehouses = new HashSet<>();
        else this.productWarehouses.clear();
        for(Product product : products)
            this.productWarehouses.add(new ProductWarehouse(this, product, defaultQuantity));
    }

    /**
     * Returns a string representation of the warehouse,
     * including its id, name, and capacity.
     * 
     * @return string representation of the warehouse
     */
    @Override
    public String toString() {
        return name ;
    }
}
