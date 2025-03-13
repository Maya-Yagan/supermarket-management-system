package com.maya2002yagan.supermarket_management.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Represents a product in the supermarket management system.
 * 
 * Each product has attributes such as name, price, production date,
 * expiration date, and belongs to a specific category.
 * 
 * @author Maya Yagan
 */
@Entity
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private float price;
    @Column(name = "productionDate")
    private LocalDate productionDate;
    @Column(name = "expirationDate")
    private LocalDate expirationDate;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductWarehouse> productWarehouses;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SupplierProduct> supplierProducts;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderProduct> orderProducts;
    @Enumerated(EnumType.STRING)
    @Column(name = "unit")
    private ProductUnit unit;
    
    /**
     * Default constructor
     * Needed for Hibernate to function properly.
     */
    public Product(){}

    /**
     * Constructor with product details.
     * 
     * @param name Name of the product
     * @param price Price of the product
     * @param productionDate Production date of the product
     * @param expirationDate Expiration date of the product
     * @param category Category to which the product belongs
     */
    public Product(String name, float price, LocalDate productionDate, LocalDate expirationDate, Category category, ProductUnit unit) {
        this.name = name;
        this.price = price;
        this.productionDate = productionDate;
        this.expirationDate = expirationDate;
        this.category = category;
        this.unit = unit;
    }

    /**
     * Returns the unique identifier of the product.
     * 
     * @return the id of the product
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of the product.
     * 
     * @return the name of the product
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the price of the product.
     * 
     * @return the price of the product
     */
    public float getPrice() {
        return price;
    }

    /**
     * Returns the production date of the product.
     * 
     * @return the production date
     */
    public LocalDate getProductionDate() {
        return productionDate;
    }

    /**
     * Returns the expiration date of the product.
     * 
     * @return the expiration date
     */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * Returns the category to which the product belongs.
     * 
     * @return the category of the product
     */
    public Category getCategory() {
        return category;
    }
    
    /**
     * Returns the set of warehouses where the product is stored.
     * 
     * @return set of product warehouses
     */
    public Set<ProductWarehouse> getProductWarehouses() {
        return productWarehouses;
    }
    
    /**
     * Returns the set of products of a supplier
     * 
     * @return set of supplier products
     */
    public Set<SupplierProduct> getSupplierProducts(){
        return supplierProducts;
    }
    
    /**
     * Returns the set of products of an order
     * 
     * @return set of order products
     */
    public Set<OrderProduct> getOrderProducts(){
        return orderProducts;
    }
    
    /**
     * Returns the unit of the product.
     * 
     * @return the unit of the product
     */
    public ProductUnit getUnit() {
        return unit;
    }

    /**
     * Sets the unit of a product
     * @param unit the unit to be set
     */
    public void setUnit(ProductUnit unit) {
        this.unit = unit;
    }

    /**
     * Sets the name of the product.
     * 
     * @param name Name to be set for the product
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the price of the product.
     * 
     * @param price Price to be set for the product
     */
    public void setPrice(float price) {
        this.price = price;
    }

    /**
     * Sets the production date of the product.
     * 
     * @param productionDate Production date to be set
     */
    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    /**
     * Sets the expiration date of the product.
     * 
     * @param expirationDate Expiration date to be set
     */
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Sets the category of the product.
     * 
     * @param category Category to be set for the product
     */
    public void setCategory(Category category) {
        this.category = category;
    }
    
    /**
     * Sets the warehouses associated with the product.
     * 
     * @param productWarehouses Set of product warehouses to be set
     */
    public void setProductWarehouses(Set<ProductWarehouse> productWarehouses) {
        this.productWarehouses = productWarehouses;
    }
    
    /**
     * Sets the products associated with a supplier.
     * 
     * @param supplierProducts Set of supplier products to be set
     */
    public void setSupplierProducts(Set<SupplierProduct> supplierProducts){
        this.supplierProducts = supplierProducts;
    }
    
    /**
     * Sets the products associated with an order.
     * 
     * @param orderProducts Set of order products to be set
     */
    public void setOrderProducts(Set<OrderProduct> orderProducts){
        this.orderProducts = orderProducts;
    }

    /**
     * Returns a string representation of the product,
     * including its name, price, and category.
     * 
     * @return string representation of the product
     */
    @Override
    public String toString() {
        return "Product{" + "name=" + name + ", price=" + price + ", category=" + category + '}';
    }
    
    /**
     * Checks if two products are equal based on their unique identifier.
     * 
     * @param obj the object to compare with
     * @return true if the products are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id == product.id;
    }

    /**
     * Returns the hash code of the product based on its unique identifier.
     * 
     * @return hash code of the product
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}