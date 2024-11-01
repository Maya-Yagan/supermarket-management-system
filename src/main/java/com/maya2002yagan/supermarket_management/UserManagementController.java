package com.maya2002yagan.supermarket_management;
import java.io.IOException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import javafx.scene.control.Button;
import java.util.ResourceBundle;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;

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

    private UserDAO userDAO;
    
    @FXML
    private Button addUserButton; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userDAO = new UserDAO();
        configureTableColumns();
        loadUserData();
        // Attach action to addUserButton
        addUserButton.setOnAction(event -> openAddUserModal());
        userTableView.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    User selectedUser = row.getItem();
                    openEditUserModal(selectedUser);
                }
            });
            return row;
        });
    }
    
    private void openEditUserModal(User user){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditUserForm.fxml"));
            Parent root = loader.load();
            EditUserFormController editController = loader.getController();
            editController.setUser(user);
            Stage modalStage = new Stage();
            modalStage.setTitle(("Edit User"));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();
            loadUserData();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void configureTableColumns() {
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        // Custom cell value factory for positionColumn to show user roles
        positionColumn.setCellValueFactory(data -> {
            User user = data.getValue();
            List<Role> roles = user.getRoles();
            // Convert the roles list to a comma-separated string of positions
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
                return new ReadOnlyObjectWrapper<>(null); // or return a default value if preferred
            }
        });
    }

    private void loadUserData() {
        List<User> users = userDAO.getUsers();
        if (users != null) {
            userTableView.getItems().setAll(users);
        }
    }
    
    // Method to open the add user modal
    private void openAddUserModal() {
        try {
            // Load the FXML for the modal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddUserForm.fxml"));
            Parent root = loader.load();

            // Create a new Stage for the modal
            Stage modalStage = new Stage();
            modalStage.setTitle("Add New User");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setScene(new Scene(root));
            modalStage.showAndWait();

            // Optionally, refresh user data after modal is closed
            loadUserData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
