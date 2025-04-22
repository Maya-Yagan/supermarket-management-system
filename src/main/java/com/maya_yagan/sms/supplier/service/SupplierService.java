package com.maya_yagan.sms.supplier.service;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.model.Category;
import com.maya_yagan.sms.supplier.dao.SupplierDAO;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.util.ValidationService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing supplier-related operations.
 * 
 * Author: Rahaf Alaa
 */
public class SupplierService {

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ValidationService validationService = new ValidationService();

    public Set<Supplier> getAllSuppliers() {
        return supplierDAO.getSuppliers();
    }

    public Supplier getSupplierById(int id) {
        return supplierDAO.getSupplierById(id);
    }

    public List<Product> getSupplierProduct(Supplier supplier) {
        return supplierDAO.getSupplierProducts(supplier);
    }

    public void deleteSupplier(int id) {
        supplierDAO.deleteSupplier(id);
    }

    public void updateSupplier(Supplier supplier) {
        supplierDAO.updateSupplier(supplier);
    }

    public void updateSupplierData(Supplier supplier, String name, String email, String phone) {
        supplier.setName(name);
        supplier.setEmail(email);
        supplier.setPhoneNumber(phone);
        validationService.validateSupplier(supplier);
        updateSupplier(supplier);
    }

    public void addSupplier(String name, String email, String phone) {
        Supplier supplier = new Supplier(name, email, phone);
        validationService.validateSupplier(supplier);
        supplierDAO.insertSupplier(supplier);
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

    public void updateSupplierProduct(SupplierProduct sp) {
        // Replace the old entry with the updated one and call updateSupplier
        Supplier supplier = sp.getSupplier();
        // Remove the old entry for the same product if exists
        supplier.getSupplierProducts().removeIf(existing -> existing.getProduct().equals(sp.getProduct()));
        // Add the updated one
        supplier.getSupplierProducts().add(sp);
        // Persist the changes using the existing DAO
        supplierDAO.updateSupplier(supplier);
    }
}
