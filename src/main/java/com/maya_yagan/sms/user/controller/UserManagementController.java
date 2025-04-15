package com.maya_yagan.sms.user.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.user.model.Role;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.UserService;
import com.maya_yagan.sms.util.ViewUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import javafx.scene.control.Button;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
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
        setupTableRowClickListener();
        setupEventHandlers();
        loadUserData();
        userTableView.setItems(userObservableList);
        userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
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

        
    private void setupTableRowClickListener(){
        userTableView.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty()))
                    handleDoubleClick(row.getItem());
            });
            return row;
        });
    }
    
    private void handleDoubleClick(User selectedUser){
        ViewUtil.displayView("/view/user/EditUser.fxml", 
                        (EditUserController controller) -> {
                            controller.setUser(selectedUser);
                            controller.setModalPane(modalPane);
                            controller.setOnCloseAction(() -> loadUserData());
                        }, modalPane);
    }
    
    private void setupEventHandlers(){
        addUserButton.setOnAction(event -> ViewUtil.displayView("/view/user/AddUser.fxml", 
                (AddUserController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> loadUserData());
                }, modalPane));
    }
    
    private void loadUserData() {
        List<User> users = userService.getAllUsers();
        userObservableList.setAll(users);
    }
}
