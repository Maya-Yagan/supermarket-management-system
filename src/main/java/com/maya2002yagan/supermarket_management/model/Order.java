
package com.maya2002yagan.supermarket_management.model;

import java.time.LocalDate;
import java.util.Set;
import javax.persistence.*;

/**
 *
 * @author Maya Yagan
 */
@Entity
@Table(name = "Orders") // "Order" is a reserved keyword in SQL, so we used "Orders" instead
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "order_date")
    private LocalDate orderDate;
    
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderProduct> orderProducts;
    
    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
    
    public Order(){};
    
    public Order(String name,
            LocalDate orderDate,
            LocalDate deliveryDate,
            Set<OrderProduct> orderProducts,
            Supplier supplier, 
            User user){
        this.name = name;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.orderProducts = orderProducts;
        this.supplier = supplier;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public Set<OrderProduct> getOrderProducts() {
        return orderProducts;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public User getUser() {
        return user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public void setOrderProducts(Set<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Order{" + "id=" + id + 
                ", name=" + name + 
                ", orderDate=" + orderDate + 
                ", deliveryDate=" + deliveryDate + 
                ", supplier=" + supplier.getName() + '}';
    }
}
