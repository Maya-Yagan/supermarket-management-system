package com.maya2002yagan.supermarket_management.model;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
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
    
    public Product(){}

    public Product(String name, float price, LocalDate productionDate, LocalDate expirationDate, Category category) {
        this.name = name;
        this.price = price;
        this.productionDate = productionDate;
        this.expirationDate = expirationDate;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getPrice() {
        return price;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public Category getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Product{" + "name=" + name + ", price=" + price + ", category=" + category + '}';
    }
}
