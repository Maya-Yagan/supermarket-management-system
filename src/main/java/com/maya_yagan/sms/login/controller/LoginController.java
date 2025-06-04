package com.maya_yagan.sms.login.controller;

import atlantafx.base.controls.PasswordTextField;
import com.maya_yagan.sms.common.ValidationService;
import com.maya_yagan.sms.homepage.controller.HomePageController;
import com.maya_yagan.sms.login.service.LoginService;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.AttendanceService;
import com.maya_yagan.sms.user.service.UserService;
import com.maya_yagan.sms.util.*;

import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.net.URL;
import java.time.LocalDate;
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

    private static LocalDate lastAttendanceInsertDate  = null;
    private final LoginService loginService = new LoginService();
    private final ValidationService validationService = new ValidationService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LocalDate today = LocalDate.now();
        configureEmailField();
        configurePasswordField();
        configureLoginButton();
        if(lastAttendanceInsertDate == null || !lastAttendanceInsertDate.equals(today)){
            attendanceService.createBlankAttendancesForToday();
            lastAttendanceInsertDate = today;
        }
        if(userService.getAllUsers().isEmpty()){
            try{
                userService.insertDefaultUser();
            } catch (RuntimeException e){
                String message = e.getMessage() != null ? e.getMessage() : "No details available.";
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred\nError message: "+ message);
            }
        }
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
            loginService.login(email, password);
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
