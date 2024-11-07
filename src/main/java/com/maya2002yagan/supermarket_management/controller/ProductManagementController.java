package com.maya2002yagan.supermarket_management.controller;

import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.ProductDAO;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.Category;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
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
    private Button addProductButton;
    @FXML
    private Button addCategoryButton;
    @FXML
    private Button editCategoriesButton;
    
    @FXML
    private MenuButton categoryMenuButton;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ObservableList<Product> productObservableList = FXCollections.observableArrayList();

    
    /**
     * Initializes the controller class.
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        addProductButton.setOnAction(event -> openAddProductForm());
        addCategoryButton.setOnAction(event -> openAddCategoryForm());
        editCategoriesButton.setOnAction(event -> openEditCategories());
        productTableView.setItems(productObservableList);
        productTableView.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    Product selectedProduct = row.getItem();
                    openEditProductModal(selectedProduct);
                }
            });
            return row;
        });
        loadCategories();
    }

    private void loadCategories() {
        categoryMenuButton.getItems().clear();
        Set<Category> categories = categoryDAO.getCategories();

        if (categories != null) {
            for (Category category : categories) {
                MenuItem menuItem = new MenuItem(category.getName());
                menuItem.setOnAction(event -> handleCategorySelection(category));
                categoryMenuButton.getItems().add(menuItem);
            }
        }
    }
    
    private void configureTableColumns(){
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productionDate.setCellValueFactory(new PropertyValueFactory<>("productionDate"));
        expirationDateColumn.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));
    }

    private void handleCategorySelection(Category category) {
        categoryMenuButton.setText(category.getName());
        Set<Product> products = productDAO.getProductsByCategory(category);
        productObservableList.clear();
        if (products != null) {
            productObservableList.addAll(products);
        }
    }
    
    private void openEditProductModal(Product product){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditProductForm.fxml"));
            Parent root = loader.load();
            EditProductFormController editController = loader.getController();
            editController.setProduct(product);
            Stage stage = new Stage();
            stage.setTitle("Edit Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            handleCategorySelection(product.getCategory());
        } catch(IOException e) {
            e.printStackTrace();
        }
        
    }
    
    private void openAddProductForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddProductForm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Product");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            // Refresh the product list for the currently selected category
        String selectedCategoryName = categoryMenuButton.getText();
        if (!selectedCategoryName.equals("Category")) { 
            Category selectedCategory = categoryDAO.getCategoryByName(selectedCategoryName);
            handleCategorySelection(selectedCategory);
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void openAddCategoryForm(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddCategory.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Category");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadCategories();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void openEditCategories(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditCategories.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Edit Categories");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadCategories();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goToHomePage(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HomePage.fxml"));
            Parent userManagementRoot = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(userManagementRoot));
            stage.setTitle("Product Management");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
