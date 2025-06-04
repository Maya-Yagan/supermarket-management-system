package com.maya_yagan.sms.util;

import com.maya_yagan.sms.common.ConfigManager;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 *
 * @author Maya Yagan
 */
public class HibernateUtil {
    private static SessionFactory sessionFactory = null;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration config = getConfiguration();

                // Register your entity classes
                config.addAnnotatedClass(com.maya_yagan.sms.user.model.Role.class);
                config.addAnnotatedClass(com.maya_yagan.sms.user.model.User.class);
                config.addAnnotatedClass(com.maya_yagan.sms.product.model.Category.class);
                config.addAnnotatedClass(com.maya_yagan.sms.product.model.Product.class);
                config.addAnnotatedClass(com.maya_yagan.sms.warehouse.model.Warehouse.class);
                config.addAnnotatedClass(com.maya_yagan.sms.warehouse.model.ProductWarehouse.class);
                config.addAnnotatedClass(com.maya_yagan.sms.supplier.model.Supplier.class);
                config.addAnnotatedClass(com.maya_yagan.sms.supplier.model.SupplierProduct.class);
                config.addAnnotatedClass(com.maya_yagan.sms.order.model.Order.class);
                config.addAnnotatedClass(com.maya_yagan.sms.order.model.OrderProduct.class);
                config.addAnnotatedClass(com.maya_yagan.sms.user.model.Attendance.class);
                config.addAnnotatedClass(com.maya_yagan.sms.homepage.model.Notification.class);
                config.addAnnotatedClass(com.maya_yagan.sms.payment.model.Receipt.class);
                config.addAnnotatedClass(com.maya_yagan.sms.payment.model.ReceiptItem.class);
                config.addAnnotatedClass(com.maya_yagan.sms.finance.model.CashBox.class);
                config.addAnnotatedClass(com.maya_yagan.sms.finance.model.FinancialRecord.class);
                config.addAnnotatedClass(com.maya_yagan.sms.settings.model.Settings.class);
                config.addAnnotatedClass(com.maya_yagan.sms.user.model.Advance.class);
                config.addAnnotatedClass(com.maya_yagan.sms.user.model.SalaryRecord.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(config.getProperties()).build();

                sessionFactory = config.buildSessionFactory(serviceRegistry);

            } catch (Exception e) {
                System.err.println("Failed to build SessionFactory.");
                e.printStackTrace();
                return null;
            }
        }
        return sessionFactory;
    }

    private static Configuration getConfiguration() {
        Configuration config = new Configuration();

        config.setProperty("hibernate.connection.driver_class", ConfigManager.getDbDriver());
        config.setProperty("hibernate.connection.url", ConfigManager.getDbUrl());
        config.setProperty("hibernate.connection.username", ConfigManager.getDbUsername());
        config.setProperty("hibernate.connection.password", ConfigManager.getDbPassword());

        config.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");
        config.setProperty("hibernate.hbm2ddl.auto", "update");
        config.setProperty("hibernate.c3p0.min_size", "5");
        config.setProperty("hibernate.c3p0.max_size", "20");
        return config;
    }
}
