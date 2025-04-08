package com.maya_yagan.sms.product.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.util.MenuButtonUtil;
import com.maya_yagan.sms.util.ViewUtil;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class for managing products in the supermarket management system.
 *
 * @author Maya Yagan
 */
public class ProductManagementController implements Initializable {

    @FXML private TableView<Product> productTableView;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn, productionDate, expirationDateColumn, discountsColumn, unitColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private Button addProductButton, addCategoryButton, editCategoriesButton;
    @FXML private MenuButton categoryMenuButton;
    @FXML private StackPane stackPane;
    
    private ModalPane modalPane;
    private final ProductService productService = new ProductService();
    private final ObservableList<Product> productObservableList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        loadCategories();
        setupEventHandlers();
        setupDynamicLayoutAdjustment();
        refreshTable("All Categories");
        modalPane = ViewUtil.initializeModalPane(stackPane);
        productTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
    }
    
    private void loadCategories() {
        MenuButtonUtil.populateMenuButton(
       categoryMenuButton,
                productService::getAllCategories, 
                Category::getName,
                this::handleCategorySelection,
     "All Categories",
                () -> refreshTable("All Categories")
        );
    }
    
    private void refreshTable(String categoryName) {
        Set<Product> products = productService.getFilteredProductsByCategory(categoryName);
        productObservableList.setAll(products);
    }
    
    private void configureTableColumns(){
        productTableView.setItems(productObservableList);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productionDate.setCellValueFactory(new PropertyValueFactory<>("productionDate"));
        expirationDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(productService.formatExpirationDate(cellData.getValue()))
        );
        unitColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getUnit().getFullName())
        );
    }
    
    private void setupEventHandlers(){
        addProductButton.setOnAction(event -> ViewUtil.displayView("/view/product/AddProduct.fxml",
                (AddProductController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> 
                    refreshTable(categoryMenuButton.getText()));
                }, modalPane));
        
        addCategoryButton.setOnAction(event -> ViewUtil.displayView("/view/product/AddCategory.fxml",
                (AddCategoryController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> loadCategories());
                }, modalPane));

        editCategoriesButton.setOnAction(event -> ViewUtil.displayView("/view/product/EditCategories.fxml",
                (EditCategoriesController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> loadCategories());
                }, modalPane));

        productTableView.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    Product selectedProduct = row.getItem();
                    ViewUtil.displayView("/view/product/EditProduct.fxml", 
                            (EditProductController controller) -> {
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

    private void handleCategorySelection(Category category) {
        String categoryName = category.getName();
        categoryMenuButton.setText(categoryName);
        refreshTable(categoryName);
    }
    
    private void setupDynamicLayoutAdjustment(){
        ViewUtil.setupDynamicLayoutAdjustment(
                categoryMenuButton,
                Arrays.asList(addCategoryButton, editCategoriesButton, addProductButton),
                   Arrays.asList(10.0, 130.0, 260.0)
        );
    }
}