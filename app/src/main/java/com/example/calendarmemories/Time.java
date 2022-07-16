package com.example.calendarmemories;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Time {

    public static LocalDate getTodaysDate() {
        return LocalDate.now();
    }

    public static LocalDate getYesterday(LocalDate today) {
        return today.minusDays(1);
    }

    public static LocalDate getTomorrow(LocalDate today) {
        return today.plusDays(1);
    }

    public static LocalDate getLastMonth(LocalDate today) {
        return today.minusMonths(1);
    }

    public static LocalDate getNextMonth(LocalDate today) {
        return today.plusMonths(1);
    }

    public static String toString(LocalDate today) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy LLLL d (EEEE)", Locale.ENGLISH);
        return today.format(dtf);
    }

    public static String getDayID(LocalDate today) {
        return today.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    public static String getMonth(LocalDate today) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy LLLL", Locale.ENGLISH);
        return today.format(dtf);
    }

    public static int getFirstDay(LocalDate today) {
        DayOfWeek dayOfWeek = today.withDayOfMonth(1).getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY: return 1;
            case TUESDAY: return 2;
            case WEDNESDAY: return 3;
            case THURSDAY: return 4;
            case FRIDAY: return 5;
            case SATURDAY: return 6;
            case SUNDAY: return 0;
        }
        return -1;
    }
}
