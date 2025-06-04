package com.maya_yagan.sms.common;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class ConfigManager {
    private static final Config config;
    static {
        File configFile = new File(System.getProperty("user.dir"), "application.conf");
        if (configFile.exists()) {
            config = ConfigFactory.parseFile(configFile)
                    .withFallback(ConfigFactory.load())
                    .resolve();
        } else {
            config = ConfigFactory.load();
        }
    }

    public static boolean isFirstRun() {
        try {
            return getDbDriver().isEmpty() ||
                    getDbUrl().isEmpty() ||
                    getDbUsername().isEmpty() ||
                    getDbPassword().isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    public static String getDbDriver() {
        return config.getString("db.driver");
    }

    public static String getDbUrl() {
        return config.getString("db.url");
    }

    public static String getDbUsername() {
        return config.getString("db.username");
    }

    public static String getDbPassword() {
        return config.getString("db.password");
    }
}