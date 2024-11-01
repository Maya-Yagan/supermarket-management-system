package com.maya2002yagan.supermarket_management;

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

public class MainController implements Initializable {
    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Text statusText;
    
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
            // Successful login
            try {
                // Load the home page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
                Parent homeRoot = loader.load();

                // Set up a new scene for the home page
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
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //emailField.requestFocus();
        passwordField.setOnAction(event -> loginButton.fire());
    }    
    
}
