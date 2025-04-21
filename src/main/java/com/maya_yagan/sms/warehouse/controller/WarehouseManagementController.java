package com.maya_yagan.sms.warehouse.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.common.AbstractTableController;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.ContextMenuUtil;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.util.ViewUtil;
import java.util.Collection;
import java.util.List;
import com.maya_yagan.sms.warehouse.service.WarehouseService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

/**
 * Controller class for managing warehouses in the application.
 * 
 * @author Maya Yagan
 */
public class WarehouseManagementController extends AbstractTableController<Warehouse> {

    @FXML private TableColumn<Warehouse, String> nameColumn;
    @FXML private TableColumn<Warehouse, Integer> capacityColumn;
    @FXML private TableColumn<Warehouse, Integer> totalProductsColumn;
    @FXML private StackPane stackPane;

    private ModalPane modalPane;
    private final WarehouseService warehouseService = new WarehouseService();

    @Override
    protected void configureColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        totalProductsColumn.setCellValueFactory(data -> {
            Warehouse warehouse = data.getValue();
            int total = warehouseService.calculateTotalProducts(warehouse);
            return new ReadOnlyObjectWrapper<>(total);
        });
    }

    @Override
    protected Collection<Warehouse> fetchData() {
        return warehouseService.getAllWarehouses();
    }

    @Override
    protected void postInit() {
        modalPane = ViewUtil.initializeModalPane(stackPane);
    }

    @Override
    protected List<ContextMenuUtil.MenuItemConfig<Warehouse>> menuItemsFor(Warehouse w) {
        return List.of(
                new ContextMenuUtil.MenuItemConfig<>("View Products", (item, row) -> handleViewAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Edit Warehouse", (item, row) -> handleEditAction(item)),
                new ContextMenuUtil.MenuItemConfig<>("Delete Warehouse", (item,  row) -> handleDeleteAction(item))
        );
    }

    private void handleDeleteAction(Warehouse warehouse) {
        AlertUtil.showDeleteConfirmation(warehouse,
                "Delete Warehouse",
                "Are you sure you want to delete this warehouse?",
                "This action cannot be undone",
                (w) -> {
                    warehouseService.deleteWarehouse(w.getId());
                    refresh();
                }
        );
    }

    private void handleEditAction(Warehouse warehouse) {
        ViewUtil.displayModalPaneView("/view/warehouse/EditWarehouse.fxml",
        (EditWarehouseController controller) -> {
            controller.setModalPane(modalPane);
            controller.setWarehouse(warehouse);
            controller.setOnCloseAction(this::refresh);
        }, modalPane);
    }

    private void handleViewAction(Warehouse warehouse) {
        ViewUtil.displayModalPaneView("/view/warehouse/WarehouseProducts.fxml",
                (WarehouseProductsController controller) ->
                        controller.setWarehouse(warehouse),
                modalPane);
    }

    @FXML
    private void addWarehouse(){
        ViewUtil.displayModalPaneView("/view/warehouse/AddWarehouse.fxml",
            (AddWarehouseController controller) -> {
                controller.setModalPane(modalPane);
                controller.setOnCloseAction(this::refresh);
            }, modalPane);
    }
}
