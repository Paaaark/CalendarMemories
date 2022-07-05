package com.example.calendarmemories;

import android.os.Bundle;

import java.time.LocalDate;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 */
public class DailyFragment extends Fragment {

    Button dailyDateBtn;
    LocalDate date;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        date = Time.getTodaysDate();

        dailyDateBtn = getView().findViewById(R.id.dailyDateBtn);
        dailyDateBtn.setText(Time.toString(date));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_daily, container, false);
    }
}