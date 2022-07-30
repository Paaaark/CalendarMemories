package com.example.calendarmemories;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.calendarmemories.settings.SettingsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PersonalFragment extends Fragment {

    public static final int DAILY_TAB = 0;
    public static final int MONTHLY_TAB = 1;
    public static final int SETTINGS_TAB = 2;

    private LinearLayout mainLinearLayout;
    private TabLayout tabLayout;
    private FragmentContainerView fragmentContainerView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private SharedPreferences sharedPref;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private int dailyFragmentViewSetting;
    private View v;

    public PersonalFragment() {

    }

    public PersonalFragment(SharedPreferences sharedPref, FirebaseAuth mAuth) {
        this.sharedPref = sharedPref;
        this.mAuth = mAuth;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_personal, container, false);

        mainLinearLayout = v.findViewById(R.id.mainLinearLayout);
        fragmentContainerView = v.findViewById(R.id.mainFragmentContainer);
        tabLayout = v.findViewById(R.id.mainTabNavigator);

        dailyFragmentViewSetting = sharedPref.getInt(getString(R.string.saved_daily_view_setting_key), R.layout.list_view);
        dailyFragmentViewSetting = checkViewSettingValid(dailyFragmentViewSetting);

        fragmentContainerView.setMinimumHeight(mainLinearLayout.getHeight() - tabLayout.getHeight());

        fragmentManager = getChildFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainFragmentContainer, new DailyFragment(dailyFragmentViewSetting, mAuth))
                .setReorderingAllowed(true)
                .commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragmentOne = getChildFragmentManager().findFragmentById(R.id.mainFragmentContainer);
                Fragment fragmentTwo;
                switch (tab.getPosition()) {
                    case MONTHLY_TAB:
                        fragmentTwo = new CalendarFragment(mAuth);
                        break;
                    case DAILY_TAB:
                    default:
                        dailyFragmentViewSetting = sharedPref.getInt(getString(R.string.saved_daily_view_setting_key), R.layout.list_view);
                        dailyFragmentViewSetting = checkViewSettingValid(dailyFragmentViewSetting);
                        fragmentTwo = new DailyFragment(dailyFragmentViewSetting, mAuth);
                        break;
                }
                fragmentManager = getChildFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction()
                        .replace(R.id.mainFragmentContainer, fragmentTwo);
                fragmentTransaction.setReorderingAllowed(true)
                        .commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return v;
    }

    public void updateUI(FirebaseUser user) {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.mainFragmentContainer);
        if (fragment instanceof DailyFragment) {

        } else if (fragment instanceof SettingsFragment) {
            System.out.println("UI Updating");
            Fragment nextFragment = new SettingsFragment(mAuth);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.mainFragmentContainer, nextFragment)
                    .setReorderingAllowed(true)
                    .commit();
        }
    }

    public void swipeRight() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.mainFragmentContainer);
        if (fragment instanceof  DailyFragment) {
            ((DailyFragment) fragment).goYesterday();
        } else if (fragment instanceof CalendarFragment) {
            ((CalendarFragment) fragment).goLastMonth();
        }
    }

    public void swipeLeft() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.mainFragmentContainer);
        if (fragment instanceof DailyFragment) {
            ((DailyFragment) fragment).goTomorrow();
        } else if (fragment instanceof CalendarFragment) {
            ((CalendarFragment) fragment).goNextMonth();
        }
    }

    public int checkViewSettingValid(int viewSetting) {
        if (viewSetting != R.layout.list_view && viewSetting != R.layout.gallery_view) {
            return R.layout.list_view;
        } else {
            return viewSetting;
        }
    }


}