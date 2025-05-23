package com.maya_yagan.sms.common;

import atlantafx.base.theme.Tweaks;
import com.maya_yagan.sms.util.ContextMenuUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

public abstract class AbstractTableController<T> implements Initializable {
    @FXML protected TableView<T> tableView;

    private final ObservableList<T> list = FXCollections.observableArrayList();

    @Override
    public void initialize(URL loc, ResourceBundle rb) {
        tableView.setItems(list);
        configureColumns();
        setupContextMenu();
        refresh();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getStyleClass().add(Tweaks.EDGE_TO_EDGE);
        postInit();
    }

    protected abstract void configureColumns();

    protected abstract Collection<T> fetchData();

    protected abstract List<ContextMenuUtil.MenuItemConfig<T>> menuItemsFor(T item);

    protected void postInit() {}

    private void setupContextMenu() {
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(
                            ContextMenuUtil.createContextMenu(row, newItem, menuItemsFor(newItem))
                    );
                }
            });
            return row;
        });
    }

    protected void refresh() {
        list.setAll(fetchData());
    }
}