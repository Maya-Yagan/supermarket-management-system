package com.maya_yagan.sms.homepage.controller;

import atlantafx.base.theme.Styles;
import com.maya_yagan.sms.component.Sidebar;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.maya_yagan.sms.common.UserSession;
import com.maya_yagan.sms.util.ViewUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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

    private void goToPayment() { navigateToPage("/view/payment/PaymentPage.fxml", "Payment Page"); }

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
        toggleButton.getStyleClass().addAll(
            Styles.BUTTON_ICON, Styles.ACCENT
        );
        toggleButton.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        toggleButton.setOnAction(e -> toggleSidebar());
        List<Sidebar.SidebarItem> sidebarItems = List.of(
                new Sidebar.SidebarItem("Home Page", Feather.HOME, this::gotToHomePage, null),
                new Sidebar.SidebarItem("Product Management", Feather.SHOPPING_BAG, this::goToProductManagement, null),
                new Sidebar.SidebarItem("Employee Management", Feather.USERS, this::goToUserManagement, null),
                new Sidebar.SidebarItem("Warehouse Management", Feather.DISC, this::goToWarehouseManagement, null),
                new Sidebar.SidebarItem("Supplier Management", Feather.BOX, this::gotToSupplierManagement, null),
                new Sidebar.SidebarItem("Order Management", Feather.PEN_TOOL, this::goToOrderManagement, null),
                new Sidebar.SidebarItem("Payment Page", Feather.SHOPPING_CART, this::goToPayment, null),
                new Sidebar.SidebarItem("Log out", Feather.LOG_OUT, this::logout, null)
        );
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
