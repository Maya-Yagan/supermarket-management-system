package com.maya_yagan.sms.supplier.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.order.controller.AddOrderController;
import com.maya_yagan.sms.product.controller.EditProductController;
import com.maya_yagan.sms.supplier.dao.SupplierDAO;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.service.SupplierService;
import com.maya_yagan.sms.util.ViewUtil;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.ContextMenuUtil;
import java.net.URL;
import java.util.ArrayList;
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
 *
 * @author Maya Yagan
 */
public class SupplierManagementController implements Initializable {

    @FXML private TableView<Supplier> suppliersTable;
    @FXML private TableColumn<Supplier, Integer> idColumn;
    @FXML TableColumn<Supplier, String> nameColumn, emailColumn, phoneNumberColumn, categoryColumn;
    @FXML private Button makeOrderButton, addSupplierButton;
    @FXML private StackPane stackPane;
    
    private ModalPane modalPane;
    private final SupplierService supplierService = new SupplierService();
    private final ObservableList<Supplier> supplierObservableList = FXCollections.observableArrayList();
    private Supplier supplier;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        loadSuppliers();
        setupEventHandlers();
        modalPane = ViewUtil.initializeModalPane(stackPane);
    }    
    
    
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
    
    
    private void loadSuppliers(){
        supplierObservableList.clear();
        supplierObservableList.addAll(supplierService.getAllSuppliers());
        suppliersTable.setItems(supplierObservableList);
    }
    
    private void setupEventHandlers(){
        addSupplierButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/supplier/AddSupplier.fxml",
                (AddSupplierController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> loadSuppliers());
                }, modalPane));
        makeOrderButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/order/OrderManagement.fxml",
                (AddOrderController controller) -> {}, modalPane));
        setupTableContextMenu();
        
    }
    
    private void setupTableContextMenu() {
        suppliersTable.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    List<ContextMenuUtil.MenuItemConfig<Supplier>> menuItems = new ArrayList<>();

                    menuItems.add(new ContextMenuUtil.MenuItemConfig<>(
                            "Delete Supplier",
                            (supplier, r) -> {
                                AlertUtil.showDeleteConfirmation(supplier,
                                        "Delete Supplier",
                                        "Are you sure you want to delete this Supplier?",
                                        "This action cannot be undone",
                                        (p) -> {
                                            supplierService.deleteSupplier(p.getId());
                                             loadSuppliers();
                                        }
                                );
                            }
                    ));
                    
                    menuItems.add(new ContextMenuUtil.MenuItemConfig<>(
                            "Edit supplier",
                            (supplier, r) -> {
                                ViewUtil.displayModalPaneView("/view/supplier/EditSupplier.fxml",
                                    (EditSupplierController controller) -> {
                                    controller.setSupplier(supplier);
                                    controller.setModalPane(modalPane);
                                    controller.setOnCloseAction(() -> loadSuppliers());
                                }, modalPane);
                            }
                    ));
                    
                    menuItems.add(new ContextMenuUtil.MenuItemConfig<>(
                            "View Details",
                            (supplier, r) -> {
                                ViewUtil.displayModalPaneView("/view/supplier/SupplierProducts.fxml",
                                    (SupplierProductsController controller) -> {
                                    controller.setSupplier(supplier);
                                    controller.setOnCloseAction(() -> loadSuppliers());
                                }, modalPane);
                            }
                    ));

                    ContextMenu contextMenu = ContextMenuUtil.createContextMenu(row, newVal, menuItems);
                    row.setContextMenu(contextMenu);
                } else {
                    row.setContextMenu(null);
                }
            });
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
                ViewUtil.displayModalPaneView("/view/supplier/EditSupplier.fxml",
                        (EditSupplierController controller) -> {
                            controller.setSupplier(selectedSupplier);
                            controller.setModalPane(modalPane);
                            controller.setOnCloseAction(() -> loadSuppliers());
                        }, modalPane);
        });
        contextMenu.getItems().add(editSupplier);
        return contextMenu;
    }
    
    private void handleRowClick(MouseEvent event, TableRow<Supplier> row){
        if(row.isEmpty()) return;
        
        if(event.getButton() == MouseButton.SECONDARY)
            row.getContextMenu().show(row, event.getScreenX(), event.getScreenY());
        else
            row.getContextMenu().hide();
        
        if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY){
            Supplier selectedSupplier = row.getItem();
            ViewUtil.displayModalPaneView("/view/supplier/SupplierProducts.fxml",
                    (SupplierProductsController controller) -> {
                        controller.setSupplier(selectedSupplier);
                    }, modalPane);
        }
    }
    
    private List<String> fetchSupplierCategories(Supplier supplier){
        List<Product> products = supplierService.getSupplierProduct(supplier);
        if(products != null){
            return products.stream()
                    .map(product -> product.getCategory().getName())
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
    
    public void setSupplier(Supplier supplier){
        this.supplier = supplier;
    }
}
