package com.maya_yagan.sms.finance.model;

import com.maya_yagan.sms.user.model.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "CashBox")
public class CashBox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "totalBalance", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalBalance;

    @Column(name = "openedAt")
    private LocalDateTime openedAt;

    @Column(name = "closedAt")
    private  LocalDateTime closedAt;

    @OneToMany(mappedBy = "cashBox", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FinancialRecord> records;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CashBoxStatus status = CashBoxStatus.CLOSED;

    @ManyToOne
    @JoinColumn(name = "opened_by", nullable = false)
    private User openedBy;

    @ManyToOne
    @JoinColumn(name = "closed_by")
    private User closedBy;

    public CashBox(){}

    public Long getId() {
        return id;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public List<FinancialRecord> getRecords() {
        return records;
    }

    public void setRecords(List<FinancialRecord> records) {
        this.records = records;
    }

    public CashBoxStatus getStatus() {
        return status;
    }

    public void setStatus(CashBoxStatus status) {
        this.status = status;
    }

    public User getOpenedBy() {
        return openedBy;
    }

    public void setOpenedBy(User openedBy) {
        this.openedBy = openedBy;
    }

    public User getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(User closedBy) {
        this.closedBy = closedBy;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CashBox cashBox = (CashBox) o;
        return Objects.equals(id, cashBox.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
