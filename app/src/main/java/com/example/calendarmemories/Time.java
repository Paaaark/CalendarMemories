package com.example.calendarmemories;

import com.google.android.material.datepicker.CalendarConstraints;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class Time {

    public static char[] ZEROES = new char[100];
    static {
        Arrays.fill(ZEROES, '0');
    }

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

    public static LocalDate decodeID(String targetID) {
        int year = Integer.parseInt(targetID.substring(0, 4));
        int month = Integer.parseInt(targetID.substring(4, 6));
        int day = Integer.parseInt(targetID.substring(6, 8));
        return LocalDate.of(year, month, day);
    }

    /**
     * Returns String of the date in BASIC_ISO_DATE format (YYYYMMDD). i.e) 20020801
     * @param today
     * @return
     */
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

    public static String getTimeStamp() {
        Instant instant = Instant.now();
        String seconds = Long.toString(instant.getEpochSecond());
        return seconds;
    }

    public static String getUID() {
        Instant instant = Instant.now();
        Random rand = new Random(instant.getEpochSecond());
        long temp = rand.nextLong();
        while (temp < 0) temp = rand.nextLong();
        String randID = prependLong(Long.toString(temp));
        return prependLong(getTimeStamp()) + randID;
    }

    public static long inMillis(LocalDate today) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
        return calendar.getTimeInMillis();
    }

    public static CalendarConstraints getCalendarConstraints(LocalDate today) {
        CalendarConstraints.Builder builder = new CalendarConstraints.Builder()
                .setStart(startInMillis(today))
                .setEnd(endInMillis(today));
        return builder.build();
    }

    private static long startInMillis(LocalDate today) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(today.getYear(), today.getMonthValue() - 1, 1);
        return calendar.getTimeInMillis();
    }

    private static long endInMillis(LocalDate today) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(today.getYear(), today.getMonthValue() - 1, today.lengthOfMonth());
        return calendar.getTimeInMillis();
    }

    private static String prependLong(String time) {
        if (time.length() < 19) {
            time = new String(ZEROES, 0, 19 - time.length()) + time;
        }
        return time;
    }
}
