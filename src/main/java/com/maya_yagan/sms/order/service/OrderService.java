package com.maya_yagan.sms.order.service;

import com.maya_yagan.sms.order.dao.OrderDAO;
import com.maya_yagan.sms.order.model.Order;
import com.maya_yagan.sms.order.model.OrderProduct;
import com.maya_yagan.sms.product.model.Product;
import com.maya_yagan.sms.supplier.model.Supplier;
import com.maya_yagan.sms.supplier.model.SupplierProduct;
import com.maya_yagan.sms.util.CustomException;
import com.maya_yagan.sms.util.ValidationService;
import com.maya_yagan.sms.warehouse.model.Warehouse;
import com.maya_yagan.sms.warehouse.service.WarehouseService;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public class OrderService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final WarehouseService warehouseService = new WarehouseService();
    private final ValidationService validationService = new ValidationService();

    public Set<Order> getOrders(){
        return orderDAO.getOrders();
    }

    public void updateOrder(Order order){

    }

    public void deleteOrder(int id){
        orderDAO.deleteOrder(id);
    }

    public float getPrice(Order order){
        float totalPrice = 0.0f;
        Supplier supplier = order.getSupplier();
        if(supplier != null && supplier.getSupplierProducts() != null){
            for(OrderProduct orderProduct : order.getOrderProducts()){
                Product product = orderProduct.getProduct();
                for(SupplierProduct supplierProduct : supplier.getSupplierProducts()){
                    if(supplierProduct.getProduct().equals(product)){
                        totalPrice += supplierProduct.getPrice() * orderProduct.getAmount();
                        break;
                    }
                }
            }
        }
        return totalPrice;
    }

    public void addProductsToOrder(Order order, Map<SupplierProduct, Integer> productsAmount){
        if(order == null)
            throw new CustomException("No order is currently being edited.", "NOT_FOUND");
        if(productsAmount.isEmpty())
            throw new CustomException("Please select at least one product to order.", "NOT_FOUND");
        for (var entry : productsAmount.entrySet()) {
            SupplierProduct sp = entry.getKey();
            Integer rawAmt = entry.getValue();

            int amt = validationService.parseAndValidateInt(
                    rawAmt.toString(),
                    "Amount for " + sp.getProduct().getName()
            );

            OrderProduct existing = order.getOrderProducts().stream()
                    .filter(op -> op.getProduct().equals(sp.getProduct()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setAmount(existing.getAmount() + amt);
            } else {
                OrderProduct op = new OrderProduct(order, sp.getProduct(), amt);
                order.getOrderProducts().add(op);
            }
        }
        orderDAO.updateOrder(order);
    }

    public void deliverOrder(Order order, Warehouse warehouse){
        warehouseService.allocateOrder(order, warehouse);
        order.setDeliveryDate(LocalDate.now());
        orderDAO.updateOrder(order);
    }
}
