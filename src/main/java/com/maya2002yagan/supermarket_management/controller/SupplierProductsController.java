package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.SupplierDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Supplier;
import com.maya2002yagan.supermarket_management.model.SupplierProduct;
import com.maya2002yagan.supermarket_management.util.FormHelper;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * FXML Controller class for managing products of a supplier.
 * 
 * This class provides functionality for displaying, adding editing, and deleting products of a specific supplier.
 * It supports operations such as editing supplier product price, filtering products by categories.
 * The controller interacts with the user interface via JavaFX components and communicates with the database via DAOs.
 *
 * @author Maya Yagan
 */
public class SupplierProductsController implements Initializable {
    
    @FXML
    private Text supplierName;
    @FXML
    private TableView<SupplierProduct> productTableView;
    @FXML
    private TableColumn<SupplierProduct, String> nameColumn;
    @FXML
    private TableColumn<SupplierProduct, String> priceColumn;
    @FXML
    private  TableColumn<SupplierProduct, Void> deleteColumn;
    @FXML
    private  Button addProductButton, backButton;
    @FXML
    private  MenuButton categoryMenuButton;
    @FXML
    private Label label1, label2;
    @FXML
    private StackPane stackPane;
    
    private ModalPane modalPane;
    private Supplier supplier;
    private Category selectedCategory;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ObservableList<SupplierProduct> productObservableList = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller class and sets up the UI components and behaviors.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        setupEventHandlers();
        loadCategories();
        setupDeleteColumn();
        setupColumnForEditing();
        initializeModalPane();
        setupDynamicLayoutAdjustment();
    }    
    
    /**
     * Configures the table columns for displaying the products.
     */
    private void configureTableColumns(){
        productTableView.setItems(productObservableList);
        productTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        nameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        priceColumn.setCellValueFactory(cellData -> {
                //new SimpleObjectProperty<>(cellData.getValue().getPrice())
                if(cellData.getValue().getPrice() == 0 || cellData.getValue() == null)
                    return new SimpleStringProperty("Unavailable");
                else
                    return new SimpleStringProperty(String.valueOf(cellData.getValue().getPrice()));
        });
    }
    
    /**
     * Sets up handlers for clicking buttons
     */
    private void setupEventHandlers(){
        addProductButton.setOnAction(event -> FormHelper.openForm("/fxml/AddProductToSupplier.fxml",
                (AddProductToSupplierController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setSupplier(supplier);
                    controller.setOnCloseAction(() -> loadProducts());
                }, modalPane));
        backButton.setOnAction(event -> goBack());
        
    }
    
    /**
     * Loads the list of product categories into the menu button.
     */
    private void loadCategories(){
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
     * Loads the list of products for the current supplier and category.
     */
    private void loadProducts(){
        productObservableList.clear();
        List<SupplierProduct> products = supplierDAO.getSupplierProductPairs(supplier);
        if(selectedCategory != null)
            products = products.stream()
                    .filter(product -> product.getProduct().getCategory().equals(selectedCategory))
                    .toList();
        productObservableList.addAll(products);
    }
    
    /**
     * Handles category selection from the button and loads products accordingly.
     * 
     * @param category The selected category from the menu
     */
    private void handleCategorySelection(Category category){
        selectedCategory = category;
        categoryMenuButton.setText(category.getName());
        loadProducts();
    }
    
    /**
     * Configures the delete button for removing a product from a supplier.
     */
    private void setupDeleteColumn(){
        deleteColumn.setCellFactory(param -> {
            return new TableCell<SupplierProduct, Void>(){
                private final Button deleteButton = new Button(null, new FontIcon(Feather.TRASH));
                {
                    deleteButton.getStyleClass().add(Styles.DANGER);
                    deleteButton.setOnAction(event -> {
                        SupplierProduct supplierProduct = getTableView().getItems().get(getIndex());
                        ShowAlert.showDeleteConfirmation(supplierProduct,
                                "Delete Confirmation",
                                "Are you sure you want to delete this product?",
                                "This action cannot be undone",
                                (SupplierProduct p) -> {
                                    supplierDAO.deleteProductFromSupplier(supplier, supplierProduct.getProduct());
                                    productObservableList.remove(supplierProduct);
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
     * Configures the price column for in-line editing of supplier product.
     */
    private void setupColumnForEditing(){
        priceColumn.setCellFactory(
                TextFieldTableCell.forTableColumn(new StringConverter<String>(){
                        @Override
                        public String toString(String object) {
                            return object != null ? object : "";
                        }

                        @Override
                        public String fromString(String string) {
                            try{
                                return string;
                            } catch(NumberFormatException e){
                                ShowAlert.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid decimal number.");
                                return null;
                            }
                        }
                }));
        
        priceColumn.setOnEditCommit(event -> {
            SupplierProduct product = event.getRowValue();
            String newPriceStr = event.getNewValue().toString();
            try{
                float newPrice = Float.parseFloat(newPriceStr);
                if(newPrice < 0){
                    ShowAlert.showAlert(Alert.AlertType.ERROR, "Invalid Input", "The price cannot be negative.");
                    productTableView.refresh();
                    return;
                }
                
                SupplierProduct supplierProduct = supplier.getSupplierProducts()
                        .stream()
                        .filter(sp -> sp.getProduct().equals(product.getProduct()))
                        .findFirst()
                        .orElse(null);
                
                if(supplierProduct != null){
                    supplierProduct.setPrice(newPrice);
                    supplierDAO.updateSupplier(supplier);
                    Supplier refreshedSupplier = supplierDAO.getSupplierById(supplier.getId());
                    supplier.setSupplierProducts(refreshedSupplier.getSupplierProducts());
                    productTableView.refresh();
                    
                    /* I did this becasue the productTableView is displaying 
                    an observable list that hasn't been updated with the new price.
                    If we don't do this explicitly, the updated price won't be shown
                    after pressing Enter. Previously, the SupplierProduct object in
                    the Supplier instance was updated. Now, after updating SupplierProduct
                    in the Supplier instance, we make the observable list aware of this change.
                    */
                    int index = productObservableList.indexOf(product);
                    if (index != -1)
                        productObservableList.set(index, supplierProduct);

                    ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Success", "The price has been updated successfully.");
                }
            } catch(NumberFormatException e){
                ShowAlert.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for the price.");
                productTableView.refresh();
            }
        });
        productTableView.setEditable(true);
    }
    
    /**
     * Sets the supplier and loads products accordingly.
     * 
     * @param supplier The supplier to be set
     */
    public void setSupplier(Supplier supplier){
        this.supplier = supplier;
        supplierName.setText(supplier.getName());
        loadProducts();
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
     * Navigates back to the warehouse management screen.
     */
    private void goBack(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SupplierManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) stackPane.getScene().getWindow();
            stage.setTitle("Supplier Management");
            stackPane.getChildren().clear();
            stackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
