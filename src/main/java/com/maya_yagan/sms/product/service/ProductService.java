package com.maya_yagan.sms.product.service;

import com.maya_yagan.sms.product.dao.CategoryDAO;
import com.maya_yagan.sms.product.dao.ProductDAO;
import com.maya_yagan.sms.product.dao.MoneyUnitDAO;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.MoneyUnit;
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
    private final MoneyUnitDAO moneyUnitDAO = new MoneyUnitDAO();
    private final ValidationService validationService = new ValidationService();


    public Set<Product> getAllProducts(){
        return productDAO.getProducts();
    }

    public Set<Category> getAllCategories(){
        return categoryDAO.getCategories();
    }

    public Set<Product> getProductsByCategory(Category category){
        return productDAO.getProductsByCategory(category);
    }

    public Category getCategoryByName(String name){
        return categoryDAO.getCategoryByName(name);
    }

    public void updateProduct(Product product){
        productDAO.updateProduct(product);
    }

    public void updateCategory(Category category){
        categoryDAO.updateCategory(category);
    }

    public Set<ProductUnit> getProductUnits(){
        return new HashSet<>(Arrays.asList(ProductUnit.values()));
    }

    public void deleteProduct(int id){
        productDAO.deleteProduct(id);
    }

    public void deleteCategory(int id){
        categoryDAO.deleteCategory(id);
    }

    public Set<Product> getFilteredProductsByCategory(String categoryName){
        if ("All Categories".equals(categoryName))
            return getAllProducts();

        Category category = getCategoryByName(categoryName);
        return (category != null) ? getProductsByCategory(category) : Set.of();
    }

    public String formatExpirationDate(Product product){
        return (product.getExpirationDate() == null) ?
                "No Expiry Date" :
                product.getExpirationDate().toString();
    }

    public boolean addProduct(String name, String priceText, String discountText,LocalDate productionDate,
                              LocalDate expirationDate, Category category, ProductUnit unit , String barcode){
        float price = validationService.parseAndValidateFloat(priceText, "price");
        float discount = validationService.parseDiscount(discountText);
        Product product = new Product(name, price, productionDate, expirationDate, category, unit, discount, barcode);
        validationService.validateProduct(product);
        return productDAO.insertProduct(product);
    }

    public boolean addCategory(String name){
        Category category = new Category();
        category.setName(name);
        validationService.validateCategory(category);
        return categoryDAO.insertCategory(category);
    }

    public void updateProductData(Product product, String name, String priceText,String discountText, LocalDate productionDate,
                                  LocalDate expirationDate, Category category, ProductUnit unit, String barcode){
        product.setName(name);
        product.setCategory(category);
        product.setPrice(validationService.parseAndValidateFloat(priceText, "price"));
        product.setDiscount(validationService.parseDiscount(discountText));        product.setProductionDate(productionDate);
        product.setExpirationDate(expirationDate);
        product.setUnit(unit);
        product.setBarcode(barcode);
        validationService.validateProduct(product);
        updateProduct(product);
    }

    public void updateCategoryData(Category category, String name){
        category.setName(name);
        validationService.validateCategory(category);
        updateCategory(category);
    }

    public void addMoneyUnit(String code, String name, String symbol) {
        moneyUnitDAO.deleteAllMoneyUnits();
        MoneyUnit moneyUnit = new MoneyUnit(code, name, symbol);
        moneyUnitDAO.insertMoneyUnit(moneyUnit);
    }

    public MoneyUnit getMoneyUnitByCode(String code) {
        return moneyUnitDAO.getMoneyUnitByCode(code);
    }

    public Set<MoneyUnit> getAllMoneyUnits() {
        return moneyUnitDAO.getAllMoneyUnits();
    }



}