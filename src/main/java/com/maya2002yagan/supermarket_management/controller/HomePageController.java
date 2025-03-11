package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.theme.Styles;
import com.maya2002yagan.supermarket_management.component.Sidebar;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;


/**
 * FXML Controller class for managing the home page in the supermarket management system.
 *
 * This controller handles the user interface for the home page, including the 
 * sidebar, navigation between different management pages (product, user, warehouse), 
 * and logging out of the application. It allows toggling the sidebar visibility and 
 * setting up the content of the main screen dynamically based on user actions.
 * 
 * @author Maya Yagan
 */
public class HomePageController implements Initializable {
    
    @FXML
    private HBox rootLayout;
    @FXML
    private AnchorPane contentPane;
    private VBox sidebar;
    private boolean isSidebarVisible = true;

    /**
     * Initializes the controller class.
     * 
     * Sets up the layout for the content pane, ensures the sidebar is visible,
     * and adjusts the anchoring of the content pane to occupy the entire window.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(sidebar == null) initializeSidebar();
        setSidebarVisibility(true);
        HBox.setHgrow(contentPane, Priority.ALWAYS);
        AnchorPane.setTopAnchor(contentPane, 0.0);
        AnchorPane.setRightAnchor(contentPane, 0.0);
        AnchorPane.setBottomAnchor(contentPane, 0.0);
        AnchorPane.setLeftAnchor(contentPane, 0.0);
    }    
    
    /**
     * Toggles the visibility of the sidebar.
     *
     * This method is invoked when the user clicks the button to toggle the 
     * sidebar's visibility. It updates the sidebar's visibility state and 
     * adjusts the layout accordingly.
     */
    @FXML
    private void toggleSidebar() {
        isSidebarVisible = !isSidebarVisible;
        Sidebar.toggleSidebar(sidebar, isSidebarVisible);
    }

    /**
     * Sets the sidebar's visibility and adjusts the layout accordingly.
     *
     * @param visible Whether the sidebar should be visible.
     */
    public void setSidebarVisibility(boolean visible) {
        isSidebarVisible = visible;
        Sidebar.toggleSidebar(sidebar, visible);
    }
    
    /**
     * Navigates to the Home Page.
     */
    private void gotToHomePage(){
        navigateToPage("/fxml/HomePage.fxml", "Home Page");
        rootLayout.getChildren().remove(sidebar);
    }
    
    /**
     * Navigates to the Product Management screen.
     */
    private void goToProductManagement() {
        navigateToPage("/fxml/ProductManagement.fxml", "Product Management");
    }
    
    /**
     * Navigates to the User Management screen.
     */
    private void goToUserManagement() {
        navigateToPage("/fxml/UserManagement.fxml", "Employee Management");
    }
    
    /**
     * Navigates to the Warehouse Management screen.
     */
    private void goToWarehouseManagement() {
        navigateToPage("/fxml/WarehouseManagement.fxml", "Warehouse Management");
    }
    
    /**
     * Navigates to the Supplier Management screen.
     */
    private void gotToSupplierManagement(){
        navigateToPage("/fxml/SupplierManagement.fxml", "Supplier Management");
    }
    
    /**
     * Navigates to the Order Management screen.
     */
    private void goToOrderManagement(){
        navigateToPage("/fxml/OrderManagement.fxml", "Order Management");
    }
    
    /**
     * Logs out the user and navigates to the sign-in page.
     *
     * This method removes the sidebar and navigates the user to the login screen.
     */
    private void logout() {
        navigateToPage("/fxml/Main.fxml", "Sign in");
        rootLayout.getChildren().remove(sidebar);
    }
    
    /**
     * Initializes the sidebar with navigation items and a toggle button.
     *
     * This method creates the sidebar layout, adding navigation options for pages.
     * It also sets up the toggle button for showing or hiding the sidebar.
     */
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
            contentPane.getChildren().clear();
            contentPane.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}          
