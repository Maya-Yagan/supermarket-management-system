package com.maya_yagan.sms.order.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.product.dao.CategoryDAO;
import com.maya_yagan.sms.order.dao.OrderDAO;
import com.maya_yagan.sms.product.dao.ProductDAO;
import com.maya_yagan.sms.supplier.dao.SupplierDAO;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.order.model.OrderProduct;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.util.FormHelper;
import com.maya_yagan.sms.util.ShowAlert;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class
 *
 * @author Maya Yagan
 */
public class AddOrderController implements Initializable {

    @FXML
    private TableView<SupplierProduct> ordersTableView;
    @FXML
    private TableColumn<SupplierProduct, Boolean> addToOrderColumn;
    @FXML
    private TableColumn<SupplierProduct, String> supplierColumn, productColumn;
    @FXML
    private TableColumn<SupplierProduct, Double> priceColumn;
    @FXML
    private Button ordersButton, saveOrderButton;
    @FXML
    private Label totalPrice;
    @FXML
    private MenuButton categoryMenuButton, supplierMenuButton;
    @FXML
    private StackPane stackPane;
    
    private ModalPane modalPane;
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final ObservableList<SupplierProduct> productObservableList = FXCollections.observableArrayList();
    private Category selectedCategory; 
    private Supplier selectedSupplier;
    private final Map<SupplierProduct, SimpleBooleanProperty> selectedProducts = new HashMap<>();
    private final Map<SupplierProduct, Integer> productsAmount = new HashMap<>();
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeModalPane();
        configureTableColumns();
        loadCategories();
        loadSuppliers();
        populateTable();
        setupEventHandlers();
        setupDynamicLayoutAdjustment();
    }

    private void configureTableColumns(){
        ordersTableView.setItems(productObservableList);
        ordersTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ordersTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        supplierColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSupplier().getName())
        );
        
        productColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getProduct().getName())
        );
        
        priceColumn.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject()
        );
        
        priceColumn.setCellFactory(column -> new TableCell<SupplierProduct, Double>(){
            @Override
            protected void updateItem(Double price, boolean empty){
                super.updateItem(price, empty);
                if(empty || price == null)
                    setText(null);
                else
                    setText(String.format("%.2f", price));
            }
        });
        setupAddColumn();
    }
    
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
    
    private void loadSuppliers(){
        supplierMenuButton.getItems().clear();
        Set<Supplier> suppliers = supplierDAO.getSuppliers();
        if(suppliers != null){
            for(Supplier supplier : suppliers){
                MenuItem menuItem = new MenuItem(supplier.getName());
                menuItem.setOnAction(event -> handleSupplierSelection(supplier));
                supplierMenuButton.getItems().add(menuItem);
            }
        }
        supplierMenuButton.setText("Select Supplier");
    }
    
    private void handleCategorySelection(Category category){
        selectedCategory = category;
        categoryMenuButton.setText(category.getName());
        populateTable();
    }
    
    private void handleSupplierSelection(Supplier supplier){
        selectedSupplier = supplier;
        supplierMenuButton.setText(supplier.getName());
        populateTable();
    }
    
    private void populateTable(){
        productObservableList.clear();
        selectedProducts.clear();
        
        if(selectedSupplier == null)
            return;
        
        Set<Product> products = (selectedCategory == null)
                ? productDAO.getProducts()
                : productDAO.getProductsByCategory(selectedCategory);
        
        for(Product product : products){
            for(SupplierProduct sp : product.getSupplierProducts()){
                if(sp.getSupplier().equals(selectedSupplier))
                    productObservableList.add(sp);
            }
        }
    }
    
    private void setupEventHandlers(){
        ordersButton.setOnAction(event -> FormHelper.openForm(
                "/view/order/OrderManagement.fxml",
                (OrderManagementController controller) -> {
                    
                }, modalPane));
        saveOrderButton.setOnAction(event -> saveOrder());
    }
    
    private void saveOrder(){
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
            Order order = new Order();
            order.setName(selectedSupplier.getName() + " Order - " + LocalDate.now());
            order.setOrderDate(LocalDate.now());
            order.setSupplier(selectedSupplier);
            ////////////////////////////////////add the logged in user later!!!!!!!!!!!!!!!!!!!!
            ////////////////////////////////////order.setUser(currentUser);
            Set<OrderProduct> orderProducts = new HashSet<>();
            for(SupplierProduct sp : selectedItems){
                int amount = productsAmount.getOrDefault(sp, 1);
                OrderProduct op = new OrderProduct(order, sp.getProduct(), amount);
                orderProducts.add(op);
            }
            order.setOrderProducts(orderProducts);
            orderDAO.insertOrder(order);
            ShowAlert.showAlert(Alert.AlertType.INFORMATION, "Order Saved",
                    "The order has been saved successfully");
            selectedProducts.clear(); 
            productsAmount.clear();
            ordersTableView.refresh();
            updateTotals();
        }
    }
    
    private void setupAddColumn() {
        ordersTableView.setEditable(true);
        addToOrderColumn.setEditable(true);
        addToOrderColumn.setCellValueFactory(cellData -> {
            SupplierProduct supplierProduct = cellData.getValue();
            SimpleBooleanProperty selectedProperty = 
                    selectedProducts.computeIfAbsent(supplierProduct, p -> {
                        SimpleBooleanProperty prop = new SimpleBooleanProperty(false);
                        prop.addListener((obs, oldVal, newVal) -> {
                            if(newVal){ //checkbox is checked
                                TextInputDialog dialog = new TextInputDialog("1");
                                dialog.setTitle("Enter the Amount");
                                dialog.setHeaderText("Enter the amount for " +
                                        supplierProduct.getProduct().getName());
                                dialog.setContentText("Amount(" + 
                                        supplierProduct.getProduct()
                                        .getUnit().getShortName() + "):");

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
                                else //the user cancelled the dialog
                                    prop.set(false);
                            }
                            else //the user unselected a checkbox, so remove any stored amount
                                productsAmount.remove(supplierProduct);
                            updateTotals();
                        });
                        return prop;
                    });
            return selectedProperty;
        });
        addToOrderColumn.setCellFactory(CheckBoxTableCell.forTableColumn(addToOrderColumn));
    }
    
    private void updateTotals(){
        float totalPriceValue = selectedProducts.entrySet().stream()
                .filter(entry -> entry.getValue().get())
                .map(entry -> entry.getKey().getPrice() * productsAmount.getOrDefault(entry.getKey(), 1))
                .reduce(0f, Float::sum);
        totalPrice.setText(String.format("%.2f", totalPriceValue));
    }
    
    private void setupDynamicLayoutAdjustment(){
        categoryMenuButton.widthProperty().addListener((observableList, oldValue, newValue) -> {
            supplierMenuButton.setLayoutX(categoryMenuButton.getLayoutX() + newValue.doubleValue() + 10);
        });
    }
    
    private void initializeModalPane() {
        StackPane root = stackPane;
        modalPane = new ModalPane();
        modalPane.setId("modalPane");
        root.getChildren().add(modalPane);
    }
}

