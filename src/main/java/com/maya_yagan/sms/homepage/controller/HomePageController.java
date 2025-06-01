package com.maya_yagan.sms.homepage.controller;

import atlantafx.base.theme.Styles;
import com.maya_yagan.sms.component.Sidebar;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.finance.service.CashBoxService;
import com.maya_yagan.sms.user.model.User;
import com.maya_yagan.sms.user.service.AuthorizationService;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.ViewUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;


/**
 * FXML Controller class for managing the home page in the supermarket management system.
 *
 * @author Maya Yagan
 */
public class HomePageController implements Initializable {
    
    @FXML private HBox rootLayout;
    @FXML private AnchorPane contentPane;
    @FXML StackPane stackPane;
    private VBox sidebar;
    private boolean isSidebarVisible = true;
    private final CashBoxService cashBoxService= new CashBoxService();
    private final AuthorizationService authorizationService = new AuthorizationService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(sidebar == null) initializeSidebar();
        setSidebarVisibility(true);
        HBox.setHgrow(contentPane, Priority.ALWAYS);
        AnchorPane.setTopAnchor(contentPane, 0.0);
        AnchorPane.setRightAnchor(contentPane, 0.0);
        AnchorPane.setBottomAnchor(contentPane, 0.0);
        AnchorPane.setLeftAnchor(contentPane, 0.0);
        navigateToPage("/view/HomePageContent.fxml", "Home Page");
    }    

    @FXML
    private void toggleSidebar() {
        isSidebarVisible = !isSidebarVisible;
        Sidebar.toggleSidebar(sidebar, isSidebarVisible);
    }

    public void setSidebarVisibility(boolean visible) {
        isSidebarVisible = visible;
        Sidebar.toggleSidebar(sidebar, visible);
    }

    private void gotToHomePage(){
        navigateToPage("/view/HomePageContent.fxml", "Home Page");
    }

    private void goToProductManagement() {
        navigateToPage("/view/product/ProductManagement.fxml", "Product Management");
    }

    private void goToUserManagement() {
        navigateToPage("/view/user/UserManagement.fxml", "Employee Management");
    }

    private void goToWarehouseManagement() {
        navigateToPage("/view/warehouse/WarehouseManagement.fxml", "Warehouse Management");
    }

    private void gotToSupplierManagement(){
        navigateToPage("/view/supplier/SupplierManagement.fxml", "Supplier Management");
    }

    private void goToOrderManagement(){
        navigateToPage("/view/order/OrderManagement.fxml", "Order Management");
    }

    private void goToPayment() {
        if(cashBoxService.isCashBoxOpen())
            navigateToPage("/view/payment/PaymentPage.fxml", "Payment Page");
        else
            AlertUtil.showAlert(Alert.AlertType.WARNING,
                    "No Open Cash Box",
                    "No open cash box!\nPlease open the cash box first.");
    }

    private void gotToSettings(){
        navigateToPage("/view/settings/SettingsPage.fxml", "Settings Page");
    }

    private void goToFinance(){
        navigateToPage("/view/finance/MainFinancePage.fxml", "Finance Page");
    }


    private void logout() {
        UserSession.getInstance().clear();
        ViewUtil.changeScene(
                "/view/Login.fxml",
                "Login",
                stackPane
        );
    }

    private void initializeSidebar(){
        if(sidebar != null) return;
        var toggleButton = new Button(null, new FontIcon(Feather.MENU));
        toggleButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT);
        toggleButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        toggleButton.setOnAction(e -> toggleSidebar());

        User currentUser = UserSession.getInstance().getCurrentUser();
        Set<AuthorizationService.Module> allowedModules = authorizationService.getAllowedModules(currentUser);
        List<String> roleNames = currentUser.getRoles().stream()
                .map(role -> role.getName().toLowerCase())
                .toList();

        List<Sidebar.SidebarItem> sidebarItems = new ArrayList<>();
        sidebarItems.add(new Sidebar.SidebarItem("Home Page", Feather.HOME, this::gotToHomePage, null));

        if (roleNames.contains("manager"))
            sidebarItems.add(new Sidebar.SidebarItem("Settings", Feather.SETTINGS, this::gotToSettings, null));

        if (allowedModules.contains(AuthorizationService.Module.PRODUCT))
            sidebarItems.add(new Sidebar.SidebarItem("Product Management", Feather.SHOPPING_BAG, this::goToProductManagement, null));

        if (allowedModules.contains(AuthorizationService.Module.EMPLOYEE))
            sidebarItems.add(new Sidebar.SidebarItem("Employee Management", Feather.USERS, this::goToUserManagement, null));

        if (allowedModules.contains(AuthorizationService.Module.INVENTORY))
            sidebarItems.add(new Sidebar.SidebarItem("Inventory Management", Feather.DISC, this::goToWarehouseManagement, null));

        if (allowedModules.contains(AuthorizationService.Module.SUPPLIER))
            sidebarItems.add(new Sidebar.SidebarItem("Supplier Management", Feather.BOX, this::gotToSupplierManagement, null));

        if (allowedModules.contains(AuthorizationService.Module.ORDER))
            sidebarItems.add(new Sidebar.SidebarItem("Order Management", Feather.PEN_TOOL, this::goToOrderManagement, null));

        if (allowedModules.contains(AuthorizationService.Module.PAYMENT))
            sidebarItems.add(new Sidebar.SidebarItem("Payment Page", Feather.SHOPPING_CART, this::goToPayment, null));

        if (allowedModules.contains(AuthorizationService.Module.FINANCE))
            sidebarItems.add(new Sidebar.SidebarItem("Financial Records Tracking", Feather.BOOK_OPEN, this::goToFinance, null));

        sidebarItems.add(new Sidebar.SidebarItem("Log out", Feather.LOG_OUT, this::logout, null));

        sidebar = Sidebar.createSidebar(sidebarItems, toggleButton);
        if(!rootLayout.getChildren().contains(sidebar))
            rootLayout.getChildren().add(0, sidebar);
    }
    
    /**
     * Helper method to navigate to a specific page.
     * 
     * @param fxmlPath the path to the FXML file
     * @param title the title for the stage
     */
    private void navigateToPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            contentPane.getChildren().setAll(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

            // defer title‐setting until after the scene is attached
            Platform.runLater(() -> {
                Stage stage = (Stage) contentPane.getScene().getWindow();
                stage.setTitle(title);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStackPane(StackPane stackPane) {
        this.stackPane = stackPane;
    }
}
