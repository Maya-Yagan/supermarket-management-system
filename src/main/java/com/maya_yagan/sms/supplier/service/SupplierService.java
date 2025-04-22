
package com.maya_yagan.sms.supplier.service;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.supplier.dao.SupplierDAO;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.util.ValidationService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Rahaf Alaa
 */
public class SupplierService {

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

    public void addProductsToSupplier(){
        //supplier.setSupplierProducts();
    }
}