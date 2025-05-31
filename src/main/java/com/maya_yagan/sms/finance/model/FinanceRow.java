package com.maya_yagan.sms.finance.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FinanceRow {
    private final ObjectProperty<LocalDateTime> dateTime = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty  category = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
    private final StringProperty person = new SimpleStringProperty();

    public FinanceRow(LocalDateTime dateTime,
                      String description,
                      String category,
                      BigDecimal amount,
                      String person) {
        this.dateTime.set(dateTime);
        this.description.set(description);
        this.category.set(category);
        this.amount.set(amount);
        this.person.set(person);
    }

    // getters for PropertyValueFactory
    public LocalDateTime getDateTime()  { return dateTime.get(); }
    public String        getDescription(){ return description.get(); }
    public String        getCategory()  { return category.get(); }
    public BigDecimal    getAmount()    { return amount.get(); }
    public String        getPerson()       { return person.get(); }

    // properties
    public ObjectProperty<LocalDateTime> dateTimeProperty() { return dateTime; }
    public StringProperty descriptionProperty()             { return description; }
    public StringProperty categoryProperty()                { return category; }
    public ObjectProperty<BigDecimal> amountProperty()      { return amount; }
    public StringProperty personProperty()                  { return person; }
}
