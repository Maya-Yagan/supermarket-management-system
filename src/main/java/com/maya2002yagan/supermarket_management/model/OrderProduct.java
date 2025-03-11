package com.maya2002yagan.supermarket_management.model;

import javax.persistence.*;

/**
 *
 * @author maya2
 */
@Entity
@Table(name = "Order_Product")
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "amount")
    private int amount;
    
    public OrderProduct(){};

    public OrderProduct(Order order, Product product, int amount) {
        this.order = order;
        this.product = product;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    @Override
    public String toString(){
        return "OrderProduct{" +
                "id=" + id +
                ", order=" + order.getName() +
                ", product=" + product.getName() + 
                ", amount=" + amount + "}";
    }
}
