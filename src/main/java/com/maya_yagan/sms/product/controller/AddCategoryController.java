package com.maya_yagan.sms.product.controller;

import atlantafx.base.controls.ModalPane;
import com.maya_yagan.sms.product.dao.CategoryDAO;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.util.AlertUtil;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ExceptionHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * FXML Controller class for handling the logic of adding a new category in the application.
 * 
 * @author Maya Yagan
 */
public class AddCategoryController implements Initializable {

    @FXML private TextField categoryName;
    
    private Runnable onCloseAction;
    private ModalPane modalPane;
    private final ProductService productService = new ProductService();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }  
    
    public void setModalPane(ModalPane modalPane){
        this.modalPane = modalPane;
    }
    
    public void setOnCloseAction(Runnable onCloseAction){
        this.onCloseAction = onCloseAction;
    }
    
    @FXML
    private void saveCategory(){
        try{
            if(productService.addCategory(categoryName.getText())){
                if(onCloseAction != null) onCloseAction.run();
                close();
            } 
        } catch(CustomException e){
            ExceptionHandler.handleException(e);
        }
    }
    
    @FXML
    private void close(){
        if(modalPane != null) modalPane.hide();
    }
}
