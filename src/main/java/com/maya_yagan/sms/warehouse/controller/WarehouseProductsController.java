package com.maya_yagan.sms.warehouse.controller;

import com.maya_yagan.sms.warehouse.controller.AddProductToWarehouseController;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.order.controller.AddOrderController;
import com.maya_yagan.sms.product.dao.CategoryDAO;
import com.maya_yagan.sms.warehouse.dao.WarehouseDAO;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.util.ViewUtil;
import com.maya_yagan.sms.util.AlertUtil;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
    private TableColumn<Product, Integer> amountColumn;
    @FXML
    private TableColumn<Product, String> unitColumn;
    @FXML
    private TableColumn<Product, Void> deleteColumn;
    @FXML
    private Button addProductButton, backButton, orderButton;
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
        addProductButton.setOnAction(event -> ViewUtil.displayView("/view/warehouse/AddProductToWarehouse.fxml", 
                (AddProductToWarehouseController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setWarehouse(warehouse);
                    controller.setOnCloseAction(() -> loadProducts());
                }, modalPane));
        orderButton.setOnAction(event -> ViewUtil.displayView("/view/order/OrderManagement.fxml",
                (AddOrderController controller) -> {}, modalPane));
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
        expirationDateColumn.setCellValueFactory(cellData -> {
            if(cellData.getValue().getExpirationDate() == null)
                return new SimpleStringProperty("No Expiry Date");
            else
                return new SimpleStringProperty(cellData.getValue().getExpirationDate().toString());
        });
        unitColumn.setCellValueFactory(cellData -> {
                return new SimpleStringProperty(cellData.getValue().getUnit().getShortName());    
        });
        amountColumn.setCellValueFactory(data -> {
            Product product = data.getValue();
            return warehouse.getProductWarehouses()
                    .stream()
                    .filter(pw -> pw.getProduct().equals(product))
                    .map(pw -> new ReadOnlyObjectWrapper<>(pw.getAmount()))
                    .findFirst()
                    .orElse(new ReadOnlyObjectWrapper(0));
        });
    }
    
    /**
     * Configures the amount column for in-line editing of product quantities.
     */
    private void setupColumnForEditing() {
        amountColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object != null ? object.toString() : "";
            }

            @Override
            public Integer fromString(String string) {
                try{
                    return Integer.valueOf(string);
                } catch(NumberFormatException e){
                    AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number.");
                    return null;
                }
            }
        }));

        amountColumn.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            Integer newAmount = event.getNewValue();
            
            try {
                if (newAmount < 0) {
                    AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid Input", "The amount cannot be negative.");
                    productTableView.refresh();
                    return;
                }

                int currentUsage = warehouse.getProductWarehouses().stream()
                                            .mapToInt(ProductWarehouse::getAmount).sum();
                int remainingCapacity = warehouse.getCapacity() - currentUsage;
                
                if (newAmount > remainingCapacity) {
                    if (remainingCapacity == 0)
                        AlertUtil.showAlert(Alert.AlertType.ERROR, "Warehouse Full", "The warehouse is already full. No more products can be added.");
                    else 
                        AlertUtil.showAlert(Alert.AlertType.ERROR, "Capacity Exceeded", 
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
                    AlertUtil.showAlert(Alert.AlertType.INFORMATION, "Success", "The product amount was successfully updated.");
                }
            } catch (NumberFormatException e) {
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for the amount.");
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
                        AlertUtil.showDeleteConfirmation(product,
                                "Delete Confirmation",
                                "Are you sure you want to delete this product?",
                                "This action cannot be undone",
                                (Product p) -> {
                                    warehouseDAO.deleteProdcutFromWarehouse(warehouse, product);
                                    productObservableList.remove(product);
                                });
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
     * Navigates back to the warehouse management screen.
     */
    private void goBack(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/warehouse/WarehouseManagement.fxml"));
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
            orderButton.setLayoutX(categoryMenuButton.getLayoutX() + newValue.doubleValue() + 120);
            label1.setLayoutX(categoryMenuButton.getLayoutX() + newValue.doubleValue() + 250);
            label2.setLayoutX(categoryMenuButton.getLayoutX() + newValue.doubleValue() + 250);
        });
    }
}