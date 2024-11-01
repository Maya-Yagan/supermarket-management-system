package com.maya2002yagan.supermarket_management;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Main.fxml"));
        primaryStage.setTitle("Supermarket Manager");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        System.out.println("Hola amigo!");
        //insertUser();
        //hashCurrentUsers();
        //login();
        //User user = new User("")
        launch(args); 
        //UserDAO dao = new UserDAO();
//        Role role1 = new Role(3, "depot_worker", 1);
//        Role role0 = new Role(4, "cashier", 0);
//        List<Role> userRoles = new ArrayList<>();
//        userRoles.add(role0);
//        userRoles.add(role1);
//        User user = new User("John", "Smith", "john@example.com", "123", userRoles);
//        dao.insertUser(user);
//        User user = dao.getUserById(2);
//        if(user != null){
//            dao.deleteUser(2);
//        }
//        try {
//            List<User> users = dao.getUsers();
//            for (User userl : users) {
//                System.out.println(userl);
//            }
//        } finally {
//        HibernateUtil.getSessionFactory().close(); // Close SessionFactory explicitly
//        System.exit(0);
//        }
    }
    
    public static void login(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the email: ");
        String email = scanner.nextLine();
        System.out.println("Enter the password: ");
        String password = scanner.nextLine();
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = session.createQuery("FROM User WHERE email = :email", User.class)
                .setParameter("email", email).uniqueResult();
        AuthenticationService auth = new AuthenticationService(email, password);
        if(auth.authenticate(user))
            System.out.println("Welcome " + user.getFirstName());
        else System.out.println("Wrong password or email!");
        session.close();
    }
    
    public static void hashCurrentUsers(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List<User> users = session.createQuery("FROM User", User.class).list();
        for (User user : users) {
            String plainPassword = user.getPassword();
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            
            user.setPassword(hashedPassword);
            session.update(user);
        }
        session.getTransaction().commit();
        System.out.println(session.get(User.class,1));
        session.close();
    }
    
    public static void insertUser(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Role role2 = new Role(2, "accountant", 2);
        Role role1 = new Role(3, "depot_worker", 1);
        Role role0 = new Role(4, "cashier", 0);
        List<Role> roles = new ArrayList<>();
        roles.add(role0);
        roles.add(role1);
        User user = new User("Mark", "Doe", "mark@example.com", "123", roles);
        session.save(user);
        session.getTransaction().commit();
        System.out.println(session.get(User.class,1));
        session.close();
    }
}
