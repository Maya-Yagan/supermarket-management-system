package com.maya2002yagan.supermarket_management.model;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.CascadeType;


/**
 * Represents a product category in the supermarket management system.
 * Each category can contain multiple products.
 * 
 * @author Maya Yagan
 */
@Entity
@Table(name = "Category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<Product> products; //set won't allow duplicates, whereas list does, so we didn't use it
    
    /**
     * Default constructor
     * Needed for Hibernate to function properly.
     */
    public Category(){}

    /**
     * Constructor with category name.
     * 
     * @param name Name of the category
     */
    public Category(String name) {
        this.name = name;
    }

    /**
     * Returns the unique identifier of the category.
     * 
     * @return the id of the category
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of the category.
     * 
     * @return the name of the category
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the set of products associated with the category.
     * 
     * @return set of products
     */
    public Set<Product> getProducts() {
        return products;
    }

    /**
     * Sets the products associated with the category.
     * 
     * @param products Set of products to be associated with this category
     */
    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    /**
     * Sets the unique identifier of the category.
     * 
     * @param id Unique identifier to be set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the name of the category.
     * 
     * @param name Name to be set for the category
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a string representation of the category, which is its name.
     * 
     * @return the name of the category
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Checks if two categories are equal based on their unique identifier.
     * 
     * @param obj the object to compare with
     * @return true if the categories are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return id == category.id;
    }
}
