package com.maya_yagan.sms.user.service;

import com.maya_yagan.sms.user.model.Role;
import com.maya_yagan.sms.user.model.User;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorizationService {

    public enum Module {
        EMPLOYEE,
        FINANCE,
        PRODUCT,
        INVENTORY,
        SUPPLIER,
        ORDER,
        PAYMENT,
    }

    private static final Map<String, Set<Module>> roleAccessMap =
    Map.of(
            "manager", Set.of(Module.values()),
            "accountant", Set.of(Module.EMPLOYEE, Module.FINANCE, Module.PRODUCT, Module.INVENTORY, Module.SUPPLIER, Module.ORDER),
            "warehouse worker", Set.of(Module.INVENTORY, Module.SUPPLIER, Module.ORDER),
            "cashier", Set.of(Module.PAYMENT, Module.SUPPLIER, Module.ORDER)
    );

    public Set<Module> getAllowedModules(User user) {
        Set<Module> allowedModules = EnumSet.noneOf(Module.class);
        for (Role role : user.getRoles()) {
            Set<Module> modules = roleAccessMap.get(role.getName().toLowerCase());
            if (modules != null) {
                allowedModules.addAll(modules);
            }
        }
        return allowedModules;
    }
}
