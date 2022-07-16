package com.example.calendarmemories;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private LocalDate date;
    private Button monthBtn, leftMonthBtn, rightMonthBtn;
    ArrayList<LinearLayout> weeklyEntries;
    ArrayList<LinearLayout> calendarEntries;
    Map<String, Integer> dayToIndex;
    Map<String, DailyMemory> dailyMemories;

    private static final int TOTAL_ENTRIES = 42;
    private static final float DIMMED_TEXT = 0.2f;
    private static final float NORMAL_TEXT = 1.0f;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // #TODO: User authentification
    private static String userDataDir = "userData";
    private String userID = "tempUser";
    private static String foodDir = "foodData";

    public CalendarFragment() {
        weeklyEntries = new ArrayList<LinearLayout>();
        calendarEntries = new ArrayList<LinearLayout>();
        dayToIndex = new HashMap<String, Integer>();
        dailyMemories = new HashMap<String, DailyMemory>();
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
        dayToIndex.clear();
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

    private void setDate(LinearLayout dayLayout, LocalDate date, boolean dim) {
        for (int i = dayLayout.getChildCount() - 1; i > 0; i--) {
            dayLayout.removeViewAt(i);
        }
        TextView textView = dayLayout.findViewById(R.id.dayNumber);
        textView.setText(Integer.toString(date.getDayOfMonth()));
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            textView.setTextColor(getResources().getColor(R.color.red, getContext().getTheme()));
        } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            textView.setTextColor(getResources().getColor(R.color.blue, getContext().getTheme()));
        }
        if (dim) {
            textView.setAlpha(DIMMED_TEXT);
        } else {
            getDailyMemoryAndUpdatePanel(dayLayout, date, db);
            textView.setAlpha(NORMAL_TEXT);
        }
    }

    private void getDailyMemoryAndUpdatePanel(LinearLayout dayLayout, LocalDate date,
                                                     FirebaseFirestore db) {
        DocumentReference userDB = db.document(getDatabasePath(date));
        userDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    DailyMemory dailyMemory = new DailyMemory(document, date);
                    dailyMemories.put(Time.getDayID(date), dailyMemory);
                    for (int i = 0; i < dailyMemory.getNumFood(); i++) {
                        dayLayout.addView(getTextView(dailyMemory.getFoodAt(i).getFoodName()));
                    }
                } else {
                    // #TODO: Failed task response
                    System.out.println("Task not successful");
                }
            }
        });
    }

    private TextView getTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setTextAppearance(R.style.calendar_food_entry);
        textView.setText(text);
        return textView;
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

    private String getDatabasePath(LocalDate target) {
        return joinPath(userDataDir, userID, foodDir, Time.getDayID(target));
    }

    private String joinPath(String ... dirs) {
        String path = "";
        for (int i = 0; i < dirs.length; i++) {
            if (i != 0) path += "/";
            path += dirs[i];
        }
        return path;
    }
}