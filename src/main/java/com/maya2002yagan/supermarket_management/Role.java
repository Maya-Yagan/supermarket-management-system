package com.maya2002yagan.supermarket_management;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "position")
    private String position;
    @Column(name = "privilegeLevel")
    private int privilegeLevel;
    @ManyToMany(mappedBy = "roles")
    private List<User> users;
    
    public Role(){}

    public Role(int id, String position, int privilegeLevel) {
        this.id = id;
        this.position = position;
        this.privilegeLevel = privilegeLevel;
    }

    public int getId() {
        return id;
    }

    public String getPosition() {
        return position;
    }

    public int getPrivilegeLevel() {
        return privilegeLevel;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setPrivilegeLevel(int privilegeLevel) {
        this.privilegeLevel = privilegeLevel;
    }

    @Override
    public String toString() {
        return "Role{" + "id=" + id + ", position=" + position + ", privilegeLevel=" + privilegeLevel + '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id == role.id;  // Compare by id or other unique identifier
    }
}
