package com.example.calendarmemories;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    public static final int DAILY_TAB = 0;
    public static final int MONTHLY_TAB = 1;

    TabLayout tabLayout;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainFragmentContainer, DailyFragment.class, null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();

        tabLayout = findViewById(R.id.mainTabNavigator);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                fragmentTransaction = fragmentManager.beginTransaction();
                switch (tab.getPosition()) {
                    case DAILY_TAB:
                        fragmentTransaction.replace(R.id.mainFragmentContainer, DailyFragment.class, null);
                        break;
                }
                fragmentTransaction.setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}