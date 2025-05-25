package com.maya_yagan.sms.settings.service;

import com.maya_yagan.sms.common.ValidationService;
import com.maya_yagan.sms.settings.dao.SettingsDAO;
import com.maya_yagan.sms.settings.model.Settings;
import com.maya_yagan.sms.util.CustomException;

public class SettingsService {
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final ValidationService validationService = new ValidationService();

    public boolean addSettings(String name, String address, String phone, String moneyUnit){
        if (getSettings() != null)
            throw new CustomException("Settings already exist. Cannot add again.");

        Settings settings = new Settings();
        settings.setMarketName(name);
        settings.setAddress(address);
        settings.setPhone(phone);
        settings.setMoneyUnit(moneyUnit);
        validationService.validateSettings(settings);
        return settingsDAO.insertSettings(settings);
    }

    public boolean updateSettings(String name, String address, String phone, String moneyUnit){
        Settings existing = getSettings();

        if (existing == null)
            throw new CustomException("Settings not found. Please add settings first.");

        existing.setMarketName(name);
        existing.setAddress(address);
        existing.setPhone(phone);
        existing.setMoneyUnit(moneyUnit);
        validationService.validateSettings(existing);
        return settingsDAO.updateSettings(existing);
    }

    public Settings getSettings(){
        return settingsDAO.getSettings();
    }
}
