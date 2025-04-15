package com.maya_yagan.sms.util;

import com.maya_yagan.sms.product.model.Category;
import java.util.function.Supplier;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import java.util.function.Consumer;
import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author Maya Yagan
 */
public class MenuButtonUtil {
    
    public static <T> void populateMenuButton(
        MenuButton menuButton,
        Supplier<Set<T>> itemsSupplier,
        Function<T, String> itemNameMapper,
        Consumer<T> onItemSelected,
        String allItemsLabel,
        Runnable onAllItemsSelected){

        menuButton.getItems().clear();
        MenuItem allItems = new MenuItem(allItemsLabel);
        menuButton.setText(allItemsLabel);

        allItems.setOnAction(event -> {
            menuButton.setText(allItemsLabel);
            onAllItemsSelected.run();
        });

        menuButton.getItems().add(allItems);

        Set<T> items = itemsSupplier.get();
        if (items != null) {
            for (T item : items) {
                String label = itemNameMapper.apply(item);
                MenuItem menuItem = new MenuItem(label);
                menuItem.setOnAction(event -> {
                    menuButton.setText(label);
                    onItemSelected.accept(item);
                });
                menuButton.getItems().add(menuItem);
            }
        }
    }
}
