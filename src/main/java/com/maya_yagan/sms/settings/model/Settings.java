package com.maya_yagan.sms.settings.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Settings")
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "marketName")
    private String marketName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "moneyUnit")
    private String moneyUnit;

    @Column(name = "address")
    private String address;

    public Settings(){}

    public int getId() {
        return id;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMoneyUnit() {
        return moneyUnit;
    }

    public void setMoneyUnit(String moneyUnit) {
        this.moneyUnit = moneyUnit;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return id == settings.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
