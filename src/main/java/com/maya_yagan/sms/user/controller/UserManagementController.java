package com.maya_yagan.sms.user.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.user.model.Role;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.UserService;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.ContextMenuUtil;
import com.maya_yagan.sms.util.ViewUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;

/**
 * Controller class for managing the user interface related to user operations.
 * 
 * @author  Maya Yagan
 */
public class UserManagementController implements Initializable {

    @FXML private TableView<User> userTableView;
    @FXML private TableColumn<User, String> firstNameColumn, lastNameColumn, genderColumn, positionColumn, partFullTimeColumn, emailColumn, phoneColumn, tcNumberColumn;
    @FXML private TableColumn<User, Float> salaryColumn;
    @FXML private TableColumn<User, LocalDate> startDateColumn;
    @FXML private TableColumn<User, Integer> ageColumn;
    @FXML private Button addUserButton;
    @FXML private StackPane stackPane;
    
    private ModalPane modalPane;
    private final UserService userService = new UserService();
    private final ObservableList<User> userObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService.initializeRole();
        modalPane = ViewUtil.initializeModalPane(stackPane);
        configureTableColumns();
        setupEventHandlers();
        refreshTable();
        userTableView.setItems(userObservableList);
        userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
    }

    private void refreshTable() {
        List<User> users = userService.getAllUsers();
        userObservableList.setAll(users);
    }

    private void configureTableColumns() {
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        tcNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tcNumber"));
        
        positionColumn.setCellValueFactory(data -> {
            User user = data.getValue();
            String roleNames = user.getRoles() != null ? 
                    user.getRoles()
                    .stream()
                    .map(Role::getName)
                    .filter(Objects::nonNull)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("") : "";
            return new ReadOnlyObjectWrapper<>(roleNames);
        });

        partFullTimeColumn.setCellValueFactory(data -> 
            new ReadOnlyObjectWrapper<>(data.getValue().getEmploymentType())
        );
        
        ageColumn.setCellValueFactory(data -> {
            LocalDate birthDate = data.getValue().getBirthDate();
            return new ReadOnlyObjectWrapper<>(birthDate != null ? 
                Period.between(birthDate, LocalDate.now()).getYears() : null);
        });
    }

    private void setupEventHandlers(){
        addUserButton.setOnAction(event -> ViewUtil.displayView("/view/user/AddUser.fxml", 
                (AddUserController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(this::refreshTable);
                }, modalPane));

        setupUserTableContextMenu();
    }

    private  void setupUserTableContextMenu(){
        userTableView.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldUser, currentUser) -> {
                if(currentUser != null) {
                    List<ContextMenuUtil.MenuItemConfig<User>> menuItems = new ArrayList<>();

                    menuItems.add(new ContextMenuUtil.MenuItemConfig<>(
                            "Edit User",
                            (user, r) ->
                                    ViewUtil.displayView("/view/user/EditUser.fxml",
                                    (EditUserController controller) -> {
                                        controller.setUser(row.getItem());
                                        controller.setModalPane(modalPane);
                                        controller.setOnCloseAction(this::refreshTable);
                                    }, modalPane)
                    ));

                    menuItems.add(new ContextMenuUtil.MenuItemConfig<>(
                            "Delete User",
                            (user, r) -> {
                                AlertUtil.showDeleteConfirmation(user,
                                        "Delete User",
                                        "Are you sure you want to delete this user?",
                                        "This action cannot be undone.",
                                        (u) -> {
                                            userService.deleteUser(u.getId());
                                            refreshTable();
                                        }
                                );
                            }
                    ));

                    ContextMenu contextMenu = ContextMenuUtil.createContextMenu(row, currentUser, menuItems);
                    row.setContextMenu(contextMenu);
                } else {
                    row.setContextMenu(null);
                }
            });
            return row;
        });
    }
}
