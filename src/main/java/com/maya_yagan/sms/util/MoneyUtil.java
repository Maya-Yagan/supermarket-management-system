package com.maya_yagan.sms.util;

import com.maya_yagan.sms.settings.service.SettingsService;

import java.math.BigDecimal;

public class MoneyUtil {

    private static SettingsService settingsService = new SettingsService();

    public static String formatMoney(BigDecimal amount){
        String unit = settingsService.getSettings().getMoneyUnit();
        return String.format("%,.2f %s", amount, unit);
    }
}
