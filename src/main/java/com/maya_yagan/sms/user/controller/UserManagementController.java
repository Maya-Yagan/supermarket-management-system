package com.maya_yagan.sms.user.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.settings.service.SettingsService;
import com.maya_yagan.sms.user.dao.SalaryDetailsDTO;
import com.maya_yagan.sms.user.model.Role;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.AdvanceService;
import com.maya_yagan.sms.user.service.UserService;
import com.maya_yagan.sms.util.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import javafx.scene.layout.StackPane;

/**
 * Controller class for managing the user interface related to user operations.
 * 
 * @author  Maya Yagan
 */
public class UserManagementController extends AbstractTableController<User> {

    @FXML private TableColumn<User, String> firstNameColumn, lastNameColumn, genderColumn, positionColumn, partFullTimeColumn, emailColumn, phoneColumn, tcNumberColumn;
    @FXML private TableColumn<User, Float> salaryColumn;
    @FXML private TableColumn<User, String> startDateColumn;
    @FXML private TableColumn<User, Integer> ageColumn;
    @FXML private Button addUserButton;
    @FXML private StackPane stackPane;
    
    private ModalPane modalPane;
    private final AdvanceService advanceService = new AdvanceService();
    private final UserService userService = new UserService();
    private final SettingsService settingsService = new SettingsService();
    private final String moneyUnit = settingsService.getSettings().getMoneyUnit();

    @Override
    protected void configureColumns() {
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        salaryColumn.setText("Daily salary (" + moneyUnit + ")");
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));
        startDateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getStartDate();
            String text = (date != null) ? DateUtil.formatDate(date) : "";
            return new ReadOnlyObjectWrapper<>(text);
        });
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

    @Override
    protected Collection<User> fetchData(){
        return userService.getAllUsers();
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<User>> menuItemsFor(User user) {
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Salary Details", (item, row) -> handleEmployeeInfoAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Employee Attendance", (item, row) -> handleAttendanceAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Pay Salary", (item, row) -> handleSalaryPayment(item)),
                new ContextMenuUtil.MenuItemConfig<>("Edit User", (item, row) -> handleEditAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Delete User", (item, row) -> handleDeleteAction(item))
        );
    }

    @Override
    protected void postInit(){
        userService.initializeRole();
        modalPane = ViewUtil.initializeModalPane(stackPane);
        setupEventHandlers();
    }

    private void setupEventHandlers(){
        addUserButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/user/AddUser.fxml",
                (AddUserController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(this::refresh);
                }, modalPane));
    }

    private void handleDeleteAction(User user) {
        AlertUtil.showDeleteConfirmation(user,
                "Delete User",
                "Are you sure you want to delete this user?",
                "This action cannot be undone.",
                u -> {
                    userService.deleteUser(u.getId());
                    refresh();
                }
        );
    }

    private void handleEditAction(User user) {
        ViewUtil.displayModalPaneView("/view/user/EditUser.fxml",
        (EditUserController controller) -> {
            controller.setUser(user);
            controller.setModalPane(modalPane);
            controller.setOnCloseAction(this::refresh);
        }, modalPane);
    }

    private void handleAttendanceAction(User user) {
        if(user != null)
            ViewUtil.displayModalPaneView("/view/user/EmployeeAttendance.fxml",
                    (EmployeeAttendanceController controller) -> {
                        controller.setModalPane(modalPane);
                        controller.setUser(user);
                        controller.setOnCloseAction(this::refresh);
                    }, modalPane);
    }

    private void handleEmployeeInfoAction(User user) {
        if (user != null)
            ViewUtil.displayModalPaneView("/view/user/SalaryDetails.fxml",
                    (SalaryDetailsController controller) -> {
                        controller.setModalPane(modalPane);
                        controller.setUser(user);
                        controller.setOnCloseAction(this::refresh);
                    }, modalPane);
    }

    private void handleSalaryPayment(User user){
        try {
            AlertUtil.showConfirmation(
                    "Pay Salary",
                    "Salary Payment",
                    "This will deduct the employee's salary from the cash box and record the transaction.",
                    () -> advanceService.paySalary(user, LocalDate.now().getYear(), LocalDate.now().getMonthValue())
            );
        } catch (CustomException e){
            ExceptionHandler.handleException(e);
        }
    }
}