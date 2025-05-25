package com.maya_yagan.sms.homepage.controller;

import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.homepage.model.Notification;
import com.maya_yagan.sms.homepage.service.HomePageService;
import com.maya_yagan.sms.payment.service.CashBoxService;
import com.maya_yagan.sms.user.model.Attendance;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.util.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class HomePageContentController extends AbstractTableController<Notification> {
    @FXML private Label nameLabel, dateLabel, checkInHourLabel, timeSinceCheckInLabel, timeUntilCheckOutLabel, cashBoxStatusLabel;
    @FXML private TableColumn<Notification, String> notificationColumn, dateColumn, senderColumn;
    @FXML private Button checkoutButton, sendButton, openCashBoxButton, closeCashBoxButton;
    @FXML private StackPane stackPane;
    @FXML TextArea messageTextArea;

    User currentUser = UserSession.getInstance().getCurrentUser();
    private final HomePageService homePageService = new HomePageService();
    private final CashBoxService cashBoxService = new CashBoxService();
    private Attendance todayAttendance;
    private Timeline timer;

    private void loadUserData(){
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

        cashBoxStatusLabel.setText(cashBoxService.getCashBoxStatus());
    }

    @Override
    protected void configureColumns() {
        notificationColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDate();
            String formattedDate = DateUtil.formatDateTime(date);
            return new ReadOnlyStringWrapper(formattedDate);
        });
        senderColumn.setCellValueFactory(cellData -> {
            User sender = cellData.getValue().getSender();
            if(sender == null)
                return new ReadOnlyStringWrapper("System");
            else
                return new ReadOnlyStringWrapper(cellData.getValue().getSender().getFullName());
        });
    }

    @Override
    protected Collection<Notification> fetchData() {
        return homePageService.getNotifications();
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<Notification>> menuItemsFor(Notification notification) {
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Delete Notification", (item, row) -> handleDeleteAction(item))
        );
    }

    @Override
    protected void postInit(){
        setupEventHandlers();
        setTableStyling();
        loadUserData();
        startTimer();
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
        AlertUtil.showAlert(Alert.AlertType.INFORMATION,
                "Confirm Check Out",
                "If you are the last employee to check out today,\nplease remember to close the cash box.");
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

    private void setTableStyling(){
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        dateColumn.setPrefWidth(140);
        senderColumn.setPrefWidth(200);

        tableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double fixedWidth = dateColumn.getWidth() + senderColumn.getWidth() + 2; // +2 for borders/padding
            double newWidthForNotification = newWidth.doubleValue() - fixedWidth;
            if (newWidthForNotification > 0) {
                notificationColumn.setPrefWidth(newWidthForNotification);
            }
        });
    }

    private void setupEventHandlers(){
        checkoutButton.setOnAction(event -> checkOut());
        sendButton.setOnAction(event -> handleSendAction());

        openCashBoxButton.setOnAction(event -> {
            try{
                cashBoxService.openCashBox();
                loadUserData();
            } catch (CustomException e){
                ExceptionHandler.handleException(e);
            }
        });

        closeCashBoxButton.setOnAction(event -> {
            try{
                cashBoxService.closeCashBox();
                loadUserData();
            } catch (CustomException e){
                ExceptionHandler.handleException(e);
            }
        });
    }

    private void handleSendAction(){
        String message = messageTextArea.getText();
        homePageService.notify(message, false);
        refresh();
        messageTextArea.clear();
    }

    private void handleDeleteAction(Notification notification){
        AlertUtil.showDeleteConfirmation(notification,
                "Delete Notification",
                "Are you sure you want to delete this notification?",
                "This action cannot be undone.",
                n -> {
                    homePageService.deleteNotification(n.getId());
                    refresh();
                }
        );
    }
}