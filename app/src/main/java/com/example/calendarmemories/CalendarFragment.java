package com.example.calendarmemories;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    private LocalDate date;
    private Button monthBtn, leftMonthBtn, rightMonthBtn;
    ArrayList<LinearLayout> weeklyEntries;
    ArrayList<LinearLayout> calendarEntries;
    Map<String, Integer> dayToIndex;

    private static final int TOTAL_ENTRIES = 42;

    public CalendarFragment() {
        weeklyEntries = new ArrayList<LinearLayout>();
        calendarEntries = new ArrayList<LinearLayout>();
        dayToIndex = new HashMap<String, Integer>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        date = Time.getTodaysDate();

        // Initialize calendar entries
        weeklyEntries.add(v.findViewById(R.id.weekOne));
        weeklyEntries.add(v.findViewById(R.id.weekTwo));
        weeklyEntries.add(v.findViewById(R.id.weekThree));
        weeklyEntries.add(v.findViewById(R.id.weekFour));
        weeklyEntries.add(v.findViewById(R.id.weekFive));
        weeklyEntries.add(v.findViewById(R.id.weekSix));
        for (LinearLayout week: weeklyEntries) {
            getDailyEntriesInWeek(week);
        }

        monthBtn = v.findViewById(R.id.monthBtn);
        updatePanels();
        leftMonthBtn = v.findViewById(R.id.leftMonthBtn);
        leftMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date = Time.getLastMonth(date);
                updatePanels();
            }
        });
        rightMonthBtn = v.findViewById(R.id.rightMonthBtn);
        rightMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date = Time.getNextMonth(date);
                updatePanels();
            }
        });

        return v;
    }

    public void updatePanels() {
        monthBtn.setText(Time.getMonth(date));
        // 0-6 (Sun - Sat) representing day of the week of the first day of the month
        int startIndex = Time.getFirstDay(date);
        for (int i = 1; i <= date.lengthOfMonth(); i++) {
            setDate(calendarEntries.get(i + startIndex - 1), date.withDayOfMonth(i), false);
            dayToIndex.put(Time.getDayID(date.withDayOfMonth(i)), i + startIndex - 1);
        }
        for (int i = 0; i < startIndex; i++) {
            LocalDate tempDate = date.withDayOfMonth(1).minusDays(i + 1);
            setDate(calendarEntries.get(startIndex - i - 1), tempDate, true);
            dayToIndex.put(Time.getDayID(tempDate), startIndex - i - 1);
        }
        for (int i = date.lengthOfMonth() + startIndex; i < TOTAL_ENTRIES; i++) {
            LocalDate tempDate = date.withDayOfMonth(date.lengthOfMonth())
                    .plusDays(i - date.lengthOfMonth() - startIndex + 1);
            setDate(calendarEntries.get(i), tempDate, true);
            dayToIndex.put(Time.getDayID(tempDate), i);
        }
    }

    private void setDate(LinearLayout dayLayout, int day) {
        ((TextView) dayLayout.findViewById(R.id.dayNumber)).setText(Integer.toString(day));
    }

    private void setDate(LinearLayout dayLayout, LocalDate date, boolean dim) {
        TextView textView = dayLayout.findViewById(R.id.dayNumber);
        textView.setText(Integer.toString(date.getDayOfMonth()));
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            textView.setTextColor(getResources().getColor(R.color.red, getContext().getTheme()));
        } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            textView.setTextColor(getResources().getColor(R.color.blue, getContext().getTheme()));
        }
        if (dim) {
            textView.setAlpha(0.2f);
        } else {
            textView.setAlpha(1.0f);
        }
    }

    private void getDailyEntriesInWeek(LinearLayout layout) {
        calendarEntries.add(layout.findViewById(R.id.dayOne));
        calendarEntries.add(layout.findViewById(R.id.dayTwo));
        calendarEntries.add(layout.findViewById(R.id.dayThree));
        calendarEntries.add(layout.findViewById(R.id.dayFour));
        calendarEntries.add(layout.findViewById(R.id.dayFive));
        calendarEntries.add(layout.findViewById(R.id.daySix));
        calendarEntries.add(layout.findViewById(R.id.daySeven));
    }
}