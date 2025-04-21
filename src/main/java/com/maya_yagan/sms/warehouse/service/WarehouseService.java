package com.maya_yagan.sms.warehouse.service;

import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.util.ValidationService;
import com.maya_yagan.sms.warehouse.dao.WarehouseDAO;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WarehouseService {
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final ValidationService validationService = new ValidationService();
    private final ProductService productService = new ProductService();

    public List<Warehouse> getAllWarehouses() { return warehouseDAO.getWarehouses(); }

    public Set<Product> getFilteredWarehouseProducts(Warehouse warehouse, String categoryName){
        Set<Product> categoryFilteredProducts = productService.getFilteredProductsByCategory(categoryName);

        return warehouseDAO.getProductWithWarehouse(warehouse)
                .stream()
                .filter(categoryFilteredProducts::contains)
                .collect(Collectors.toSet());
    }

    public List<ProductWarehouse> getProductWarehousesByCategory(Warehouse warehouse, String categoryName) {
        Set<ProductWarehouse> all = warehouse.getProductWarehouses();
        Set<Integer> filteredIds = getFilteredWarehouseProducts(warehouse, categoryName)
                .stream()
                .map(Product::getId)
                .collect(Collectors.toSet());
        return all.stream()
                .filter(pw -> filteredIds.contains(pw.getProduct().getId()))
                .toList();
    }


    public void deleteWarehouse(int id){
        warehouseDAO.deleteWarehouse(id);
    }

    public void deleteProductFromWarehouse(Warehouse warehouse, Product product){
        warehouseDAO.deleteProductFromWarehouse(warehouse, product);
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

    public void updateProductStock(Warehouse warehouse, ProductWarehouse productWarehouse, int newAmount){
        int totalBefore = calculateTotalProducts(warehouse) - productWarehouse.getAmount();
        int remainingCapacity = warehouse.getCapacity() - totalBefore;
        validationService.validateStockAmount(newAmount, remainingCapacity);
        productWarehouse.setAmount(newAmount);
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
