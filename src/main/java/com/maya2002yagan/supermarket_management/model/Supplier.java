package com.maya2002yagan.supermarket_management.model;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents a supplier in the supermarket management system.
 * 
 * @author Maya Yagan
 */
@Entity
@Table(name = "Supplier")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @Column(name = "phoneNumber")
    private String phoneNumber;
    @Column(name = "description")
    private String description;
    
    /**
     * Default constructor
     * Needed for Hibernate to function properly.
     */
    public Supplier(){};

    /**
     * Constructor with supplier details.
     * 
     * @param name the name of the supplier
     * @param email the email of the supplier
     * @param phoneNumber the phone number of the supplier
     */
    public Supplier(String name, String email, String phoneNumber, String description) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.description = description;
    }

    /**
     * Returns the id of the supplier.
     * 
     * @return the id of the supplier
     */
    public int getId() {
        return id;
    }
    
    /**
     * Returns the name of the supplier.
     * 
     * @return the name of the supplier
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the email of the supplier.
     * 
     * @return the email of the supplier
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the phone number of the supplier.
     * 
     * @return the phone number of the supplier
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    /**
     * Returns the description of the supplier
     * 
     * @return the description of the supplier 
     */
    public String getDescription(){
        return description;
    }

    /**
     * Sets the name of the supplier.
     * 
     * @param name the name to be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the email of the supplier.
     * 
     * @param email the email to be set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the phone number of the supplier.
     * 
     * @param phoneNumber the phone number to be set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    /**
     * Sets the description of the supplier.
     * 
     * @param description the description of the supplier
     */
    public void setDescription(String description){
        this.description = description;
    }

    /**
     * Returns a string representation of the supplier,
     * 
     * @return string representation of the supplier
     */
    @Override
    public String toString() {
        return "Supplier{" + "name=" + name + ", email=" + email + ", phoneNumber=" + phoneNumber + ", Description=" + description + '}';
    }
     
    /**
     * Checks if two suppliers are equal based on their unique identifier.
     * 
     * @param obj the object to compare with
     * @return true if the suppliers are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Supplier supplier = (Supplier) obj;
        return id == supplier.id;
    }
    
    /**
     * Returns the hash code of the supplier based on its unique identifier.
     * 
     * @return hash code of the supplier
     */
    @Override
    public int hashCode(){
        return Objects.hash(id);
    }
}
