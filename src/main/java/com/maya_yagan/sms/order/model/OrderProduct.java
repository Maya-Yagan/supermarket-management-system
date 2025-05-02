package com.maya_yagan.sms.order.model;

import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.product.model.Product;
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

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setOrder(Order order) {
        this.order = order;
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
