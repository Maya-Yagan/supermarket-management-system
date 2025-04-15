package com.maya_yagan.sms.util;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ContextMenuUtil {
    public static <T>ContextMenu createContextMenu( TableRow<T> row, T item, List<MenuItemConfig<T>> menuItemConfigs){
        ContextMenu contextMenu = new ContextMenu();
        for(MenuItemConfig<T> config : menuItemConfigs){
            if(config.getCondition() == null || config.getCondition().test(item)){
                MenuItem menuItem = new MenuItem(config.getLabel());
                menuItem.setOnAction(e -> config.getAction().accept(item, row));
                contextMenu.getItems().add(menuItem);
            }
        }
        return contextMenu;
    }

    public static class MenuItemConfig<T> {
        private final String label;
        private final BiConsumer<T, TableRow<T>> action;
        private final Predicate<T> condition;

        public MenuItemConfig(String label, BiConsumer<T, TableRow<T>> action, Predicate<T> condition){
            this.label = label;
            this.action = action;
            this.condition = condition;
        }

        public MenuItemConfig(String label, BiConsumer<T, TableRow<T>> action) {
            this(label, action, null);
        }

        public String getLabel() {
            return label;
        }

        public BiConsumer<T, TableRow<T>> getAction() {
            return action;
        }

        public Predicate<T> getCondition() {
            return condition;
        }
    }
}
