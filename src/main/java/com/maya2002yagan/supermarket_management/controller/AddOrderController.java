package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.dao.OrderDAO;
import com.maya2002yagan.supermarket_management.dao.ProductDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.model.Order;
import com.maya2002yagan.supermarket_management.model.OrderProduct;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.Supplier;
import com.maya2002yagan.supermarket_management.model.SupplierProduct;
import com.maya2002yagan.supermarket_management.util.FormHelper;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;

/**
 * Controller class for adding an order.
 * Handles product selection, category filtering, and order saving.
 * 
 * @author Maya Yagan
 */
public class AddOrderController implements Initializable {

    @FXML
    private TableView productsTable;
    @FXML
    private TableColumn<SupplierProduct, Boolean> addToOrderColumn;
    @FXML
    private TableColumn<SupplierProduct, String> productColumn;
    @FXML
    private TableColumn<SupplierProduct, Float> priceColumn;
    @FXML
    private MenuButton categoryMenuButton;
    
    private ModalPane modalPane;
    private Runnable onCloseAction;
    private Order order;
    private Category selectedCategory;
    private Supplier selectedSupplier;

    private final ObservableList<SupplierProduct> productsObservableList = FXCollections.observableArrayList();
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final Map<SupplierProduct, SimpleBooleanProperty> selectedProducts = new HashMap<>();
    private final Map<SupplierProduct, Integer> productsAmount = new HashMap<>();
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        loadCategories();
        populateTable();
    }    
    
    /**
     * Configures the table columns.
     */
    private void configureTableColumns(){
        productsTable.setItems(productsObservableList);
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productsTable.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        productColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getProduct().getName())
        );
        
        priceColumn.setCellValueFactory(cellData -> 
                new SimpleFloatProperty(cellData.getValue().getPrice()).asObject()
        );
        setupAddColumn();
    }
    
    /**
     * Sets up the add-to-order column with checkboxes.
     */
    private void setupAddColumn(){
        productsTable.setEditable(true);
        addToOrderColumn.setEditable(true);
        addToOrderColumn.setCellValueFactory(cellData -> {
            SupplierProduct supplierProduct = cellData.getValue();
            SimpleBooleanProperty selectedProperty =
                    selectedProducts.computeIfAbsent(supplierProduct, p -> {
                        SimpleBooleanProperty prop = new SimpleBooleanProperty(false);
                        prop.addListener((obs, oldVal, newVal) -> {
                            if(newVal){
                                TextInputDialog dialog = new TextInputDialog("1");
                                dialog.setTitle("Enter the Amount");
                                dialog.setHeaderText("Enter the amount for " +
                                        supplierProduct.getProduct().getName());
                                dialog.setContentText("Amount:");

                                Optional<String> result = dialog.showAndWait();
                                if(result.isPresent()){
                                    try{
                                        int amount = Integer.parseInt(result.get());
                                        if(amount <= 0){
                                            prop.set(false);
                                            ShowAlert.showAlert(Alert.AlertType.ERROR,
                                                    "Invalid Input", "Amount must be greater than zero");
                                        } 
                                        else
                                            productsAmount.put(supplierProduct, amount);
                                    } catch(NumberFormatException e){
                                        prop.set(false);
                                        ShowAlert.showAlert(Alert.AlertType.ERROR,
                                                "Invalid Input", "Please enter a valid number");
                                    }
                                }
                                else
                                    prop.set(false);
                            }
                            else
                                productsAmount.remove(supplierProduct);
                        });
                        return prop;
                    });
            return selectedProperty;
        });
        addToOrderColumn.setCellFactory(CheckBoxTableCell.forTableColumn(addToOrderColumn));
    }
    
    /**
     * Loads available categories into the menu button.
     */
    private void loadCategories(){
        categoryMenuButton.getItems().clear();
        MenuItem allCategoriesMenuItem = new MenuItem("All Categories");
        categoryMenuButton.setText("All Categories");
        allCategoriesMenuItem.setOnAction(event -> {
            selectedCategory = null;
            categoryMenuButton.setText("All Categories");
            populateTable();
        });
        categoryMenuButton.getItems().add(allCategoriesMenuItem);
        Set<Category> categories = categoryDAO.getCategories();
        if(categories != null){
            for(Category category : categories){
                MenuItem menuItem = new MenuItem(category.getName());
                menuItem.setOnAction(event -> handleCategorySelection(category));
                categoryMenuButton.getItems().add(menuItem);
            }
        }
    }
    
    /**
     * Handles category selection.
     * @param category The selected category.
     */
    private void handleCategorySelection(Category category){
        selectedCategory = category;
        categoryMenuButton.setText(category.getName());
        populateTable();
    }
    
    /**
     * Populates the table with products based on the selected category
     */
    private void populateTable(){
        productsObservableList.clear();
        selectedProducts.clear();
        
        if(selectedSupplier == null)
            return;
        
        Set<Product> products = (selectedCategory == null)
                ? productDAO.getProducts()
                : productDAO.getProductsByCategory(selectedCategory);
        
        for(Product product : products){
            for(SupplierProduct sp : product.getSupplierProducts()){
                if(sp.getSupplier().equals(selectedSupplier))
                    productsObservableList.add(sp);
            }
        }
    }
    
    /**
     * Saves the selected products to the order.
     */
    public void saveProducts(){
        List<SupplierProduct> selectedItems = new ArrayList<>();
        for(Map.Entry<SupplierProduct, SimpleBooleanProperty> entry : selectedProducts.entrySet()){
            if(entry.getValue().get())
                selectedItems.add(entry.getKey());
        }
        
        if(selectedItems.isEmpty()){
            ShowAlert.showAlert(Alert.AlertType.INFORMATION,
                    "No Products Selected",
                    "Please select at least one product to order");
            return;
        }
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        
        grid.add(new Label("Product"), 0, 0);
        grid.add(new Label("Amount"), 1, 0);
        grid.add(new Label("Price"), 2, 0);
        grid.add(new Label("Total"), 3, 0);
        
        int row = 1;
        float grandTotal = 0;
        int grandQuantity = 0;
        
        for(SupplierProduct sp : selectedItems){
            int amount = productsAmount.getOrDefault(sp, 1);
            float price = sp.getPrice();
            float total = price * amount;
            grandTotal += total;
            grandQuantity += amount;
            
            grid.add(new Label(sp.getProduct().getName()), 0, row);
            grid.add(new Label(String.valueOf(amount)), 1, row);
            grid.add(new Label(String.format("%.2f", price)), 2, row);
            grid.add(new Label(String.format("%.2f", total)), 3, row);
            row++;
        }
        
        Label summaryLabel = new Label("Total Quantity: " + grandQuantity +
                                       "   Grand Total: " + String.format("%.2f", grandTotal));
        summaryLabel.setStyle("-fx-font-weight: bold;");
        grid.add(summaryLabel, 0, row, 4, 1);
        
        Optional<ButtonType> result = FormHelper.showCustomDialog(
                "Confirm Order",
                "Please review the order details below and click OK to save the order",
                grid);
        if(result.isPresent() && result.get() == ButtonType.OK){
            if(order == null){
                ShowAlert.showAlert(Alert.AlertType.ERROR, 
                        "No Order", "No order is currently being edited.");
                return;
            }
            
            for(SupplierProduct sp : selectedItems){
                int amount = productsAmount.getOrDefault(sp, 1);
                Optional<OrderProduct> existingOrderProduct = 
                        order.getOrderProducts().stream()
                        .filter(op -> op.getProduct().equals(sp.getProduct()))
                        .findFirst();
                
                if(existingOrderProduct.isPresent()){
                    OrderProduct op = existingOrderProduct.get();
                    op.setAmount(op.getAmount() + amount);
                }
                else{
                    OrderProduct newOrderProduct  = new OrderProduct(order, sp.getProduct(), amount);
                    order.getOrderProducts().add(newOrderProduct);
                }
            }
            orderDAO.updateOrder(order);
            closeModal();
            if(onCloseAction != null) onCloseAction.run();
        }
    }
    
    /**
     * Closes the modal window.
     */
    private void closeModal(){
        if(modalPane != null) modalPane.hide();
    }

    /**
     * Sets the modal pane.
     * @param modalPane The modal pane instance.
     */
    public void setModalPane(ModalPane modalPane) {
        this.modalPane = modalPane;
    }

    /**
     * Sets the order.
     * @param order The order instance.
     */
    public void setOrder(Order order) {
        this.order = order;
    }
    
    /**
     * Sets the supplier.
     * @param supplier The selected supplier.
     */
    public void setSupplier(Supplier supplier){
        this.selectedSupplier = supplier;
        populateTable();
    }
    
    /**
     * Sets the action to be performed when the modal closes.
     * @param onCloseAction The action to execute.
     */
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
}
