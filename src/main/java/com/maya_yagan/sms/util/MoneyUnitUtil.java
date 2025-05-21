// com.maya_yagan.sms.util.MoneyUnitUtil.java
package com.maya_yagan.sms.util;

import com.maya_yagan.sms.product.model.MoneyUnit;
import com.maya_yagan.sms.product.service.ProductService;

/**
 *
 * @author Rahaf Alaa
 */

public class MoneyUnitUtil {

    private static final ProductService productService = new ProductService();

    public static String getCurrentMoneyUnitName() {
        String code = MoneyUnitContext.getSelectedMoneyUnitCode();
        MoneyUnit unit = productService.getMoneyUnitByCode(code);
        return (unit != null) ? unit.getName() : "";
    }

    public static String formatPriceHeader(String baseHeader) {
        return baseHeader + " (" + getCurrentMoneyUnitName() + ")";
    }

    public static String formatHeaderWithMoneyUnitName(String baseHeader) {
        String name = getCurrentMoneyUnitName();
        return baseHeader + (name.isEmpty() ? "" : " (" + name + ")");
    }

    public static String getCurrentMoneyUnitCode() {
        return MoneyUnitContext.getSelectedMoneyUnitCode();
    }
    public static void setSelectedMoneyUnitCode(String code) {
        MoneyUnitContext.setSelectedMoneyUnitCode(code);
    }
    public static String getCurrentMoneyUnitSymbol() {
        String code = getCurrentMoneyUnitCode();
        MoneyUnit unit = productService.getMoneyUnitByCode(code);
        return (unit != null) ? unit.getSymbol() : code;
    }

}
