package com.maya_yagan.sms.supplier.controller;

import com.maya_yagan.sms.supplier.controller.EditSupplierController;
import com.maya_yagan.sms.supplier.controller.AddSupplierController;
import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.order.controller.AddOrderController;
import com.maya_yagan.sms.supplier.dao.SupplierDAO;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.util.ViewUtil;
import com.maya_yagan.sms.util.AlertUtil;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * FXML Controller class for managing suppliers in the supermarket management system.
 * Handles supplier listing, editing, deleting and adding. 
 *
 * @author Maya Yagan
 */
public class SupplierManagementController implements Initializable {

    @FXML
    private TableView<Supplier> suppliersTable;
    @FXML
    private TableColumn<Supplier, Integer> idColumn;
    @FXML
    TableColumn<Supplier, String> nameColumn, emailColumn, phoneNumberColumn, categoryColumn;
    @FXML
    private TableColumn<Supplier, Void> deleteColumn;
    @FXML
    private Button makeOrderButton, addSupplierButton;
    @FXML
    private StackPane stackPane;
    
    private ModalPane modalPane;
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ObservableList<Supplier> supplierObservableList = FXCollections.observableArrayList();
    private Supplier supplier;
    
    /**
     * Initializes the controller class, setting up the table, event handlers, and modal pane
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        setupDeleteColumn();
        setupRowInteractions();
        loadSuppliers();
        setupEventHandlers();
        initializeModalPane();
    }    
    
    /**
     * Configures table columns by setting up property bindings and cell factories
     */
    private void configureTableColumns(){
        suppliersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        suppliersTable.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        categoryColumn.setCellFactory(column -> new TableCell<Supplier, String>(){
            private final ComboBox<String> comboBox = new ComboBox<>();
            
            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if(empty || getTableRow() == null || getTableRow().getItem() == null){
                    setGraphic(null);
                    setText(null);
                    return;
                }
                else{
                    Supplier supplier = getTableRow().getItem();
                    List<String> categories = fetchSupplierCategories(supplier);
                    if(categories.isEmpty()){
                        setGraphic(null);
                        setText("No Category");
                    }
                    else if(categories.size() == 1){
                        setGraphic(null);
                        setText(categories.get(0));
                    }
                    else{
                        comboBox.setItems(FXCollections.observableArrayList(categories));
                        comboBox.getSelectionModel().select(0);
                        setGraphic(comboBox);
                        setText(null);
                    }
                }
            }
        });
    }
    
    /**
     * Loads supplier data from the database into the table
     */
    private void loadSuppliers(){
        supplierObservableList.clear();
        supplierObservableList.addAll(supplierDAO.getSuppliers());
        suppliersTable.setItems(supplierObservableList);
    }
    
    /**
     * Sets up event handlers for buttons
     */
    private void setupEventHandlers(){
        addSupplierButton.setOnAction(event -> ViewUtil.displayView("/view/supplier/AddSupplier.fxml",
                (AddSupplierController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> loadSuppliers());
                }, modalPane));
        makeOrderButton.setOnAction(event -> ViewUtil.displayView("/view/order/OrderManagement.fxml",
                (AddOrderController controller) -> {}, modalPane));
    }
    
    /**
     * Configures row interactions, including right-click and double-click actions
     */
    private void setupRowInteractions(){
        suppliersTable.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            ContextMenu contextMenu = createContextMenu(row);
            row.setContextMenu(contextMenu);
            row.setOnMouseClicked(event -> handleRowClick(event, row));
            return row;
        });
    }
    
    /**
     * Creates a context menu for a table row
     * 
     * @param row The table row to attach the context menu to
     * @return The configured context menu
     */
    private ContextMenu createContextMenu(TableRow<Supplier> row){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editSupplier = new MenuItem("Edit this supplier");
        editSupplier.setOnAction(event -> {
            Supplier selectedSupplier = row.getItem();
            if(selectedSupplier != null)
                ViewUtil.displayView("/view/supplier/EditSupplier.fxml",
                        (EditSupplierController controller) -> {
                            controller.setSupplier(selectedSupplier);
                            controller.setModalPane(modalPane);
                            controller.setOnCloseAction(() -> loadSuppliers());
                        }, modalPane);
        });
        contextMenu.getItems().add(editSupplier);
        return contextMenu;
    }
    
    /**
     * Handles row click events, including right-click for context menu and 
     * double-click to open supplier products details
     * 
     * @param event The mouse event
     * @param row The clicked table row
     */
    private void handleRowClick(MouseEvent event, TableRow<Supplier> row){
        if(row.isEmpty()) return;
        
        if(event.getButton() == MouseButton.SECONDARY)
            row.getContextMenu().show(row, event.getScreenX(), event.getScreenY());
        else
            row.getContextMenu().hide();
        
        if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY){
            Supplier selectedSupplier = row.getItem();
            ViewUtil.displayView("/view/supplier/SupplierProducts.fxml",
                    (SupplierProductsController controller) -> {
                        controller.setSupplier(selectedSupplier);
                    }, modalPane);
        }
    }
    
    /**
     * Initializes and configures the modal pane.
     */
    private void initializeModalPane(){
        StackPane root = stackPane;
        modalPane = new ModalPane();
        modalPane.setId("modalPane");
        root.getChildren().add(modalPane);
    }
    
    /**
     * Fetches the list of categories for a given supplier
     * 
     * @param supplier The supplier whose categories are retrieved
     * @return A list of category names
     */
    private List<String> fetchSupplierCategories(Supplier supplier){
        List<Product> products = supplierDAO.getSupplierProducts(supplier);
        if(products != null){
            return products.stream()
                    .map(product -> product.getCategory().getName())
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    /**
     * Sets up the delete button with delete buttons for each row
     */
    private void setupDeleteColumn(){
        deleteColumn.setCellFactory(column -> {
            return new TableCell<Supplier, Void>(){
                private final Button deleteButton = new Button(null, new FontIcon(Feather.TRASH));
                {
                    deleteButton.getStyleClass().add(Styles.DANGER);
                    deleteButton.setOnAction(event -> {
                        Supplier supplier = getTableView().getItems().get(getIndex());
                        AlertUtil.showDeleteConfirmation(supplier,
                            "Delete Confirmation",
                            "Are you sure you want to delete this supplier?",
                            "This action cannot be undone",
                            (Supplier s) -> {
                                supplierDAO.deleteSupplier(s.getId());
                                supplierObservableList.remove(supplier);
                            }
                        );
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty){
                    super.updateItem(item, empty);
                    if(empty) setGraphic(null);
                    else setGraphic(deleteButton);
                }
            };
        });
    }
    
    /**
     * Sets the supplier
     * 
     * @param supplier the supplier to be st
     */
    public void setSupplier(Supplier supplier){
        this.supplier = supplier;
    }
}
