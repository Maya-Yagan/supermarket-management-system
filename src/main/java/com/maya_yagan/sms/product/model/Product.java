package com.maya_yagan.sms.product.model;

import com.maya_yagan.sms.order.model.OrderProduct;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
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
    @Column(name = "expirationDate", nullable = true)
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
    @Column(name = "discount", nullable = true)
    private Float discount = 0f;
    @Column(name = "barcode", unique = true, nullable = true)
    private String barcode;
    @Column(name = "minLimit")
    private int minLimit;

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
     * @param unit Unit of the product
     */
    public Product(String name, float price, LocalDate productionDate, LocalDate expirationDate, Category category, ProductUnit unit, Float discount, String barcode) {
        this.name = name;
        this.price = price;
        this.productionDate = productionDate;
        this.expirationDate = expirationDate;
        this.category = category;
        this.unit = unit;
        this.discount = (discount != null) ? discount : 0.0f;
        this.barcode = barcode;

    }

    /**
     * Constructor without the expiry date
     * @param name Name of the product
     * @param price Price of the product
     * @param productionDate Production date of the product
     * @param category Category to which the product belongs
     * @param unit Unit of the product
     */
    public Product(String name, float price, LocalDate productionDate, Category category, ProductUnit unit) {
        this.name = name;
        this.price = price;
        this.productionDate = productionDate;
        this.category = category;
        this.unit = unit;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
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

    public Set<ProductWarehouse> getProductWarehouses() {
        return productWarehouses;
    }

    public Set<SupplierProduct> getSupplierProducts(){
        return supplierProducts;
    }

    public Set<OrderProduct> getOrderProducts(){
        return orderProducts;
    }

    public ProductUnit getUnit() {
        return unit;
    }

    public void setUnit(ProductUnit unit) {
        this.unit = unit;
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

    public void setProductWarehouses(Set<ProductWarehouse> productWarehouses) {
        this.productWarehouses = productWarehouses;
    }

    public int getMinLimit() {
        return minLimit;
    }

    public void setMinLimit(int minLimit) {
        this.minLimit = minLimit;
    }

    public void setSupplierProducts(Set<SupplierProduct> supplierProducts){
        this.supplierProducts = supplierProducts;
    }

    public void setOrderProducts(Set<OrderProduct> orderProducts){
        this.orderProducts = orderProducts;
    }

    @Override
    public String toString() {
        return "Product{" + "name=" + name + ", price=" + price + ", category=" + category + ", barcode=" + barcode + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}