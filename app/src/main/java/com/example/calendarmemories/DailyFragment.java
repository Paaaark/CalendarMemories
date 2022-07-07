package com.example.calendarmemories;

import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyFragment extends Fragment {

    private Button dailyDateBtn, leftDateBtn, rightDateBtn;
    private FloatingActionButton floatingBtn;
    private LocalDate date;
    private View v;
    private ConstraintLayout constraintLayout;
    private File currentFile = null;
    private static DailyMemory dailyMemory;
    private LinearLayout.LayoutParams layoutParams;
    private static LinearLayout foodLinearLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_daily, null, false);
        v.setClickable(true);
        v.setFocusable(true);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        date = Time.getTodaysDate();
        updateDailyMemory();
        updatePanel();

        dailyDateBtn = v.findViewById(R.id.dailyDateBtn);
        dailyDateBtn.setText(Time.toString(date));
        leftDateBtn = v.findViewById(R.id.leftDateBtn);
        leftDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goYesterday();
            }
        });
        rightDateBtn = v.findViewById(R.id.rightDateBtn);
        rightDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goTomorrow();
            }
        });
        floatingBtn = v.findViewById(R.id.floatingBtn);
        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddFragment myAddFragment = new AddFragment();
                myAddFragment.show(
                        getChildFragmentManager(), AddFragment.TAG
                );
                // #TODO: Floating btn click response
            }
        });
        constraintLayout = v.findViewById(R.id.constraintLayout);
        foodLinearLayout = v.findViewById(R.id.foodLinearLayout);
        return v;
    }

    public void goYesterday() {
        date = Time.getYesterday(date);
        dailyDateBtn.setText(Time.toString(date));
    }

    public void goTomorrow() {
        date = Time.getTomorrow(date);
        dailyDateBtn.setText(Time.toString(date));
    }

    public void updateDailyMemory() {
        // If we already have a file open, write in the file and close the file
        if (currentFile != null) {
            try (ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(currentFile))) {
                dailyMemory.updateFile(objOut);
            } catch (Exception e) {
                // #TODO: Exception handling
            }
        }
        // Else open a new file. If exists, read the data from it
        currentFile =  new File(getActivity().getFilesDir(), Time.toString(date));
        if (!currentFile.exists()) {
            try {
                currentFile.createNewFile();
                dailyMemory = new DailyMemory();
                return;
            } catch (Exception e) {
                // #TODO: Exception handling
            }
        }
        try (ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(currentFile))) {
            dailyMemory = new DailyMemory(objIn);
        } catch (Exception e) {
            // #TODO: Exception handling
        }
    }

    public void updatePanel() {
        // #TODO: updatePanel
        // Remove all previous views in the panel
        for (int i = foodLinearLayout.getChildCount() - 1; i > 0; i--) {
            foodLinearLayout.removeViewAt(i);
        }
        // Add views in the panel
        for (int i = 0; i < dailyMemory.getNumFood(); i++) {
            Food food = dailyMemory.getFoodAt(i);
            // #TODO: Finish up creating a card for foods
            MaterialCardView foodCard = new MaterialCardView();
        }
    }

    public ViewGroup.LayoutParams getCardLayoutParams() {
        // #TODO: Card Layout params method
    }

    public static void newFoodAdded(Food food) {
        dailyMemory.addFood(food);
    }
}