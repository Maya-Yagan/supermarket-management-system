package com.maya_yagan.sms;

import atlantafx.base.theme.NordLight;
import com.maya_yagan.sms.payment.creditcard.StripeConfig;
import com.maya_yagan.sms.common.ConfigManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.util.Objects;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/app_icon.png")));
        primaryStage.getIcons().add(icon);
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

        Parent root;
        if (ConfigManager.isFirstRun()) {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/setup/MainSetupPage.fxml")));
            primaryStage.setTitle("First-Time Setup");
        } else {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/Login.fxml")));
            primaryStage.setTitle("Supermarket Management System");
        }

        primaryStage.setScene(new Scene(root));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        StripeConfig.init();
        launch(args);
    }
}
