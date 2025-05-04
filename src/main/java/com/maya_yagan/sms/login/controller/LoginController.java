package com.maya_yagan.sms.login;

import atlantafx.base.controls.PasswordTextField;
import com.maya_yagan.sms.homepage.HomePageController;
import com.maya_yagan.sms.util.*;

import javafx.scene.layout.StackPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controller class for the main login screen of the application.
 *
 * @author Maya Yagan
 */
public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordTextField passwordField;
    @FXML private Button loginButton;
    @FXML private StackPane stackPane;

    private final LoginService loginService = new LoginService();
    private final ValidationService validationService = new ValidationService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureEmailField();
        configurePasswordField();
        configureLoginButton();
    }

    private void configureEmailField() {
        emailField.setOnAction(event -> passwordField.requestFocus());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            String borderColor = validationService.isValidEmail(newVal) ? "green" : "red";
            emailField.setStyle("-fx-border-color: " + borderColor + ";");
        });
    }

    private void configurePasswordField() {
        passwordField.setOnAction(event -> loginButton.fire());
        passwordField.setPrefWidth(250);

        FontIcon eyeIcon = new FontIcon(Feather.EYE_OFF);
        eyeIcon.setCursor(Cursor.HAND);
        eyeIcon.setOnMouseClicked(e -> togglePasswordReveal(eyeIcon));
        passwordField.setRight(eyeIcon);
    }

    private void togglePasswordReveal(FontIcon icon) {
        boolean isRevealed = passwordField.getRevealPassword();
        icon.setIconCode(isRevealed ? Feather.EYE_OFF : Feather.EYE);
        passwordField.setRevealPassword(!isRevealed);
    }

    private void configureLoginButton() {
        loginButton.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getPassword();

        try {
            if (loginService.login(email, password))
                navigateToHomePage();
        } catch (CustomException e) {
            ExceptionHandler.handleException(e);
        }
    }

    private void navigateToHomePage() {
        ViewUtil.switchView(
                "/view/HomePage.fxml",
                (HomePageController controller) -> {
                    controller.setStackPane(stackPane);
                },
                stackPane,
                "Home Page"
        );
    }
}
