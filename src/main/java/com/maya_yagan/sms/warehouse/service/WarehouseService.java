package com.maya_yagan.sms.warehouse.service;

import com.maya_yagan.sms.homepage.service.HomePageService;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.order.model.OrderProduct;
import com.maya_yagan.sms.payment.model.Receipt;
import com.maya_yagan.sms.payment.model.ReceiptItem;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.product.service.ProductService;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.common.ValidationService;
import com.maya_yagan.sms.warehouse.dao.WarehouseDAO;
import com.maya_yagan.sms.warehouse.model.ProductWarehouse;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WarehouseService {
    private final WarehouseDAO warehouseDAO = new WarehouseDAO();
    private final ValidationService validationService = new ValidationService();
    private final ProductService productService = new ProductService();
    private final HomePageService homePageService = new HomePageService();

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
        String msg = String.format(
                "%s is out of stock in %s. Make an order ASAP!",
                product.getName(), warehouse.getName());
        homePageService.notify(msg, true, true);
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

    public void updateProductStock(Warehouse warehouse, ProductWarehouse productWarehouse, int newTotalForProduct){
        int currentQty = productWarehouse.getAmount();
        int delta      = newTotalForProduct - currentQty;
        int freeSpace = warehouse.getCapacity() - calculateTotalProducts(warehouse);

        if (delta > 0)
            validationService.validateStockAmount(delta, freeSpace);

        productWarehouse.setAmount(newTotalForProduct);
        warehouseDAO.updateWarehouse(warehouse);
        checkStock(productWarehouse);
    }

    public void addWarehouse(String name, String capacity){
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacity(validationService.parseAndValidateInt(capacity, "capacity"));
        warehouse.setName(name);
        validationService.validateWarehouse(name, warehouse.getCapacity());
        warehouseDAO.insertWarehouse(warehouse);
    }

    public void allocateOrder(Order order, Warehouse warehouse){
        Warehouse fresh = warehouseDAO.getWarehouseById(warehouse.getId());
        if(fresh == null) throw new CustomException("Warehouse not found", "NOT_FOUND");
        int currentUsage = calculateTotalProducts(fresh);
        int orderTotal = order.getOrderProducts()
                .stream()
                .mapToInt(OrderProduct::getAmount)
                .sum();
        if(currentUsage + orderTotal > fresh.getCapacity())
            throw new CustomException(
                    "This warehouse doesn't have enough capacity for this order. Please choose another warehouse.",
                    "INSUFFICIENT_CAPACITY");

        for(var op : order.getOrderProducts()){
            fresh.getProductWarehouses().stream()
                    .filter(pw -> pw.getProduct().equals(op.getProduct()))
                    .findFirst()
                    .ifPresentOrElse(
                            pw -> pw.setAmount(pw.getAmount() + op.getAmount()),
                            () -> fresh.getProductWarehouses()
                                    .add(new ProductWarehouse(fresh, op.getProduct(), op.getAmount()))
                    );
        }
        warehouseDAO.updateWarehouse(fresh);
    }

    private void checkStock(ProductWarehouse pw) {
        Product product = pw.getProduct();
        Warehouse warehouse = pw.getWarehouse();
        int amount = pw.getAmount();
        int limit = product.getMinLimit();

        if (amount == 0) {
            String msg = String.format(
                    "%s is out of stock in %s. Make an order ASAP!",
                    product.getName(), warehouse.getName());
            homePageService.notify(msg, true, true);
            return;
        }

        if (limit > 0 && amount <= limit) {
            String msg = String.format(
                    "Low stock on %s: %d %s remaining in %s. "
                            + "Place a reorder ASAP!",
                    product.getName(), amount, product.getUnit().getShortName(), warehouse.getName());
            homePageService.notify(msg, true, true);
        }
    }

    public Optional<ProductWarehouse> findProductWarehouseByName(Warehouse warehouse, String name){
        if (warehouse == null) return Optional.empty();

        return warehouse.getProductWarehouses()
                .stream()
                .filter(pw -> pw.getProduct()
                        .getName()
                        .equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<ProductWarehouse> findProductWarehouseByBarcode(Warehouse warehouse, String barcode){
        if (warehouse == null) return Optional.empty();

        return warehouse.getProductWarehouses()
                .stream()
                .filter(pw -> barcode.equals(pw.getProduct().getBarcode()))
                .findFirst();
    }

    public void decreaseReceiptItemFromWarehouse(Receipt receipt, Warehouse warehouse) {
        for (ReceiptItem item : receipt.getItems()) {
            Product product = item.getProduct();
            double quantity = item.getQuantity();

            findProductWarehouseByName(warehouse, product.getName())
                    .ifPresentOrElse(pw -> {
                        int currentAmount = pw.getAmount();
                        int newAmount = currentAmount - (int) quantity;
                        if (newAmount < 0) {
                            throw new CustomException("Insufficient stock for product: " + product.getName(), "INSUFFICIENT_STOCK");
                        }
                        if (newAmount == 0) {
                            deleteProductFromWarehouse(warehouse, product);
                        } else {
                            updateProductStock(warehouse, pw, newAmount);
                        }
                    }, () -> {
                        throw new CustomException("Product not found in warehouse: " + product.getName(), "PRODUCT_NOT_FOUND");
                    });
        }
    }

    public void addProductToWarehouse(Warehouse warehouse, Product product, int amount){
        warehouseDAO.addProductToWarehouse(warehouse.getId(), product.getId(), amount);
    }

    public void transferProduct(Warehouse           sourceWarehouse,
                                ProductWarehouse     sourcePw,
                                int                  amount,
                                Warehouse            targetWarehouse) {

        if (sourceWarehouse.getId() == targetWarehouse.getId())
            throw new CustomException("Please choose a different destination warehouse", "GENERAL");

        int freeSpaceInTarget = targetWarehouse.getCapacity()
                - calculateTotalProducts(targetWarehouse);

        validationService.validateTransfer(sourcePw.getAmount(),
                amount,
                freeSpaceInTarget);

        warehouseDAO.transferProduct(sourcePw.getProduct().getId(),
                amount,
                sourceWarehouse.getId(),
                targetWarehouse.getId());
    }
}