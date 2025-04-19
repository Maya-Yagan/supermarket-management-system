package com.maya_yagan.sms.warehouse.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.ContextMenuUtil;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.util.ViewUtil;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import com.maya_yagan.sms.warehouse.service.WarehouseService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

/**
 * Controller class for managing warehouses in the application.
 * 
 * @author Maya Yagan
 */
public class WarehouseManagementController implements Initializable {

    @FXML private TableView<Warehouse> warehouseTableView;
    @FXML private TableColumn<Warehouse, String> nameColumn;
    @FXML private TableColumn<Warehouse, Integer> capacityColumn;
    @FXML private TableColumn<Warehouse, Integer> totalProductsColumn;
    @FXML private StackPane stackPane;

    private ModalPane modalPane;
    private final WarehouseService warehouseService = new WarehouseService();
    private final ObservableList<Warehouse> warehouseObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        setupProductTableContextMenu();
        refreshTable();
        modalPane = ViewUtil.initializeModalPane(stackPane);
        warehouseTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        warehouseTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
    }

    private void refreshTable(){
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        warehouseObservableList.setAll(warehouses);
    }

    private void configureTableColumns(){
        warehouseTableView.setItems(warehouseObservableList);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        totalProductsColumn.setCellValueFactory(data -> {
            Warehouse warehouse = data.getValue();
            int total = warehouseService.calculateTotalProducts(warehouse);
            return new ReadOnlyObjectWrapper<>(total);
        });
    }

    private void setupProductTableContextMenu() {
        warehouseTableView.setRowFactory(tv -> {
            TableRow<Warehouse> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldVal, currentWarehouse) -> {
                if(currentWarehouse != null){
                    List<ContextMenuUtil.MenuItemConfig<Warehouse>> menuItems = new ArrayList<>();

                    menuItems.add(new ContextMenuUtil.MenuItemConfig<>(
                            "View Products",
                            (warehouse, r) ->
                                    ViewUtil.displayView("/view/warehouse/WarehouseProducts.fxml",
                                            (WarehouseProductsController controller) ->
                                                    controller.setWarehouse(warehouse),
                                            modalPane)
                    ));

                    menuItems.add(new ContextMenuUtil.MenuItemConfig<>(
                            "Edit Warehouse",
                            (warehouse, r) ->
                                    ViewUtil.displayView("/view/warehouse/EditWarehouse.fxml",
                                    (EditWarehouseController controller) -> {
                                        controller.setModalPane(modalPane);
                                        controller.setWarehouse(warehouse);
                                        controller.setOnCloseAction(this::refreshTable);
                                    }, modalPane)
                    ));

                    menuItems.add(new ContextMenuUtil.MenuItemConfig<>(
                            "Delete Warehouse",
                            (warehouse, r) -> {
                                AlertUtil.showDeleteConfirmation(warehouse,
                                        "Delete Warehouse",
                                        "Are you sure you want to delete this warehouse?",
                                        "This action cannot be undone",
                                        (w) -> {
                                            warehouseService.deleteWarehouse(w.getId());
                                            refreshTable();
                                        }
                                );
                            }
                    ));

                    ContextMenu contextMenu = ContextMenuUtil.createContextMenu(row, currentWarehouse, menuItems);
                    row.setContextMenu(contextMenu);
                } else {
                    row.setContextMenu(null);
                }
            });
            return row;
        });
    }

    @FXML
    private void addWarehouse(){
        ViewUtil.displayView("/view/warehouse/AddWarehouse.fxml", 
            (AddWarehouseController controller) -> {
                controller.setModalPane(modalPane);
                controller.setOnCloseAction(this::refreshTable);
            }, modalPane);
    }
}
