package com.maya_yagan.sms.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.event.EventHandler;
import javafx.util.StringConverter;

/**
 *
 * @author Maya Yagan
 */
public class TableViewUtil {
    /**
     * Sets up a table column for editing with a TextField cell.
     *
     * @param <S> the type of the items contained within the TableView
     * @param <T> the type of the cell data
     * @param column the TableColumn to be set up for editing
     * @param tableView the TableView that contains the column; it will be set to editable
     * @param converter a StringConverter for converting between String and T; if null, a default converter is used
     * @param commitHandler an event handler that is invoked when an edit is committed
     */
    public static <S, T> void setupColumnForEditing(
            TableColumn<S, T> column,
            TableView<S> tableView,
            StringConverter<T> converter,
            EventHandler<TableColumn.CellEditEvent<S, T>> commitHandler) {
        if (converter != null) 
            column.setCellFactory(TextFieldTableCell.forTableColumn(converter));
        //else
            //column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit(commitHandler);
        tableView.setEditable(true);
    }
}
