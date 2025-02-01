package com.maya2002yagan.supermarket_management.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.maya2002yagan.supermarket_management.dao.CategoryDAO;
import com.maya2002yagan.supermarket_management.model.Category;
import com.maya2002yagan.supermarket_management.util.ShowAlert;
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
 * FXML Controller class for managing and editing categories in the supermarket management system.
 *
 * This controller handles the user interface for the category management screen. It provides functionality 
 * for displaying a list of categories in a table, editing category names, and deleting categories. The categories 
 * are managed through interactions with the `CategoryDAO` for data persistence. 
 * Users can modify category names directly in the table, and a delete option is provided for each category.
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

    private Runnable onCloseAction;
    private ModalPane modalPane;
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private ObservableList<Category> categoryList;
    
    /**
     * Initializes the controller class.
     *
     * This method sets up the category table, enables editing for the name column,
     * and configures the delete column with a button to delete categories.
     * The method also handles the close button action and applies styles to the table.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCategories();
        setupNameColumnForEditing();
        setupDeleteColumn();
        closeButton.setOnAction(event -> closeWindow());
        categoryTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        categoryTableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
    }    
    
    /**
     * Sets the modal pane for the controller.
     *
     * @param modalPane The modal pane to be set.
     */
    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
    
    /**
     * Sets the action to be executed when the window is closed.
     *
     * @param onCloseAction The action to be executed when the window is closed.
     */
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
    
    /**
     * Loads the categories from the database and sets them in the table view.
     *
     * This method retrieves the list of categories from the `CategoryDAO` and sets the list in the category table. 
     * The name column is bound to the category name property.
     */
    private void loadCategories(){
        categoryList = FXCollections.observableArrayList(categoryDAO.getCategories());
        categoryTableView.setItems(categoryList);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    }
    
    /**
     * Configures the name column to be editable and updates the category when the name is changed.
     *
     * This method enables inline editing for the category name in the table. When a name is edited, it is 
     * updated in the `Category` object and persisted in the database.
     */
    private void setupNameColumnForEditing(){
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Category category = event.getRowValue();
            String newName = event.getNewValue();
            
            if(newName != null && !newName.trim().isEmpty()){
                category.setName(newName.trim());
                categoryDAO.updateCategory(category);
            }
        });
        categoryTableView.setEditable(true);
    }
    
    /**
     * Configures the delete column with a delete button to remove categories.
     *
     * This method sets up a delete button for each row in the table. When clicked, the button triggers a 
     * confirmation dialog asking if the user is sure they want to delete the category.
     */
    private void setupDeleteColumn(){
        deleteColumn.setCellFactory(param -> {
            return new TableCell<Category, Void>(){
                private final Button deleteButton = new Button("Delete");
                {
                    deleteButton.getStyleClass().add(Styles.DANGER);
                    deleteButton.setOnAction(event -> {
                        Category category = getTableView().getItems().get(getIndex());
                        ShowAlert.showDeleteConfirmation(category,
                                "Delete Confirmation",
                                "Are you sure you want to delete this category?",
                                "This action cannot be undone",
                                (Category c) -> {
                                    categoryDAO.deleteCategory(c.getId());
                                    categoryList.remove(c);
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
     * Closes the window and executes the close action.
     *
     * This method is invoked when the close button is pressed.
     * It runs the specified `onCloseAction` and hides the modal pane.
     */
    private void closeWindow(){
        if(onCloseAction != null) onCloseAction.run();
        if(modalPane != null) modalPane.hide();
    }
}
