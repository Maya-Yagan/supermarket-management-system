package com.maya_yagan.sms.product.service;

import com.maya_yagan.sms.product.dao.CategoryDAO;
import com.maya_yagan.sms.product.dao.ProductDAO;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.model.ProductUnit;
import com.maya_yagan.sms.util.ValidationService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Maya Yagan
 */
public class ProductService {
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ValidationService validationService = new ValidationService();
    
    public Set<Product> getAllProducts(){
        return productDAO.getProducts();
    }
   
    public Set<Product> getProductsByCategory(Category category){
        return productDAO.getProductsByCategory(category);
    }
    
    public Set<Category> getAllCategories(){
        return categoryDAO.getCategories();
    }
    
    public Category getCategoryByName(String name){
        return categoryDAO.getCategoryByName(name);
    }
    
    public Set<ProductUnit> getProductUnits(){
        return new HashSet<>(Arrays.asList(ProductUnit.values()));
    } 
    
    public Set<Product> getFilteredProductsByCategory(String categoryName){
        if ("All Categories".equals(categoryName))
            return getAllProducts();
        
        Category category = getCategoryByName(categoryName);
        return (category != null) ? getProductsByCategory(category) : Set.of();
    }
    
    public String formatExpirationDate(Product product){
            return (product.getExpirationDate() == null) ?
                    "No Expiray Date" : 
                    product.getExpirationDate().toString();
    }
    
    public boolean addProduct(String name, String priceText, LocalDate productionDate,
                           LocalDate expirationDate, Category category, ProductUnit unit){
        float price = validationService.parseAndValidateFloat(priceText, "price");
        Product product = new Product(name, price, productionDate, expirationDate, category, unit);
        validationService.validateProduct(product);
        return productDAO.insertProduct(product);
    }
    
    public boolean addCategory(String name){
        Category category = new Category();
        category.setName(name);
        validationService.validateCategory(category);
        return categoryDAO.insertCategory(category);
    }
}
