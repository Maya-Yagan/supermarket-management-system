package com.maya_yagan.sms.settings.controller;

import com.maya_yagan.sms.settings.model.Settings;
import com.maya_yagan.sms.settings.service.SettingsService;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ExceptionHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsPageController implements Initializable {
    @FXML private TextField nameField, addressField, phoneField, moneyUnitField;
    @FXML private Button saveButton;

    private final SettingsService settingsService = new SettingsService();
    private Settings settings;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        settings = settingsService.getSettings();
        loadData();
        saveButton.setOnAction(event -> saveSettings());
    }

    private void loadData(){
        if(settings != null){
            nameField.setText(settings.getMarketName());
            phoneField.setText(settings.getPhone());
            addressField.setText(settings.getAddress());
            moneyUnitField.setText(settings.getMoneyUnit());
        }
    }

    private void saveSettings(){
        if(settings != null){
            try{
                if(settingsService.updateSettings(
                        nameField.getText(),
                        addressField.getText(),
                        phoneField.getText(),
                        moneyUnitField.getText()
                ))
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Settings Updated", "Settings has been updated successfully");
            } catch(CustomException e){
                ExceptionHandler.handleException(e);
            }
        }
        else{
            try{
                if(settingsService.addSettings(
                        nameField.getText(),
                        addressField.getText(),
                        phoneField.getText(),
                        moneyUnitField.getText()
                ))
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Settings Added", "Settings has been added successfully");
            } catch(CustomException e){
                ExceptionHandler.handleException(e);
            }
        }
    }
}
