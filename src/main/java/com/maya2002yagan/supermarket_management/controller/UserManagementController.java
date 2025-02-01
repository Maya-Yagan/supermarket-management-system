package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya2002yagan.supermarket_management.model.Role;
import com.maya2002yagan.supermarket_management.model.User;
import com.maya2002yagan.supermarket_management.dao.UserDAO;
import com.maya2002yagan.supermarket_management.util.FormHelper;
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
import javafx.scene.control.Button;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.layout.StackPane;

/**
 * Controller class for managing the user interface related to user operations,
 * including displaying, adding, and editing users.
 * It interacts with the UserDAO for data access and manipulation.
 * 
 * @author  Maya Yagan
 */
public class UserManagementController implements Initializable {

    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, String> firstNameColumn;
    @FXML
    private TableColumn<User, String> lastNameColumn;
    @FXML
    private TableColumn<User, String> genderColumn;
    @FXML
    private TableColumn<User, String> positionColumn;
    @FXML
    private TableColumn<User, String> partFullTimeColumn;
    @FXML
    private TableColumn<User, Float> salaryColumn;
    @FXML
    private TableColumn<User, LocalDate> startDateColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> phoneColumn;
    @FXML
    private TableColumn<User, String> tcNumberColumn;
    @FXML
    private TableColumn<User, Integer> ageColumn;
    @FXML
    private Button addUserButton;
    @FXML
    private StackPane stackPane;
    private final UserDAO userDAO = new UserDAO();
    private ModalPane modalPane;
    private final ObservableList<User> userObservableList = FXCollections.observableArrayList();

    /**
     * Initializes the controller. It sets up the table columns and loads
     * the user data from the database.
     * 
     * @param location  The location of the FXML resource.
     * @param resources The resources to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        addUserButton.setOnAction(event -> FormHelper.openForm("/fxml/AddUserForm.fxml", 
                (AddUserController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> loadUserData());
                }, modalPane));
        userTableView.setItems(userObservableList);
        userTableView.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    User selectedUser = row.getItem();
                    //openEditUserModal(selectedUser);
                    FormHelper.openForm("/fxml/EditUserForm.fxml", 
                            (EditUserFormController controller) -> {
                                controller.setUser(selectedUser);
                                controller.setModalPane(modalPane);
                                controller.setOnCloseAction(() -> loadUserData());
                            }, modalPane);
                }
            });
            return row;
        });
        userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        userTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        loadUserData();
        initializeModalPane();
    }
    
    private void initializeModalPane() {
        StackPane root = stackPane;
        modalPane = new ModalPane();
        modalPane.setId("modalPane");
        root.getChildren().add(modalPane);
    }

    /**
     * Configures the columns of the user table with the appropriate cell value factories.
     */
    private void configureTableColumns() {
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        positionColumn.setCellValueFactory(data -> {
            User user = data.getValue();
            List<Role> roles = user.getRoles();
            String roleNames = roles != null ? 
                roles.stream().map(Role::getPosition).reduce((a, b) -> a + ", " + b).orElse("") : "";
            return new ReadOnlyObjectWrapper<>(roleNames);
        });

        partFullTimeColumn.setCellValueFactory(data -> {
            User user = data.getValue();
            String employmentType = user.getEmploymentType();
            return new ReadOnlyObjectWrapper<>(employmentType);
        });
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        tcNumberColumn.setCellValueFactory(new PropertyValueFactory<>("tcNumber"));

        // Set up the Age column to calculate age based on birthDate
        ageColumn.setCellValueFactory(data -> {
            User user = data.getValue();
            LocalDate birthDate = user.getBirthDate();
            if (birthDate != null) {
                int age = Period.between(birthDate, LocalDate.now()).getYears();
                return new ReadOnlyObjectWrapper<>(age);
            } else {
                return new ReadOnlyObjectWrapper<>(null);
            }
        });
    }

    /**
     * Loads the user data from the database and populates the table view.
     */
    private void loadUserData() {
        List<User> users = userDAO.getUsers();
        userObservableList.clear();
        if (users != null) {
            userObservableList.addAll(users);
        }
    }
}
