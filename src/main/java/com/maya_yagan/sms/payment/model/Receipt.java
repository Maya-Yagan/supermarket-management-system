package com.maya_yagan.sms.payment.model;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.user.model.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "Receipt")
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private  String code;

    @Column(name = "dateTime", nullable = false)
    private LocalDateTime dateTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentMethod paymentMethod;   // CASH / CARD / …

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal paidAmount;         // what the customer gave

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal changeGiven;        // change returned

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ReceiptStatus status = ReceiptStatus.PENDING;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptItem> items = new ArrayList<>();

    public Receipt(){}

    public Receipt(String code,
                   LocalDateTime dateTime,
                   User cashier,
                   PaymentMethod paymentMethod) {
        this.code          = code;
        this.dateTime      = dateTime;
        this.cashier       = cashier;
        this.paymentMethod = paymentMethod;
    }

    public void addItem(ReceiptItem li) {
        items.add(li);
        li.setReceipt(this);
    }
    public void removeItem(ReceiptItem li) {
        items.remove(li);
        li.setReceipt(null);
    }

    @Transient
    public BigDecimal getSubTotal() {
        return items.stream()
                .map(i -> i.getUnitPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public BigDecimal getTotalCost() {
        return items.stream()
                .map(i -> {
                    BigDecimal lt = i.getLineTotal();
                    return lt != null ? lt :
                            i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public User getCashier() {
        return cashier;
    }

    public void setCashier(User cashier) {
        this.cashier = cashier;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getChangeGiven() {
        return changeGiven;
    }

    public void setChangeGiven(BigDecimal changeGiven) {
        this.changeGiven = changeGiven;
    }

    public ReceiptStatus getStatus() {
        return status;
    }

    public void setStatus(ReceiptStatus status) {
        this.status = status;
    }

    public List<ReceiptItem> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItem> items) {
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Receipt receipt = (Receipt) o;
        return Objects.equals(id, receipt.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

