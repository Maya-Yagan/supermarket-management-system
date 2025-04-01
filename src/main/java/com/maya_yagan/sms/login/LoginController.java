package com.maya_yagan.sms.login;

import atlantafx.base.controls.PasswordTextField;
import com.maya_yagan.sms.util.HibernateUtil;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.ValidationService;
import java.io.IOException;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controller class for the main login screen of the application.
 * 
 * Note: This controller uses Hibernate for database access
 * and depends on the AuthenticationService for password verification.
 * 
 * @author Maya Yagan
 */
public class LoginController implements Initializable {
    @FXML private TextField emailField;
    @FXML private PasswordTextField passwordField;
    @FXML private Button loginButton;
    @FXML private Text statusText;
    
    private final ValidationService validationService = new ValidationService();
    private final Session session = HibernateUtil.getSessionFactory().openSession();
    
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
        emailField.setOnAction(event -> passwordField.requestFocus());
        emailField.textProperty().addListener((obs, old, newVal) -> {
            if (validationService.isValidEmail(newVal)) 
                emailField.setStyle("-fx-border-color: green;"); // Valid input
            else 
                emailField.setStyle("-fx-border-color: red;");   // Invalid input
        });
        passwordField.setPrefWidth(250);
        FontIcon icon = new FontIcon(Feather.EYE_OFF);
        icon.setCursor(Cursor.HAND);
        icon.setOnMouseClicked(e -> {
            icon.setIconCode(passwordField.getRevealPassword() ? Feather.EYE_OFF : Feather.EYE);
            passwordField.setRevealPassword(!passwordField.getRevealPassword());
        });
        passwordField.setRight(icon);
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
        String password = passwordField.getPassword();

        if(email.isEmpty() || password.isEmpty()){
            statusText.setText("Please fill in the empty fields");
            return;
        }
        User user = session.createQuery("FROM User WHERE email = :email", User.class)
                .setParameter("email", email)
                .uniqueResult();
        AuthenticationService auth = new AuthenticationService(email, password);
        if (auth.authenticate(user)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomePage.fxml"));
                Parent homeRoot = loader.load();
                Scene scene = loginButton.getScene();
                scene.setRoot(homeRoot);
                Stage stage = (Stage) scene.getWindow();
                stage.setTitle("Home Page");
                stage.setMaximized(true);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            statusText.setText("Wrong email or password!");
        }
    }     
}
