package com.maya_yagan.sms.warehouse.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.warehouse.dao.WarehouseDAO;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.util.AlertUtil;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

/**
 * Controller class for editing warehouse information.
 *
 * This class provides functionality to edit, delete, and view warehouses. 
 * Users can update warehouse names and capacities directly in the table, 
 * and also delete warehouses using a delete button.
 * 
 * @author Maya Yagan
 */
public class EditWarehouseController implements Initializable {
    
    @FXML
    private TableView<Warehouse> warehouseTableView;
    @FXML
    private TableColumn<Warehouse, String> nameColumn;
    @FXML
    private TableColumn<Warehouse, Integer> capacityColumn;
    @FXML
    private TableColumn<Warehouse, Void> deleteColumn;
    @FXML
    private Button closeButton;
    
    private Runnable onCloseAction;
    private ModalPane modalPane;
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private ObservableList<Warehouse> warehouseList;

    /**
     * Initializes the controller.
     *
     * Configures the table columns for editing and deleting warehouse entries, 
     * loads warehouse data into the table, and sets up the close button action.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadWarehouses();
        setupColumnsForEditing();
        setupDeleteColumn();
        closeButton.setOnAction(event -> closeWindow());
        warehouseTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        warehouseTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
    }   
    
    /**
     * Sets the modal pane for this controller.
     *
     * @param modalPane The modal pane to display.
     */
    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
 
    /**
     * Sets the action to perform when the close button is clicked.
     *
     * @param onCloseAction A runnable task to execute on close.
     */
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
    
    /**
     * Loads warehouse data from the database and displays it in the table view.
     */
    private void loadWarehouses(){
        warehouseList = FXCollections.observableArrayList(warehouseDAO.getWarehouses());
        warehouseTableView.setItems(warehouseList);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
    }
    
    /**
     * Configures the table columns for editing warehouse names and capacities.
     *
     * Allows users to edit the warehouse name or capacity directly in the table.
     * Validates the input and updates the database if changes are valid.
     */
    private void setupColumnsForEditing(){
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Warehouse warehouse = event.getRowValue();
            String newName = event.getNewValue();
            if(newName != null && !newName.trim().isEmpty()){
                warehouse.setName(newName.trim());
                warehouseDAO.updateWarehouse(warehouse);
                warehouseTableView.refresh();
            }
        });
        
        capacityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : object.toString();
            }

            @Override
            public Integer fromString(String string) {
                try {
                    return Integer.parseInt(string);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }));
        capacityColumn.setOnEditCommit(event -> {
            Warehouse warehouse = event.getRowValue();
            Integer newCapacity = event.getNewValue();
            if(newCapacity != null && newCapacity >= 0){
                warehouse.setCapacity(newCapacity);
                warehouseDAO.updateWarehouse(warehouse);
            }
            else{
                AlertUtil.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Invalid capacity. Enter a valid number.");
                warehouseTableView.refresh();
            }
        });
        warehouseTableView.setEditable(true);
    }

    /**
     * Configures the delete column with buttons for deleting warehouse entries.
     *
     * Adds a "Delete" button to each row, allowing users to remove warehouses 
     * from the database and the table view after confirmation.
     */
    private void setupDeleteColumn(){
        deleteColumn.setCellFactory(param -> {
            return new TableCell<Warehouse, Void>(){
                private final Button deleteButton = new Button("Delete");
                {
                    deleteButton.getStyleClass().add(Styles.DANGER);
                    deleteButton.setOnAction(event -> {
                        Warehouse warehouse = getTableView().getItems().get(getIndex());
                        AlertUtil.showDeleteConfirmation(warehouse,
                                "Delete Warehouse",
                                "Are you sure you want to delete this warehouse?",
                                "This action cannot be undone",
                                (Warehouse w) -> {
                                    warehouseDAO.deleteWarehouse(w.getId());
                                    warehouseList.remove(w);
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
     * Closes the current window or hides the modal pane.
     *
     * Executes the onClose action if provided and hides the modal pane if it is set.
     */
    private void closeWindow(){
        if(onCloseAction != null) onCloseAction.run();
        if(modalPane != null) modalPane.hide();
    }
}