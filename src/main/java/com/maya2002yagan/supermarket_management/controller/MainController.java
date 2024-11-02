package com.maya2002yagan.supermarket_management.controller;

import com.maya2002yagan.supermarket_management.service.AuthenticationService;
import com.maya2002yagan.supermarket_management.util.HibernateUtil;
import com.maya2002yagan.supermarket_management.model.User;
import java.io.IOException;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;

/**
 * Controller class for the main login screen of the application.
 * 
 * Note: This controller uses Hibernate for database access
 * and depends on the AuthenticationService for password verification.
 * 
 * @author Maya Yagan
 */
public class MainController implements Initializable {
    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Text statusText;
    
    /**
     * Initializes the controller class.
     * Sets up an action event for the password field to trigger
     * the login button when the Enter key is pressed.
     *
     * @param location  The location of the FXML resource.
     * @param resources The resources to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        passwordField.setOnAction(event -> loginButton.fire());
    } 
    
    /**
     * Handles the login button click event.
     * Validates user input and attempts to authenticate the user 
     * with the provided email and password. If authentication is 
     * successful, it loads the home page; otherwise, it displays 
     * an error message.
     */
    @FXML
    public void handleLoginButtonClick(){
        String email = emailField.getText();
        String password = passwordField.getText();
        
        if(email.isEmpty() || password.isEmpty()){
            statusText.setText("Please fill in the fields");
            return;
        }
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = session.createQuery("FROM User WHERE email = :email", User.class)
                .setParameter("email", email)
                .uniqueResult();
        AuthenticationService auth = new AuthenticationService(email, password);
        if (auth.authenticate(user)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
                Parent homeRoot = loader.load();
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(homeRoot));
                stage.setTitle("Home Page");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            statusText.setText("Wrong email or password");
        }
        session.close();
    }     
}
