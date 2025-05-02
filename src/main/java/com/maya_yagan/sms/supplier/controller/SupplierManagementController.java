package com.maya_yagan.sms.supplier.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.service.SupplierService;
import com.maya_yagan.sms.util.ViewUtil;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.ContextMenuUtil;
import java.util.Collection;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

/**
 * FXML Controller class for managing suppliers in the supermarket management system.
 *
 * @author Maya Yagan
 */
public class SupplierManagementController extends AbstractTableController<Supplier> {

    @FXML private TableColumn<Supplier, Integer> idColumn;
    @FXML TableColumn<Supplier, String> nameColumn, emailColumn, phoneNumberColumn, categoryColumn;
    @FXML private Button makeOrderButton, addSupplierButton;
    @FXML private StackPane stackPane;

    private ModalPane modalPane;
    private final SupplierService supplierService = new SupplierService();
    
    @Override
    protected void configureColumns(){
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        categoryColumn.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Supplier supplier = getTableRow().getItem();
                    List<String> categories = supplierService.fetchSupplierCategories(supplier);
                    if (categories.isEmpty()) {
                        setGraphic(null);
                        setText("No Category");
                    } else if (categories.size() == 1) {
                        setGraphic(null);
                        setText(categories.get(0));
                    } else {
                        comboBox.setItems(FXCollections.observableArrayList(categories));
                        comboBox.getSelectionModel().select(0);
                        setGraphic(comboBox);
                        setText(null);
                    }
                }
            }
        });
    }

    @Override
    protected Collection<Supplier> fetchData(){
        return supplierService.getAllSuppliers();
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<Supplier>> menuItemsFor(Supplier supplier){
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("Delete Supplier", (item, row) -> handleDeleteAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Edit Supplier", (item, row) -> handleEditAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("View Details", (item, row) -> handleDetailsAction(item))
        );
    }

    @Override
    protected void postInit(){
        modalPane = ViewUtil.initializeModalPane(stackPane);
        setupEventHandlers();
    }
    
    private void setupEventHandlers(){
        addSupplierButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/supplier/AddSupplier.fxml",
                (AddSupplierController controller) -> {
                    controller.setModalPane(modalPane);
                    controller.setOnCloseAction(this::refresh);
                }, modalPane));

        makeOrderButton.setOnAction(event -> ViewUtil.displayModalPaneView("/view/order/AddOrder.fxml",
                controller -> {}, modalPane));
    }

    private void handleDetailsAction(Supplier supplier) {
        ViewUtil.displayModalPaneView("/view/supplier/SupplierProducts.fxml",
            (SupplierProductsController controller) -> {
            controller.setSupplier(supplier);
            controller.setOnCloseAction(this::refresh);
        }, modalPane);
    }

    private void handleEditAction(Supplier supplier) {
        ViewUtil.displayModalPaneView("/view/supplier/EditSupplier.fxml",
            (EditSupplierController controller) -> {
            controller.setSupplier(supplier);
            controller.setModalPane(modalPane);
            controller.setOnCloseAction(this::refresh);
        }, modalPane);
    }

    private void handleDeleteAction(Supplier supplier) {
        AlertUtil.showDeleteConfirmation(supplier,
                "Delete Supplier",
                "Are you sure you want to delete this Supplier?",
                "This action cannot be undone",
                (p) -> {
                    supplierService.deleteSupplier(p.getId());
                    refresh();
                }
        );
    }
}
