package com.maya_yagan.sms.component;

import atlantafx.base.theme.Styles;
import java.util.List;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * This class is responsible for creating and managing a sidebar UI component 
 * with collapsible and expandable functionality, along with sidebar items.
 * Each sidebar item can have a name, an icon, an action to perform, and potentially child items.
 * The sidebar supports toggle functionality for expansion and collapsing with animation.
 * 
 * @author Maya Yagan
 */
public class Sidebar {
    
    private static final double EXPANDED_WIDTH = 250; // Full sidebar width
    private static final double COLLAPSED_WIDTH = 50; // Width of the collapsed sidebar
    
    /**
     * Creates a sidebar with collapsible functionality.
     * This method initializes a sidebar and populates it with the given items. 
     * The sidebar's toggle button is added at the top, followed by the list of sidebar items.
     * Each sidebar item is represented by a button, and if it has an icon, the icon will be displayed.
     * 
     * @param items A list of SidebarItem objects representing the items to display in the sidebar.
     * @param toggleButton A Button that will toggle the visibility of the sidebar when clicked.
     * @return A VBox representing the complete sidebar, containing all items and the toggle button.
     */
    public static VBox createSidebar(List<SidebarItem> items, Button toggleButton) {
        VBox sidebar = new VBox(10);
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #34495e, white); -fx-padding: 10; -fx-spacing: 5;");
        sidebar.getChildren().add(toggleButton);
        
        // Add buttons for each sidebar item
        for (SidebarItem item : items) {
            Button button = new Button(item.getName());
            button.setMaxWidth(Double.MAX_VALUE);
            
            // Add an icon to the button if the sidebar item has one
            if (item.getIcon() != null) {
                FontIcon icon = new FontIcon(item.getIcon());
                button.setGraphic(icon);
                button.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
            }
            
            // Define the action to perform when the button is clicked
            button.setOnAction(e -> {
                if (item.action != null) {
                    item.action.run();
                }
            });
            sidebar.getChildren().add(button);
        }

        // Set initial sidebar width
        sidebar.setPrefWidth(EXPANDED_WIDTH);
        sidebar.setMinWidth(COLLAPSED_WIDTH);

        return sidebar;
    }
    
    /**
     * Toggles the visibility of the sidebar with a smooth animation.
     * The sidebar will either expand or collapse based on the current state. 
     * The transition is animated to smoothly adjust the width, 
     * and the visibility of child elements is adjusted.
     * 
     * @param sidebar The VBox representing the sidebar to toggle.
     * @param isExpanded A boolean indicating whether the sidebar should be expanded (true) or collapsed (false).
     */
    public static void toggleSidebar(VBox sidebar, boolean isExpanded) {
        double targetWidth = isExpanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH;
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sidebar);
        transition.setToX(0); // Animate horizontally (no shift needed, just for smooth transition)
        transition.play();
         // Adjust width dynamically
        sidebar.setPrefWidth(targetWidth);
        // Adjust visibility of child buttons (except toggle button)
        sidebar.getChildren().forEach(node -> {
            if (node instanceof Button && ((Button) node).getText() != null) {
                node.setVisible(isExpanded);
                node.setManaged(isExpanded);
            }
        });
    }

    /**
     * Represents a single item in the sidebar.
     * A sidebar item may include a name, an icon, an action to perform when clicked,
     * and optionally, child items that represent a hierarchical structure.
     */
    public static class SidebarItem {
        private final String name;
        private final Feather icon;
        private final Runnable action;
        private final List<SidebarItem> children;

        /**
         * Constructs a SidebarItem with the given properties.
         *
         * @param name The name of the sidebar item.
         * @param icon The icon associated with the item (can be null).
         * @param action The action to perform when the item is clicked (can be null).
         * @param children A list of child SidebarItem objects (can be empty).
         */
        public SidebarItem(String name, Feather icon, Runnable action, List<SidebarItem> children) {
            this.name = name;
            this.icon = icon;
            this.action = action;
            this.children = children;
        }

        public String getName() {
            return name;
        }

        public Feather getIcon() {
            return icon;
        }

        public Runnable getAction() {
            return action;
        }

        public List<SidebarItem> getChildren() {
            return children;
        }

        /**
         * Creates a UI representation of a sidebar item, either a button or a titled pane
         * depending on whether the item has child items.
         *
         * @param item The SidebarItem to create a UI component for.
         * @return A Node representing the sidebar item (Button or TitledPane).
         */
        private static Node createSidebarItem(SidebarItem item){
            // If the item has no children, create a button
            if(item.getChildren() == null || item.getChildren().isEmpty()){
                Button button = new Button(item.getName());
                button.setStyle("-fx-text-fill: white; -fx-background-color: transparent; -fx-alignment: LEFT;");
                button.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.ACCENT);
                button.setOnAction(e -> {
                    if(item.getAction() != null) item.getAction().run();
                });
                return button;
            }
            // If the item has children, create a TitledPane
            else{
                VBox childrenContainer = new VBox(5);
                for(SidebarItem child : item.getChildren()){
                    childrenContainer.getChildren().add(createSidebarItem(child));
                }
                TitledPane titledPane = new TitledPane(item.getName(), childrenContainer);
                titledPane.setExpanded(false); // Initially collapsed
                return titledPane;
            }
        }
    }
}
