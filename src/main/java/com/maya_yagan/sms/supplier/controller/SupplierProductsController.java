package com.maya_yagan.sms.supplier.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.supplier.service.SupplierService;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.ContextMenuUtil;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ExceptionHandler;
import com.maya_yagan.sms.util.MenuButtonUtil;
import com.maya_yagan.sms.util.ViewUtil;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FXML Controller class for managing products of a supplier.
 * 
 * This class provides functionality for displaying, adding editing, and deleting products of a specific supplier.
 * It supports operations such as editing supplier product price, filtering products by categories.
 * The controller interacts with the user interface via JavaFX components and communicates with the database via DAOs.
 *
 * @author Maya Yagan
 */
public class SupplierProductsController  implements Initializable {
    
    @FXML private Text supplierName;
    @FXML private TableView<SupplierProduct> productTableView;
    @FXML private TableColumn<SupplierProduct, String> nameColumn;   
    @FXML private TableColumn<SupplierProduct, String> unitColumn;   
    @FXML private TableColumn<SupplierProduct, Double> priceColumn;   
    @FXML private  Button addProductButton, backButton;   
    @FXML private  MenuButton categoryMenuButton;   
    @FXML private Label label1, label2; 
    @FXML private StackPane stackPane;
    
    private Runnable onCloseAction;
    private ModalPane modalPane;
    private Supplier supplier;
    private Category selectedCategory;
    private final SupplierService supplierService = new SupplierService();
    private final ProductService productService = new ProductService();
    private final ObservableList<SupplierProduct> productObservableList = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        setupEventHandlers();
        loadCategories();
        initializeModalPane();
        setupDynamicLayoutAdjustment();
        setupContextMenu();
    }    

    private void configureTableColumns(){
        productTableView.setItems(productObservableList);
        productTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        nameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        priceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getUnit().getFullName()));
    }
    
    private void setupEventHandlers(){
        addProductButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/supplier/AddProductToSupplier.fxml",
                (AddProductToSupplierController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setSupplier(supplier);
                    controller.setOnCloseAction(() -> loadProducts());
                }, modalPane));
        backButton.setOnAction(event -> goBack());
        
    }

    private void loadCategories() {
    MenuButtonUtil.populateMenuButton(
        categoryMenuButton,
        productService::getAllCategories,
        Category::getName,
        this::handleCategorySelection,
        "All Categories",
        () -> {
            selectedCategory = null;
            categoryMenuButton.setText("All Categories");
            loadProducts();
        }
    );
}


    private void loadProducts(){
        productObservableList.clear();
        List<SupplierProduct> products = supplierService.getSupplierProductsByCategory(supplier,selectedCategory);  
        productObservableList.addAll(products);
    }
    
    private void handleCategorySelection(Category category){
        selectedCategory = category;
        categoryMenuButton.setText(category.getName());
        loadProducts();
    }

    public void setSupplier(Supplier supplier){
        this.supplier = supplier;
        supplierName.setText(supplier.getName());
        loadProducts();
    }

    private void initializeModalPane() {
        StackPane root = stackPane;
        modalPane = new ModalPane();
        modalPane.setId("modalPane");
        root.getChildren().add(modalPane);
    }

    private void goBack(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/supplier/SupplierManagement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) stackPane.getScene().getWindow();
            stage.setTitle("Supplier Management");
            stackPane.getChildren().clear();
            stackPane.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupDynamicLayoutAdjustment(){
        categoryMenuButton.widthProperty().addListener((observable, oldValue, newValue) -> {
            addProductButton.setLayoutX(categoryMenuButton.getLayoutX() + newValue.doubleValue() + 10);
            label1.setLayoutX(categoryMenuButton.getLayoutX() + newValue.doubleValue() + 150);
            label2.setLayoutX(categoryMenuButton.getLayoutX() + newValue.doubleValue() + 150);
        });
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
    
     private void setupContextMenu() {
    productTableView.setRowFactory(tv -> {
        TableRow<SupplierProduct> row = new TableRow<>();
        row.itemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                List<ContextMenuUtil.MenuItemConfig<SupplierProduct>> menuItems = new ArrayList<>();

                // Edit Price
                menuItems.add(new ContextMenuUtil.MenuItemConfig<>(
                        "Edit Price",
                        (sp, r) -> handleEditPriceAction(sp)
                ));

                // Delete Product
                menuItems.add(new ContextMenuUtil.MenuItemConfig<>(
                        "Delete Product",
                        (sp, r) -> AlertUtil.showDeleteConfirmation(
                                sp,
                                "Delete Product",
                                "Are you sure you want to delete this product?",
                                "This action cannot be undone.",
                                deletedProduct -> {
                                    supplierService.deleteProductFromSupplier(
                                            deletedProduct.getSupplier(),
                                            deletedProduct.getProduct()
                                    );
                                    productObservableList.remove(deletedProduct);
                                    AlertUtil.showAlert(
                                            Alert.AlertType.INFORMATION,
                                            "Product Deleted",
                                            "The product has been successfully deleted.");
                                }
                        )
                ));

                ContextMenu contextMenu = ContextMenuUtil.createContextMenu(row, newItem, menuItems);
                row.setContextMenu(contextMenu);
            } else {
                row.setContextMenu(null);
            }
        });
        return row;
    });
}
     private void handleEditPriceAction(SupplierProduct sp) {
    ViewUtil.showFloatInputDialog(
            "Edit Price",
            "Enter new price for " + sp.getProduct().getName(),
            "Price (" + sp.getProduct().getUnit().getShortName() + ")",
            sp.getPrice(),
            "Price",
            newPrice -> {
                try {
                    sp.setPrice(newPrice.floatValue()); 
                    supplierService.updateSupplierProduct(sp);
                    productTableView.refresh();
                    AlertUtil.showAlert(
                            Alert.AlertType.INFORMATION,
                            "Success",
                            "The product price was successfully updated.");
                } catch (CustomException ex) {
                    ExceptionHandler.handleException(ex);
                    productTableView.refresh();
                }
            }
    );
}

     


} 