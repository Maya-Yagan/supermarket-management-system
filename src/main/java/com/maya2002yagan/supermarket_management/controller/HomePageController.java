package com.maya2002yagan.supermarket_management.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;


/**
 * FXML Controller class for managing the home page in the supermarket management system.
 *
 * @author Maya Yagan
 */
public class HomePageController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
    
    /**
     * Handles the event when the User Management Button is clicked
     * 
     * This method loads the User Management screen's FXML layout and displays
     * it in the current application window. It replaces the current scene with
     * the User Management interface.
     * 
     * @param event The ActionEvent triggered by clicking the 
     * "User Management" button which serves as the source of the event.
     */
    @FXML
    private void handleUserManagementButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserManagement.fxml"));
            Parent userManagementRoot = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(userManagementRoot));
            stage.setTitle("User Management");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
    
    @FXML
    private void handleProductManagementButtonClick(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductManagement.fxml"));
            Parent userManagementRoot = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(userManagementRoot));
            stage.setTitle("Product Management");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogoutButtonClick(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            Parent userManagementRoot = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(userManagementRoot));
            stage.setTitle("Product Management");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
