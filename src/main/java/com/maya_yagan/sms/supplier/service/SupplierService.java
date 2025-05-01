package com.maya_yagan.sms.supplier.service;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.supplier.dao.SupplierDAO;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.common.ValidationService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing supplier-related operations.
 * Author: Rahaf Alaa
 */
public class SupplierService {

    private final ProductService productService = new ProductService();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ValidationService validationService = new ValidationService();

    public Set<Supplier> getAllSuppliers() {
        return supplierDAO.getSuppliers();
    }

    public List<Product> getSupplierProduct(Supplier supplier) {
        return supplierDAO.getSupplierProducts(supplier);
    }
    
    public void deleteSupplier(int id) {
        supplierDAO.deleteSupplier(id);
    }

    public void updateSupplierData(Supplier supplier, String name, String email, String phone) {
        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhoneNumber(phone);
        validationService.validateSupplier(supplier);
        supplierDAO.updateSupplier(supplier);
    }

    public void addSupplier(String name, String email, String phone, Map<Product, Float> products) {
        Supplier supplier = new Supplier(name, email, phone);
        validationService.validateSupplier(supplier);
        Set<SupplierProduct> supplierProducts = products.entrySet().stream()
                .map(e -> {
                    SupplierProduct sp = new SupplierProduct();
                    sp.setSupplier(supplier);
                    sp.setProduct(e.getKey());
                    sp.setPrice(e.getValue());
                    return sp;
                })
                .collect(Collectors.toSet());
        supplier.setSupplierProducts(supplierProducts);
        supplierDAO.insertSupplier(supplier);
    }

    public List<String> fetchSupplierCategories(Supplier supplier){
        List<Product> products = getSupplierProduct(supplier);
        if(products != null){
            return products.stream()
                    .map(product -> product.getCategory().getName())
                    .distinct()
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public void addProductsToSupplier(Supplier supplier, Map<Product, Float> products) {
        if (supplier == null)
            throw new CustomException("No supplier is currently being edited.", "NOT_FOUND");
        if (products.isEmpty())
            throw new CustomException("Please select at least one product to add.", "NOT_FOUND");

        for (var entry : products.entrySet()) {
            Product p = entry.getKey();
            Float rawPrice = entry.getValue();

            float price = validationService.parseAndValidateFloat(
                    rawPrice.toString(),
                    "Price for " + p.getName()
            );

            SupplierProduct existing = supplier.getSupplierProducts().stream()
                    .filter(sp -> sp.getProduct().equals(p))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setPrice(price);              // update existing price
            } else {
                SupplierProduct sp = new SupplierProduct(p, supplier, price);
                supplier.getSupplierProducts().add(sp); // add new mapping
            }
        }

        System.out.println("SupplierProducts in memory: " +
                supplier.getSupplierProducts().size());
        supplier.getSupplierProducts().forEach(sp ->
                System.out.println(" -> " + sp.getProduct().getName()));

        supplierDAO.updateSupplier(supplier);
    }


    public void deleteProductFromSupplier(Supplier supplier, Product product) {
        supplierDAO.deleteProductFromSupplier(supplier, product);
    }

    public List<SupplierProduct> getSupplierProductsByCategory(Supplier supplier, Category category) {
        List<SupplierProduct> products = supplierDAO.getSupplierProductPairs(supplier);
        if (category != null) {
            products = products.stream()
                    .filter(sp -> sp.getProduct().getCategory().equals(category))
                    .collect(Collectors.toList());
        }
        return products;
    }

    public void updateSupplier(Supplier supplier, SupplierProduct sp, double newPrice) {
        validationService.parseAndValidateFloat(String.valueOf(newPrice), "Price");
        Set<SupplierProduct> supplierProducts = supplier.getSupplierProducts();
        SupplierProduct target =
                supplierProducts
                .stream()
                .filter(p -> p.getProduct().equals(sp.getProduct()))
                .findFirst()
                .orElseThrow(() -> new CustomException(
                        "The product is not associated with this supplier",
                        "NOT_FOUND"
                ));
        target.setPrice((float)newPrice);
        supplierDAO.updateSupplier(supplier);
    }

    public SupplierProduct getByProductAndSupplier(Product product, Supplier supplier){
        return supplier.getSupplierProducts()
                .stream()
                .filter(sp -> sp.getProduct().equals(product))
                .findFirst()
                .orElse(null);
    }

    public Collection<SupplierProduct> getSupplierProductsByCategoryAndSupplier(
            String category, Supplier supplier){
        if(supplier == null) return Collections.emptyList();
        Set<Product> products = productService.getFilteredProductsByCategory(category);
        return products.stream()
                .map(product -> getByProductAndSupplier(product, supplier))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}