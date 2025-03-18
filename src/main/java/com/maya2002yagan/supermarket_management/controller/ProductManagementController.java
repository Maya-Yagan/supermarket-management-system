package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.ProductDAO;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.util.FormHelper;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class for managing products in the supermarket management system.
 * 
 * This controller handles the product management interface, including displaying product details in a table,
 * adding new products, editing existing products, and managing categories. The class interacts with the 
 * database to fetch and update product and category data.
 * 
 * @author Maya Yagan
 */
public class ProductManagementController implements Initializable {

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
    private TableColumn<Product, String> discountsColumn;
    @FXML
    private TableColumn<Product, String> unitColumn;
    @FXML
    private Button addProductButton;
    @FXML
    private Button addCategoryButton;
    @FXML
    private Button editCategoriesButton;
    @FXML
    private MenuButton categoryMenuButton;
    @FXML
    private StackPane stackPane;
    private ModalPane modalPane;
    private Category selectedCategory;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ObservableList<Product> productObservableList = FXCollections.observableArrayList();

    
    /**
     * Initializes the controller class.
     * 
     * This method configures table columns, sets up event handlers for buttons and table rows,
     * loads categories, and initializes the modal pane for displaying forms.
     * 
     * @param location The location of the FXML resource.
     * @param resources The resources to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        populateTable();
        setupEventHandlers();
        loadCategories();
        initializeModalPane();
        setupDynamicLayoutAdjustment();
        productTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
    }
    
    /**
     * Initializes the modal pane for the product management screen.
     * 
     * This method sets up the modal pane and adds it to the root stack pane.
     */
    private void initializeModalPane() {
        StackPane root = stackPane;
        modalPane = new ModalPane();
        modalPane.setId("modalPane");
        root.getChildren().add(modalPane);
    }
    
    /**
     * Loads categories from the database and populates the category menu button.
     * 
     * This method retrieves all categories and adds them as menu items to the category menu button.
     */
    private void loadCategories() {
        categoryMenuButton.getItems().clear();
        MenuItem allCategoriesItem = new MenuItem("All Categories");
        categoryMenuButton.setText("All Categories");
        allCategoriesItem.setOnAction(event -> {
            selectedCategory = null;
            categoryMenuButton.setText("All Categories");
            populateTable();
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
    
    private void populateTable(){
        Set<Product> products = productDAO.getProducts();
        productObservableList.clear();
        if(products != null) productObservableList.addAll(products);
    }
    
    /**
     * Configures the table columns for displaying product information.
     * 
     * This method sets the cell value factory for each column to display product attributes 
     * such as ID, name, price, production date, and expiration date.
     */
    private void configureTableColumns(){
        productTableView.setItems(productObservableList);
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
                return new SimpleStringProperty(cellData.getValue().getUnit().getFullName());    
        });
    }
    
    /**
     * Sets up event handlers for button and row clicks
     */
    private void setupEventHandlers(){
        addProductButton.setOnAction(event -> FormHelper.openForm("/fxml/AddProductForm.fxml",
                (AddProductFormController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> handleCloseAction());
                }, modalPane));
        
        addCategoryButton.setOnAction(event -> FormHelper.openForm("/fxml/AddCategory.fxml",
                (AddCategoryController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> loadCategories());
                }, modalPane));
        
        editCategoriesButton.setOnAction(event -> FormHelper.openForm("/fxml/EditCategories.fxml", 
                (EditCategoriesController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> loadCategories());
                }, modalPane));
        productTableView.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    Product selectedProduct = row.getItem();
                    FormHelper.openForm("/fxml/EditProductForm.fxml", 
                            (EditProductFormController controller) -> {
                                controller.setProduct(selectedProduct);
                                controller.setModalPane(modalPane);
                                controller.setOnCloseAction(() -> 
                                        handleCategorySelection(selectedProduct.getCategory()));
                            }, modalPane);
                }
            });
            return row;
        });
    }

    /**
     * Handles the selection of a category from the category menu button.
     * 
     * This method updates the displayed products in the table to show only those belonging 
     * to the selected category.
     * 
     * @param category The selected category.
     */
    private void handleCategorySelection(Category category) {
        categoryMenuButton.setText(category.getName());
        Set<Product> products = productDAO.getProductsByCategory(category);
        productObservableList.clear();
        if (products != null) {
            productObservableList.addAll(products);
        }
    }
    
    /**
     * Handles the close action after a new product is added.
     * 
     * This method refreshes the product list after adding a new product by selecting the appropriate category.
     */
    private void handleCloseAction(){
        String selectedCategoryName = categoryMenuButton.getText();
            if (!selectedCategoryName.equals("All Categories")) { 
                handleCategorySelection(
                        categoryDAO.getCategoryByName(selectedCategoryName)
                );
            }
    }
    
    private void setupDynamicLayoutAdjustment(){
        categoryMenuButton.widthProperty().addListener((obs, oldVal, newVal) -> {
            addCategoryButton.setLayoutX(categoryMenuButton.getLayoutX() + newVal.doubleValue() + 10);
            editCategoriesButton.setLayoutX(categoryMenuButton.getLayoutX() + newVal.doubleValue() + 130);
            addProductButton.setLayoutX(categoryMenuButton.getLayoutX() + newVal.doubleValue() + 260);
        });
    }
}
