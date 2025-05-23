package com.maya_yagan.sms.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;


/**
 *
 * @author Maya Yagan
 */
public class DateUtil {
    public static final String DEFAULT_DATE_PATTERN = "dd.MM.yyyy";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm";
    public static final String DEFAULT_DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);

    public static String formatDate(LocalDate date) {
        return date == null
                ? ""
                : DATE_FORMATTER.format(date);
    }

    public static String formatTime(LocalTime time) {
        return time == null
                ? ""
                : TIME_FORMATTER.format(time);
    }

    public static String formatTimeOrDefault(LocalTime time, String defaultText) {
        return (time == null) ? defaultText : TIME_FORMATTER.format(time);
    }

    public static String formatDayName(LocalDate date) {
        if (date == null) return "";
        DayOfWeek day = date.getDayOfWeek();
        return day.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    public static String formatDuration(Duration duration) {
        if (duration == null) return "";
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        return String.format("%d hrs %d mins", hours, minutes);
    }

    public static String formatDateTime(LocalDateTime dateTime){
        return dateTime == null ? "" : DATE_TIME_FORMATTER.format(dateTime);
    }

    public static void applyDateFormat(DatePicker datePicker){
        datePicker.setConverter(new StringConverter<LocalDate>(){
            @Override
            public String toString(LocalDate date){
                return DateUtil.formatDate(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if(text == null || text.trim().isEmpty()) return null;
                try {
                    return LocalDate.parse(text, DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    ExceptionHandler.handleException(new CustomException(
                            "Invalid date format.\nPlease follow this format: " + DEFAULT_DATE_PATTERN,
                            "INVALID_DATE"));
                    return null;
                }
            }
        });

        datePicker.setPromptText(DEFAULT_DATE_PATTERN);

        datePicker.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                try {
                    LocalDate ld = datePicker.getConverter()
                            .fromString(datePicker.getEditor().getText());
                    datePicker.setValue(ld);
                } catch (Exception e) {
                    datePicker.getEditor().clear();
                }
            }
        });
    }
}
