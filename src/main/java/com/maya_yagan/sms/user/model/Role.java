package com.maya_yagan.sms.user.model;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;


/**
 * Represents a role in the supermarket management system.
 * 
 * This entity is mapped to the "Role" table in the database
 * and defines user roles with associated privileges. Each role 
 * can be associated with multiple users.
 * 
 * @author Maya Yagan
 */
@Entity
@Table(name = "Role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "privilegeLevel")
    private int privilegeLevel;
    
    @ManyToMany(mappedBy = "roles")
    private List<User> users;
    
    /**
     * Default constructor for the Role class.
     * Needed for Hibernate to function properly.
     */
    public Role(){}

    /**
     * Constructs a Role with the specified id, position, and privilege level.
     * 
     * @param id the unique identifier for the role
     * @param name the name or title of the role
     * @param privilegeLevel the level of privileges associated with the role
     */
    public Role(int id, String name, int privilegeLevel) {
        this.id = id;
        this.name = name;
        this.privilegeLevel = privilegeLevel;
    }

    /**
     * Returns the unique identifier of the role.
     * 
     * @return the id of the role
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the position of the role.
     * 
     * @return the position of the role
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the privilege level of the role.
     * 
     * @return the privilege level of the role
     */
    public int getPrivilegeLevel() {
        return privilegeLevel;
    }

    /**
     * Sets the unique identifier for the role.
     * 
     * @param id the unique identifier to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the position for the role.
     * 
     * @param name the position to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the privilege level for the role.
     * 
     * @param privilegeLevel the privilege level to set
     */
    public void setPrivilegeLevel(int privilegeLevel) {
        this.privilegeLevel = privilegeLevel;
    }

    /**
     * Returns a string representation of the Role object.
     * 
     * @return a string that contains the role's id, position, and privilege level
     */
    @Override
    public String toString() {
        return "Role{" + "id=" + id + ", position=" + name + ", privilegeLevel=" + privilegeLevel + '}';
    }
    
    /**
     * Compares this role to another object for equality.
     * 
     * @param o the object to compare with
     * @return true if the object is equal to this role, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id == role.id;
    }
}
