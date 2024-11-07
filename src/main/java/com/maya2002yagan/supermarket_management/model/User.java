package com.maya2002yagan.supermarket_management.model;

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

/**
 * Represents a user in the supermarket management system.
 * 
 * This entity is mapped to the "Users" table in the database
 * and contains user information, including personal details, 
 * roles, and employment type. Passwords are stored securely 
 * using hashing.
 * 
 * @author Maya Yagan
 */
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
    
    /**
     * Default constructor for the User class.
     * Needed for Hibernate to function properly.
     */
    public User(){}

    /**
     * Constructs a User with essential attributes for creation.
     * Used for testing
     * 
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param password the user's password
     * @param roles the roles assigned to the user
     */
    public User(String firstName, String lastName, String email, String password, List<Role> roles) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = hashPassword(password);
        this.roles = roles;
    }

    /**
     * Constructs a User with all attributes specified.
     * 
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param tcNumber the user's national identification number
     * @param birthDate the user's date of birth
     * @param gender the user's gender
     * @param email the user's email address
     * @param phoneNumber the user's phone number
     * @param salary the user's salary
     * @param password the user's password
     * @param roles the roles assigned to the user
     * @param isPartTime indicates if the user is part-time
     * @param isFullTime indicates if the user is full-time
     */
    public User(String firstName, String lastName, String tcNumber, LocalDate birthDate, String gender, String email, String phoneNumber, float salary, String password, List<Role> roles, Boolean isPartTime, Boolean isFullTime) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.tcNumber = tcNumber;
        this.birthDate = birthDate;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.salary = salary;
        this.password = hashPassword(password);
        this.roles = roles;
        this.isPartTime = isPartTime;
        this.isFullTime = isFullTime;
        this.startDate = LocalDate.now();
    }
    
    /**
     * Hashes the provided password using BCrypt.
     * 
     * @param password the password to hash
     * @return the hashed password
     */
    private String hashPassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt());
    } 
    
    /**
     * Checks if the provided password matches the stored password.
     * 
     * @param password the password to check
     * @return true if the passwords match, false otherwise
     */
    public boolean checkPassword(String password){
        return BCrypt.checkpw(password, this.password);
    }

    /**
     * Returns the unique identifier of the user.
     * 
     * @return the user's id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the user's first name.
     * 
     * @return the user's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the user's last name.
     * 
     * @return the user's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the user's TC number.
     * 
     * @return the user's TC number
     */
    public String getTcNumber() {
        return tcNumber;
    }

    /**
     * Returns the user's date of birth.
     * 
     * @return the user's birth date
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /**
     * Returns the user's gender.
     * 
     * @return the user's gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Returns the user's email address.
     * 
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the user's phone number.
     * 
     * @return the user's phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Returns the user's salary.
     * 
     * @return the user's salary
     */
    public float getSalary() {
        return salary;
    }

    /**
     * Returns the user's password (hashed).
     * 
     * @return the user's hashed password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user's start date of employment.
     * 
     * @return the user's start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Returns the roles assigned to the user.
     * 
     * @return the list of roles
     */
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Returns whether the user is part-time.
     * 
     * @return true if the user is part-time, false otherwise
     */
    public Boolean getIsPartTime() {
        return isPartTime;
    }

    /**
     * Returns whether the user is full-time.
     * 
     * @return true if the user is full-time, false otherwise
     */
    public Boolean getIsFullTime() {
        return isFullTime;
    }

    /**
     * Sets the unique identifier for the user.
     * 
     * @param id the unique identifier to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the user's first name.
     * 
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the user's last name.
     * 
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the user's TC number.
     * 
     * @param tcNumber the TC number to set
     */
    public void setTcNumber(String tcNumber) {
        this.tcNumber = tcNumber;
    }

    /**
     * Sets the user's date of birth.
     * 
     * @param birthDate the birth date to set
     */
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * Sets the user's gender.
     * 
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Sets the user's email address.
     * 
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the user's phone number.
     * 
     * @param phoneNumber the phone number to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Sets the user's salary.
     * 
     * @param salary the salary to set
     */
    public void setSalary(float salary) {
        this.salary = salary;
    }

    /**
     * Sets the user's password and hashes it.
     * 
     * @param plaintextPassword the password to set
     */
    public void setPassword(String plaintextPassword) {
         if (plaintextPassword == null || plaintextPassword.isEmpty()) {
        System.out.println("Password field is empty, skipping hash.");
        return;
    }
    
    // Only hash if it's a new plaintext password
    if (!checkPassword(plaintextPassword)) {
        System.out.println("Setting new password hash.");
        this.password = hashPassword(plaintextPassword);
    } else {
        System.out.println("Password unchanged, skipping rehash.");
    }
    }

    /**
     * Sets the user's start date of employment.
     * 
     * @param startDate the start date to set
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    /**
     * Sets the roles assigned to the user.
     * 
     * @param roles the list of roles to set
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    /**
     * Sets whether the user is full-time.
     * 
     * @param isFullTime true if the user is full-time, false otherwise
     */
    public void setIsFullTime(Boolean isFullTime) {
        this.isFullTime = isFullTime;
        if (isFullTime != null && isFullTime) {
            this.isPartTime = false;
        }
    }

    /**
     * Sets whether the user is part-time.
     * 
     * @param isPartTime true if the user is part-time, false otherwise
     */
    public void setIsPartTime(Boolean isPartTime) {
        this.isPartTime = isPartTime;
        if (isPartTime != null && isPartTime) {
            this.isFullTime = false;
        }
    }

    /**
     * Returns the employment type of the user as a string.
     * 
     * @return "Part time" if the user is part time, "Full time" if full time, or "Unspecified"
     */
    public String getEmploymentType(){
        if(isPartTime) return "Part time";
        else if(isFullTime) return "Full time";
        else return "Unspecified";
    }

    /**
     * Returns a string representation of the User object.
     * 
     * @return a string containing the user's first name, last name, and roles
     */
    @Override
    public String toString() {
        return "User{" + "firstName=" + firstName + ", lastName=" + lastName + ", role=" + roles.toString() + '}';
    }
}
