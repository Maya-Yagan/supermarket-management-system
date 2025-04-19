package com.maya_yagan.sms.warehouse.service;

import com.maya_yagan.sms.util.ValidationService;
import com.maya_yagan.sms.warehouse.dao.WarehouseDAO;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.warehouse.model.Warehouse;

import java.util.List;

public class WarehouseService {
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final ValidationService validationService = new ValidationService();

    public List<Warehouse> getAllWarehouses() { return warehouseDAO.getWarehouses(); }

    public void deleteWarehouse(int id){
        warehouseDAO.deleteWarehouse(id);
    }

    public int calculateTotalProducts(Warehouse warehouse){
        if(warehouse.getProductWarehouses() == null) return 0;
        return warehouse.getProductWarehouses()
                .stream()
                .mapToInt(ProductWarehouse::getAmount)
                .sum();
    }

    public void updateWarehouse(Warehouse warehouse, String name, String capacity){
        int newCapacity = validationService.parseAndValidateInt(capacity, "capacity");
        int totalProducts = calculateTotalProducts(warehouse);
        warehouse.setName(name);
        warehouse.setCapacity(newCapacity);
        validationService.validateWarehouse(name, newCapacity, totalProducts);
        warehouseDAO.updateWarehouse(warehouse);
    }

    public void addWarehouse(String name, String capacity){
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacity(validationService.parseAndValidateInt(capacity, "capacity"));
        warehouse.setName(name);
        validationService.validateWarehouse(name, warehouse.getCapacity());
        warehouseDAO.insertWarehouse(warehouse);
    }
}
