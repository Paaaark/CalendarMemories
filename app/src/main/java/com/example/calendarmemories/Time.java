package com.example.calendarmemories;

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

    public static String toString(LocalDate today) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy LLLL d (EEEE)", Locale.ENGLISH);
        return today.format(dtf);
    }
}
