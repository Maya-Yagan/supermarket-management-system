package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.maya2002yagan.supermarket_management.dao.SupplierDAO;
import com.maya2002yagan.supermarket_management.model.Product;
import com.maya2002yagan.supermarket_management.model.Supplier;
import com.maya2002yagan.supermarket_management.util.FormHelper;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * FXML Controller class
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
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configureTableColumns();
        setupDeleteColumn();
        loadSuppliers();
        setupEventHandlers();
        initializeModalPane();
        // Define behavior for double-clicking a row
        suppliersTable.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && (!row.isEmpty())){
                    Supplier selectedSupplier = row.getItem();
                    FormHelper.openForm("/fxml/SupplierProducts.fxml",
                            (SupplierProductsController controller) -> {
                                controller.setSupplier(selectedSupplier);
                            }, modalPane);
                }
            });
            return row;
        });
    }    
    
    private void configureTableColumns(){
        suppliersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        suppliersTable.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        //i need to find a way to display the categories of the supplier in a drop down in the column Category
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
                    //comboBox.setItems(FXCollections.observableArrayList(categories));
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
        supplierObservableList.addAll(supplierDAO.getSuppliers());
        suppliersTable.setItems(supplierObservableList);
    }
    
    private void setupEventHandlers(){
        addSupplierButton.setOnAction(event -> FormHelper.openForm("/fxml/AddSupplier.fxml",
                (AddSupplierController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(() -> loadSuppliers());
                }, modalPane));
        //makeOrderButton.setOnAction(event -> openForm("/fxml/MakeOrder.fxml"));
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
    
    private void setupDeleteColumn(){
        deleteColumn.setCellFactory(column -> {
            return new TableCell<Supplier, Void>(){
                private final Button deleteButton = new Button(null, new FontIcon(Feather.TRASH));
                {
                    deleteButton.getStyleClass().add(Styles.DANGER);
                    deleteButton.setOnAction(event -> {
                        Supplier supplier = getTableView().getItems().get(getIndex());
                        ShowAlert.showDeleteConfirmation(supplier,
                                "Delete Confirmation",
                                "Are you sure you want to delete this supplier?",
                                "This action cannot be undone",
                                (Supplier s) -> {
                                    supplierDAO.deleteSupplier(s.getId());
                                    supplierObservableList.remove(supplier);
                                });
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
