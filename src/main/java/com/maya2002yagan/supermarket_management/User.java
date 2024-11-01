package com.maya2002yagan.supermarket_management;

import java.time.LocalDate;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import org.mindrot.jbcrypt.BCrypt;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "firstName")
    private String firstName;
    @Column(name = "lastName")
    private String lastName;
    @Column(name = "tcNumber")
    private String tcNumber;
    @Column(name = "birthDate")
    private LocalDate birthDate;
    @Column(name = "gender")
    private String gender;
    @Column(name = "email")
    private String email;
    @Column(name = "phoneNumber")
    private String phoneNumber;
    @Column(name = "salary")
    private float salary;
    @Column(name = "password")
    private String password;
    @Column(name = "startDate")
    private LocalDate startDate;
    @ManyToMany
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;
    @Column(name = "isPartTime")
    private Boolean isPartTime;
    @Column(name = "isFullTime")
    private Boolean isFullTime;
    
    public User(){}

    public User(String firstName, String lastName, String email, String password, List<Role> roles) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = hashPassword(password);
        this.roles = roles;
    }

    public User(String firstName, String lastName, String tcNumber, LocalDate birthDate, String gender, String email, String phoneNumber, float salary, String password, List<Role> roles, Boolean isPartTime, Boolean isFullTime) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.tcNumber = tcNumber;
        this.birthDate = birthDate;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.salary = salary;
        this.password = password;
        this.roles = roles;
        this.isPartTime = isPartTime;
        this.isFullTime = isFullTime;
        this.startDate = LocalDate.now();
    }
    
    
    
    private String hashPassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt());
    } 
    
    public boolean checkPassword(String password){
        return BCrypt.checkpw(password, this.password);
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTcNumber() {
        return tcNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public float getSalary() {
        return salary;
    }

    public String getPassword() {
        return password;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public Boolean getIsPartTime() {
        return isPartTime;
    }

    public Boolean getIsFullTime() {
        return isFullTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setTcNumber(String tcNumber) {
        this.tcNumber = tcNumber;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    public void setPassword(String password) {
        this.password = hashPassword(password);
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void setIsFullTime(Boolean isFullTime) {
        this.isFullTime = isFullTime;
        if (isFullTime != null && isFullTime) {
            this.isPartTime = false;
        }
    }

    public void setIsPartTime(Boolean isPartTime) {
        this.isPartTime = isPartTime;
        if (isPartTime != null && isPartTime) {
            this.isFullTime = false;
        }
    }

    
    public String getEmploymentType(){
        if(isPartTime) return "Part time";
        else if(isFullTime) return "Full time";
        else return "Unspecified";
    }

    @Override
    public String toString() {
        return "User{" + "firstName=" + firstName + ", lastName=" + lastName + ", role=" + roles.toString() + '}';
    }
}
