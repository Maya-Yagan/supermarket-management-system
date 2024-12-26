package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.WarehouseDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.ProductWarehouse;
import com.maya2002yagan.supermarket_management.model.Warehouse;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controller class for managing products within a warehouse.
 * 
 * This class provides functionality for displaying, adding, editing, and deleting products in a specific warehouse.
 * It supports operations such as editing product quantities, filtering products by categories, and managing warehouse capacity.
 * The controller interacts with the user interface via JavaFX components and communicates with the database via DAOs.
 * 
 * @author Maya Yagan
 */
public class WarehouseProductsController implements Initializable {
    
    @FXML
    private Text warehouseName;
    @FXML
    private TableView<Product> productTableView;
    @FXML
    private TableColumn<Product, Integer> idColumn;
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product, String> productionDate;
    @FXML
    private TableColumn<Product, String> expirationDateColumn;
    @FXML
    private TableColumn<Product, String> amountColumn;
    @FXML
    private TableColumn<Product, Void> deleteColumn;
    @FXML
    private Button addProductButton, backButton;
    @FXML
    private MenuButton categoryMenuButton;
    @FXML
    private Label label1;
    @FXML
    private Label label2;
    @FXML
    private StackPane stackPane;
    
    private ModalPane modalPane;
    private Warehouse warehouse;
    private Category selectedCategory;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final ObservableList<Product> productObservableList = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller and sets up the UI components and behaviors.
     * 
     * This method configures table columns, sets up actions for buttons, loads categories,
     * initializes the modal pane, and configures product quantity editing and delete actions.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up table columns for product data
        configureTableColumns();
        // Bind the product list to the table view
        productTableView.setItems(productObservableList);
        addProductButton.setOnAction(event -> openAddProductForm(warehouse));
        backButton.setOnAction(event -> goBack());
        productTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        loadCategories();
        initializeModalPane();
        setupColumnForEditing();
        setupDeleteColumn();
        setupDynamicLayoutAdjustment();
    }  
    
    /**
     * Initializes and configures the modal pane.
     */
    private void initializeModalPane() {
        StackPane root = stackPane;
        modalPane = new ModalPane();
        modalPane.setId("modalPane");
        root.getChildren().add(modalPane);
    }
    
    /**
     * Sets the current warehouse and loads its products.
     * 
     * @param warehouse The warehouse being managed
     */
    public void setWarehouse(Warehouse warehouse){
        this.warehouse = warehouse;
        warehouseName.setText(warehouse.getName());
        loadProducts();
    }
    
    /**
     * Configures the table columns for displaying product data.
     */
    private void configureTableColumns(){
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productionDate.setCellValueFactory(new PropertyValueFactory<>("productionDate"));
        expirationDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
        amountColumn.setCellValueFactory(data -> {
            Product product = data.getValue();
            return warehouse.getProductWarehouses()
                    .stream()
                    .filter(pw -> pw.getProduct().equals(product))
                    .map(pw -> new ReadOnlyObjectWrapper<>(String.valueOf(pw.getAmount())))
                    .findFirst()
                    .orElse(new ReadOnlyObjectWrapper("0"));
        });
    }
    
    /**
     * Configures the amount column for in-line editing of product quantities.
     */
    private void setupColumnForEditing() {
        amountColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        }));

        amountColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            String newAmountStr = event.getNewValue();

            try {
                int newAmount = Integer.parseInt(newAmountStr);
                if (newAmount < 0) {
                    ShowAlert.showAlert(Alert.AlertType.ERROR, "Invalid Input", "The amount cannot be negative.");
                    productTableView.refresh();
                    return;
                }

                int currentUsage = warehouse.getProductWarehouses().stream()
                                            .mapToInt(ProductWarehouse::getAmount).sum();
                int remainingCapacity = warehouse.getCapacity() - currentUsage;
                
                if (newAmount > remainingCapacity) {
                    if (remainingCapacity == 0)
                        ShowAlert.showAlert(Alert.AlertType.ERROR, "Warehouse Full", "The warehouse is already full. No more products can be added.");
                    else 
                        ShowAlert.showAlert(Alert.AlertType.ERROR, "Capacity Exceeded", 
                                  "The entered amount exceeds the remaining warehouse capacity. Remaining capacity: " + remainingCapacity);
                    productTableView.refresh();
                    return;
                }

                ProductWarehouse productWarehouse = warehouse.getProductWarehouses()
                        .stream()
                        .filter(pw -> pw.getProduct().equals(product))
                        .findFirst()
                        .orElse(null);

                if (productWarehouse != null) {
                    productWarehouse.setAmount(newAmount);
                    warehouseDAO.updateWarehouse(warehouse);
                    Warehouse refreshedWarehouse = warehouseDAO.getWarehouseById(warehouse.getId());
                    warehouse.setProductWarehouses(refreshedWarehouse.getProductWarehouses());
                    productTableView.refresh();
                    ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Success", "The product amount was successfully updated.");
                }
            } catch (NumberFormatException e) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for the amount.");
                productTableView.refresh();
            }
        });
        productTableView.setEditable(true);
    }

    /**
     * Configures the delete column for removing products from the warehouse.
     */
    private void setupDeleteColumn(){
        deleteColumn.setCellFactory(param -> {
            return new TableCell<Product, Void>(){
                private final Button deleteButton = new Button(null, new FontIcon(Feather.TRASH));
                {
                    deleteButton.getStyleClass().add(Styles.DANGER);
                    deleteButton.setOnAction(event -> {
                        Product product = getTableView().getItems().get(getIndex());
                        showDeleteConfirmation(product);
                    });
                }
                @Override
                protected  void updateItem(Void item, boolean empty){
                    super.updateItem(item, empty);
                    if(empty) setGraphic(null);
                    else setGraphic(deleteButton);
                }
            };
        });
    }
    
    /**
     * Loads the list of product categories into the menu button.
     */
    private void loadCategories() {
        categoryMenuButton.getItems().clear();
        MenuItem allCategoriesItem = new MenuItem("All Categories");
        categoryMenuButton.setText("All Categories");
        allCategoriesItem.setOnAction(event -> {
            selectedCategory = null;
            categoryMenuButton.setText("All Categories");
            loadProducts();
        });
        categoryMenuButton.getItems().add(allCategoriesItem);
        
        Set<Category> categories = categoryDAO.getCategories();
        if (categories != null) {
            for (Category category : categories) {
                MenuItem menuItem = new MenuItem(category.getName());
                menuItem.setOnAction(event -> handleCategorySelection(category));
                categoryMenuButton.getItems().add(menuItem);
            }
        }
    }
    
    /**
     * Loads the list of products for the current warehouse and category.
     */
    private void loadProducts(){
        productObservableList.clear();
        List<Product> products = warehouseDAO.getProductWithWarehouse(warehouse);
        if(selectedCategory != null)
            products = products.stream()
                    .filter(product -> product.getCategory().equals(selectedCategory))
                    .toList();
        productObservableList.addAll(products);
    }
    
    /**
     * Opens the form to add a new product to the warehouse.
     * 
     * @param warehouse The warehouse to add the product to
     */
    private void openAddProductForm(Warehouse warehouse) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddProductToWarehouse.fxml"));
            Parent root = loader.load();
            AddProductToWarehouseController addController = loader.getController();
            addController.setModalPane(modalPane);
            addController.setWarehouse(warehouse);
            modalPane.setContent(root);
            modalPane.show(root);
            addController.setOnCloseAction(() -> loadProducts());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Navigates back to the warehouse management screen.
     */
    private void goBack(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WarehouseManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) stackPane.getScene().getWindow();
            stage.setTitle("Warehouse Management");
            stackPane.getChildren().clear();
            stackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles category selection from the menu button.
     * 
     * @param category The selected category
     */
    private void handleCategorySelection(Category category) {
        selectedCategory = category;
        categoryMenuButton.setText(category.getName());
        loadProducts();
    }
    
    /**
     * Adjusts the layout dynamically when the width of the category menu changes.
     */
    private void setupDynamicLayoutAdjustment(){
        categoryMenuButton.widthProperty().addListener((observable, oldValue, newValue) -> {
            addProductButton.setLayoutX(categoryMenuButton.getLayoutX() + newValue.doubleValue() + 10);
        });
        categoryMenuButton.widthProperty().addListener((observable, oldValue, newValue) -> {
            label1.setLayoutX(categoryMenuButton.getLayoutX() + newValue.doubleValue() + 150);
            label2.setLayoutX(categoryMenuButton.getLayoutX() + newValue.doubleValue() + 150);
        });
    }
    
    /**
    * Displays a confirmation dialog to delete a product from the warehouse.
    * 
    * @param product The product to be deleted from the warehouse.
    */
    private void showDeleteConfirmation(Product product){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this product?");
        alert.setContentText("This action cannot be undone");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            warehouseDAO.deleteProdcutFromWarehouse(warehouse, product);
            productObservableList.remove(product);
        }
    }
}