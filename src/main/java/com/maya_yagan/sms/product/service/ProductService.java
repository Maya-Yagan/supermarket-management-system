package com.maya_yagan.sms.product.service;

import com.maya_yagan.sms.product.dao.CategoryDAO;
import com.maya_yagan.sms.product.dao.ProductDAO;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.model.ProductUnit;
import com.maya_yagan.sms.common.ValidationService;

import java.time.LocalDate;
import java.util.*;

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
                              LocalDate expirationDate, Category category, ProductUnit unit , String barcode,
                              String minStockLimit, String tax){
        float price = validationService.parseAndValidateFloat(priceText, "price");
        float discount = validationService.parseAndValidateFloat(discountText, "Discount");
        Product product = new Product(name, price, productionDate, expirationDate, category, unit, discount, barcode);
        int minLimit = validationService.parseAndValidateInt(minStockLimit, "Min Stock Limit");
        product.setMinLimit(minLimit);
        float taxPercentage = validationService.parseAndValidateFloat(tax, "Tax");
        product.setTaxPercentage(taxPercentage);
        validationService.validateProduct(product);
        return productDAO.insertProduct(product);
    }

    public boolean addCategory(String name){
        Category category = new Category();
        category.setName(name);
        validationService.validateCategory(category);
        return categoryDAO.insertCategory(category);
    }

    public void updateProductData(Product product, String name, String priceText,
                                  String discountText, LocalDate productionDate,
                                  LocalDate expirationDate, Category category,
                                  ProductUnit unit, String barcode, String minLimit, String tax){
        product.setName(name);
        product.setCategory(category);
        product.setPrice(validationService.parseAndValidateFloat(priceText, "price"));
        product.setDiscount(validationService.parseAndValidateFloat(discountText, "Discount"));
        product.setProductionDate(productionDate);
        product.setExpirationDate(expirationDate);
        product.setUnit(unit);
        product.setBarcode(barcode);
        float taxPercentage = validationService.parseAndValidateFloat(tax, "Tax");
        product.setTaxPercentage(taxPercentage);
        int min = validationService.parseAndValidateInt(minLimit, "Min Stock Limit");
        product.setMinLimit(min);
        validationService.validateProduct(product);
        updateProduct(product);
    }

    public void updateCategoryData(Category category, String name){
        category.setName(name);
        validationService.validateCategory(category);
        updateCategory(category);
    }

    public List<String> getProductNames(){
        var products = getAllProducts();
        return products.stream().map(Product::getName).toList();
    }

    public float calculateDiscountedPrice(Product product){
        float price = product.getPrice();
        float discountPercent = product.getDiscount();
        return price * (1 - discountPercent / 100f);
    }

    public Optional<Product> findProductByName(String name){
        return getAllProducts()
                .stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<Product> findProductByBarcode(String barcode){
        return Optional.ofNullable(productDAO.getProductByBarcode(barcode));
    }
}