package com.maya_yagan.sms.product.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.util.AlertUtil;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

/**
 * FXML Controller class for editing categories in the supermarket management system.
 *
 * @author Maya Yagan
 */
public class EditCategoriesController implements Initializable {

    @FXML private TableView<Category> categoryTableView;
    @FXML private TableColumn<Category, String> nameColumn;
    @FXML private TableColumn<Category, Void> deleteColumn;

    private Runnable onCloseAction;
    private ModalPane modalPane;
    private final ObservableList<Category> categoryObservableList = FXCollections.observableArrayList();
    private final ProductService productService = new ProductService();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCategories();
        setupNameColumnForEditing();
        setupDeleteColumn();
        categoryTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        categoryTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
    }    
    
    private void loadCategories(){
        categoryObservableList.setAll(productService.getAllCategories());
        categoryTableView.setItems(categoryObservableList);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }
    
    private void setupNameColumnForEditing(){
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Category category = event.getRowValue();
            String newName = event.getNewValue();
            productService.updateCategoryData(category, newName);
        });
        categoryTableView.setEditable(true);
    }
    
    private void setupDeleteColumn(){
        deleteColumn.setCellFactory(param -> {
            return new TableCell<Category, Void>(){
                private final Button deleteButton = new Button("Delete");
                {
                    deleteButton.getStyleClass().add(Styles.DANGER);
                    deleteButton.setOnAction(event -> {
                        Category category = getTableView().getItems().get(getIndex());
                        AlertUtil.showDeleteConfirmation(category,
                                "Delete Confirmation",
                            "Are you sure you want to delete this category?",
                            "This action cannot be undone",
                                (Category c) -> {
                                    productService.deleteCategory(c.getId());
                                    categoryObservableList.remove(c);
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
    
    @FXML
    private void close(){
        if(onCloseAction != null) onCloseAction.run();
        if(modalPane != null) modalPane.hide();
    }
    
    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
    
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
}
