package com.maya2002yagan.supermarket_management;

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
 * FXML Controller class
 *
 * @author maya2
 */
public class HomePageController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
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
}
