package com.maya_yagan.sms.homepage.controller;

import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.homepage.service.HomePageService;
import com.maya_yagan.sms.user.model.Attendance;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class HomePageContentController implements Initializable {
    @FXML private Label nameLabel, dateLabel, checkInHourLabel, timeSinceCheckInLabel, timeUntilCheckOutLabel;
    @FXML private Button checkoutButton;
    @FXML private StackPane stackPane;

    User currentUser = UserSession.getInstance().getCurrentUser();
    private final HomePageService homePageService = new HomePageService();
    private Attendance todayAttendance;
    private Timeline timer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserData();
        startTimer();
    }

    private void loadUserData(){
        checkoutButton.setOnAction(event -> checkOut());

        nameLabel.setText(currentUser.getFullName());
        dateLabel.setText(DateUtil.formatDate(LocalDate.now()));

        todayAttendance = homePageService.getTodayAttendance(currentUser);
        if(todayAttendance != null && todayAttendance.getCheckIn() != null){
            LocalTime in = todayAttendance.getCheckIn();
            checkInHourLabel.setText(DateUtil.formatTime(in));
            updateElapsedTime(in);
        } else {
            checkInHourLabel.setText("-");
            timeSinceCheckInLabel.setText("-");
        }
    }

    private void startTimer(){
        if (timer != null) timer.stop();
        timer = new Timeline(
                new KeyFrame(Duration.ZERO, event -> {
                    if(todayAttendance != null && todayAttendance.getCheckIn() != null)
                        updateElapsedTime(todayAttendance.getCheckIn());
                }),
                new KeyFrame(Duration.minutes(1)));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateElapsedTime(LocalTime checkIn){
        timeSinceCheckInLabel.setText(homePageService.getElapsedTime(checkIn));
        timeUntilCheckOutLabel.setText(
                homePageService.getTimeUntilCheckout(todayAttendance)
        );
    }

    private void checkOut(){
        String time = DateUtil.formatTime(LocalTime.now());
        ViewUtil.showStringInputDialog(
                "Check Out",
                "You’re about to check out at " + time + " for " + currentUser.getFullName(),
                "Enter your password:",
                "********",
                "Password",
                inputPassword  -> {
                    try{
                        homePageService.checkOut(currentUser, inputPassword);
                        ViewUtil.changeScene("/view/Login.fxml", "Login", stackPane);
                    } catch (CustomException e){
                        ExceptionHandler.handleException(e);
                    }
                }
        );
    }
}
