package com.example.calendarmemories;

import android.app.FragmentContainer;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private static final int TOTAL_ENTRIES = 42;
    private static final float DIMMED_TEXT = 0.2f;
    private static final float NORMAL_TEXT = 1.0f;

    private LocalDate date;
    private Button monthBtn, leftMonthBtn, rightMonthBtn;
    private FloatingActionButton floatingBtn;
    private FragmentContainerView fragmentContainer;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    ArrayList<LinearLayout> weeklyEntries;
    ArrayList<LinearLayout> calendarEntries;
    Map<String, Integer> dayToIndex;
    Map<String, DailyMemory> dailyMemories;
    private LinearLayout highlightedDay;

    // #TODO: User authentification
    private static String userDataDir = "userData";
    private String userID = "tempUser";
    private static String foodDir = "foodData";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        fragmentContainer = v.findViewById(R.id.calendarFragmentContainer);
        monthBtn = v.findViewById(R.id.monthBtn);
        updatePanels();
        leftMonthBtn = v.findViewById(R.id.leftMonthBtn);
        leftMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goLastMonth();
            }
        });
        rightMonthBtn = v.findViewById(R.id.rightMonthBtn);
        rightMonthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNextMonth();
            }
        });
        floatingBtn = v.findViewById(R.id.floatingBtn);
        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // #TODO: CalendarView add food
                AddFragment myAddFragment = new AddFragment(date);
                myAddFragment.show(
                        getChildFragmentManager(), AddFragment.TAG
                );
            }
        });
        highlightedDay = calendarEntries.get(dayToIndex.get(Time.getDayID(date)));
        highlightedDay.setBackgroundResource(R.drawable.calendar_border);
        for (LinearLayout day: calendarEntries) {
            day.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (highlightedDay == day) {
                        // #TODO: set the date of dailyFragment to selected date
                        LocalDate target = Time.decodeID(highlightedDay.getTag().toString());
                        DailyFragment myDailyFragment = new DailyFragment(target);
                        myDailyFragment.show(
                                getChildFragmentManager(), DailyFragment.TAG
                        );
                    } else { // else, highlight the selected date
                        if (highlightedDay != null) {
                            highlightedDay.setBackground(new ColorDrawable(Color.TRANSPARENT));
                        }
                        day.setBackgroundResource(R.drawable.calendar_border);
                        highlightedDay = day;
                        date = Time.decodeID(day.getTag().toString());
                    }
                }
            });
        }
        return v;
    }

    public void goNextMonth() {
        date = Time.getNextMonth(date);
        updatePanels();
    }

    public void goLastMonth() {
        date = Time.getLastMonth(date);
        updatePanels();
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
        dayLayout.setTag(Time.getDayID(date));
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            textView.setTextColor(getResources().getColor(R.color.red, getContext().getTheme()));
        } else if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            textView.setTextColor(getResources().getColor(R.color.light_blue_A200, getContext().getTheme()));
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
        // If the object already exists, use cached data
        if (dailyMemories.containsKey(Time.getDayID(date))) {
            DailyMemory dailyMemory = dailyMemories.get(Time.getDayID(date));
            for (int i = 0; i < dailyMemory.getNumFood(); i++) {
                dayLayout.addView(getTextView(dailyMemory.getFoodAt(i).getFoodName()));
            }
            return;
        }
        // Otherwise, get the data
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
        textView.setText(text);
        textView.setTextAppearance(R.style.TextAppearance_AppCompat_calendar_food_entry);
        textView.setMaxLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        //textView.setTextAppearance(R.style.calendar_food_entry);
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

    public void addFood(Food food, LocalDate date) {
        DailyMemory dailyMemory = dailyMemories.get(Time.getDayID(date));
        food.setFoodID(dailyMemory.generateFoodID());
        dailyMemory.addFood(food);
        LinearLayout layout = calendarEntries.get(dayToIndex.get(Time.getDayID(date)));
        layout.addView(getTextView(food.getFoodName()));
        addFoodToDatabase(food, date, dailyMemory);
    }

    private void addFoodToDatabase(Food food, LocalDate target, DailyMemory dailyMemory) {
        DocumentReference userDB = db.document(getDatabasePath(target));
        Map<String, Object> dataToAdd = new HashMap<String, Object>();
        dataToAdd.put(DailyMemory.NUM_FOOD_KEY, dailyMemory.getNumFood());
        dataToAdd.put(DailyMemory.ID_TEMPLATE_KEY, dailyMemory.getIDTemplate());
        dataToAdd.put(food.getFoodID(), food);
        userDB.set(dataToAdd, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        ViewHelper.getSnackbar(getView(), R.string.add_success_msg,
                                Snackbar.LENGTH_SHORT, floatingBtn).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ViewHelper.getSnackbar(getView(), R.string.add_failed_msg,
                                Snackbar.LENGTH_LONG, floatingBtn).show();
                    }
                });
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