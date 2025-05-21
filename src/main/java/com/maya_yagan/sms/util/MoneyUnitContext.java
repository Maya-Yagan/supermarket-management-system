package com.maya_yagan.sms.util;

/**
 *
 * @author Rahaf Alaa
 */

public class MoneyUnitContext {
    private static String selectedMoneyUnitCode = "USD"; // default

    public static String getSelectedMoneyUnitCode() {
        return selectedMoneyUnitCode;
    }

    public static void setSelectedMoneyUnitCode(String code) {
        selectedMoneyUnitCode = code;
    }
}
