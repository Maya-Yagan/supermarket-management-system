
package com.maya_yagan.sms.supplier.service;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.supplier.dao.SupplierDAO;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.util.ValidationService;
import java.util.List;
import java.util.Set;

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
}