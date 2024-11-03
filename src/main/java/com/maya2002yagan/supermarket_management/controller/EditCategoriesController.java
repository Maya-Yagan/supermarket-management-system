package com.maya2002yagan.supermarket_management.controller;

import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Maya Yagan
 */
public class EditCategoriesController implements Initializable {

    @FXML
    private TableView<Category> categoryTableView;
    @FXML
    private TableColumn<Category, String> nameColumn;
    @FXML
    private TableColumn<Category, Void> deleteColumn;
    @FXML
    private Button closeButton;

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Category> categoryList;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCategories();
        setupNameColumnForEditing();
        setupDeleteColumn();
        closeButton.setOnAction(event -> closeWindow());
    }    
    
    private void loadCategories(){
        categoryList = FXCollections.observableArrayList(categoryDAO.getCategories());
        categoryTableView.setItems(categoryList);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }
    
    private void setupNameColumnForEditing(){
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Category category = event.getRowValue();
            String newName = event.getNewValue();
            
            if(newName != null && !newName.trim().isEmpty()){
                System.out.println("IM HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + newName);
                category.setName(newName.trim());
                categoryDAO.updateCategory(category);
            }
        });
        categoryTableView.setEditable(true);
    }
    
    private void setupDeleteColumn(){
        deleteColumn.setCellFactory(param -> {
            return new TableCell<Category, Void>(){
                private final Button deleteButton = new Button("Delete");
                {
                    deleteButton.setOnAction(event -> {
                        Category category = getTableView().getItems().get(getIndex());
                        showDeleteConfirmation(category);
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
    
    private void showDeleteConfirmation(Category category){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this category?");
        alert.setContentText("This action cannot be undone");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            categoryDAO.deleteCategory(category.getId());
            categoryList.remove(category);
        }
    }
    
    private void closeWindow(){
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
