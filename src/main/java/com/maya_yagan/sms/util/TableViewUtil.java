package com.maya_yagan.sms.util;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 *
 * @author Maya Yagan
 */
public class TableViewUtil {
    public static <T, V> void setupCheckboxColumn(
            TableColumn<T, Boolean> column,
            Map<T, V> backingMap,
            BiConsumer<T, Boolean> onToggle) {

        column.setCellValueFactory(cellData -> {
            T item = cellData.getValue();
            SimpleBooleanProperty prop = new SimpleBooleanProperty(backingMap.containsKey(item));

            prop.addListener((obs, wasSelected, isNowSelected) -> {
                onToggle.accept(item, isNowSelected);
                cellData.getTableView().refresh();
            });

            return prop;
        });

        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        column.setEditable(true);
    }
}