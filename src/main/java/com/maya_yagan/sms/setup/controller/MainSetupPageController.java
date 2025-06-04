package com.maya_yagan.sms.setup.controller;

import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.user.model.Role;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.UserService;
import com.maya_yagan.sms.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class MainSetupPageController implements Initializable {
    @FXML private TextField dbNameField, portField, serverField, usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button saveButton, testButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        testButton.setOnAction(event -> {
            if(!areFieldsValid()) return;

            boolean success = testConnection();
            if(success)
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "Database connection successful!");
        });

        saveButton.setOnAction(event -> {
            if(!areFieldsValid()) return;
            if(!testConnection()) return;

            try {
                saveConfig();
                AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Saved", "Configurations saved successfully.\nUse the following credentials to log in:\nEmail: admin@admin.admin\nPassword: 123\nIMPORTANT!! Make sure to create a new user with a strong password and delete this default one.");
                restartApplication();
            } catch (IOException e){
                e.printStackTrace();
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Error", "Failed to save configurations.\nError message: " + e.getMessage());
            }
        });
    }

    private String buildJdbcUrl() {
        String server = serverField.getText().trim();
        String port = portField.getText().trim();
        String dbName = dbNameField.getText().trim();

        return String.format("jdbc:sqlserver://%s:%s;databaseName=%s", server, port, dbName);
    }

    private boolean testConnection() {
        String url = buildJdbcUrl();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            return conn.isValid(2);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Failed", "Could not connect to the database.\nError message: " + e.getMessage());
            return false;
        }
    }

    private void saveConfig() throws IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String jdbcUrl = buildJdbcUrl();

        String configContent = String.format("""
        db {
          driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
          url = "%s"
          username = "%s"
          password = "%s"
        }
        """, jdbcUrl, username, password);

        Path configPath = Paths.get(System.getProperty("user.dir"), "application.conf");
        Files.writeString(configPath, configContent, StandardCharsets.UTF_8);
    }

    private void restartApplication(){
        try {
            // Path to the currently running jar
            String jarPath = new java.io.File(MainSetupPageController.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getPath();

            // Relaunch the jar using java -jar
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", jarPath);
            builder.start();

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showAlert(Alert.AlertType.ERROR, "Restart Failed", "Could not restart the application.\n" + e.getMessage());
        }
    }

    private boolean areFieldsValid() {
        if (serverField.getText().trim().isEmpty() ||
                portField.getText().trim().isEmpty() ||
                dbNameField.getText().trim().isEmpty() ||
                usernameField.getText().trim().isEmpty() ||
                passwordField.getText().isEmpty()) {

            AlertUtil.showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all the required fields.");
            return false;
        }
        return true;
    }
}
