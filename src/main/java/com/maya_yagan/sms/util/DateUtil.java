package com.maya_yagan.sms.util;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;


/**
 *
 * @author Maya Yagan
 */
public class DateUtil {
    public static final String DEFAULT_DATE_PATTERN = "dd.MM.yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);

    public static void applyDateFormat(DatePicker datePicker){
        datePicker.setConverter(new StringConverter<LocalDate>(){
            @Override
            public String toString(LocalDate date){
                return (date != null) ? DATE_FORMATTER.format(date) : "";
            }
            
            @Override
            public LocalDate fromString(String string){
                if(string != null && !string.isEmpty()){
                    try{
                        return LocalDate.parse(string, DATE_FORMATTER);
                    } catch(DateTimeParseException e){
                    ExceptionHandler.handleException(new CustomException(
                        "Invalid date format.\nPlease follow this format: DD.MM.YYYY",
                      "INVALID_DATE"));
                    return null;
                    }
                }
                ExceptionHandler.handleException(new CustomException(
                "Date cannot be empty.", "EMPTY_FIELDS"));
                return null;
            }
        });
        datePicker.setPromptText(DEFAULT_DATE_PATTERN.toUpperCase());
    
        datePicker.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if(!newVal){
                try{
                    LocalDate parseDate = 
                            datePicker.getConverter()
                            .fromString(datePicker.getEditor().getText());
                    datePicker.setValue(parseDate);
                } catch (Exception e){
                    datePicker.getEditor().clear();
                }
            }
        });
    }
}
