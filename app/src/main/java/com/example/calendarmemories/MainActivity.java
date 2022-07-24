package com.example.calendarmemories;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.MaterialSharedAxis;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int DAILY_TAB = 0;
    public static final int MONTHLY_TAB = 1;
    public static final int SETTINGS_TAB = 2;

    private LinearLayoutCompat mainConstraintLayout;
    private TabLayout tabLayout;
    private FragmentContainerView fragmentContainerView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private GestureDetectorCompat gestureDetector;
    private SharedPreferences sharedPref;
    private int dailyFragmentViewSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainConstraintLayout = findViewById(R.id.mainConstraintLayout);
        fragmentContainerView = findViewById(R.id.mainFragmentContainer);
        tabLayout = findViewById(R.id.mainTabNavigator);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        dailyFragmentViewSetting = sharedPref.getInt(getString(R.string.saved_daily_view_setting_key), R.layout.list_view);

        fragmentContainerView.setMinimumHeight(mainConstraintLayout.getHeight() - tabLayout.getHeight());

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainFragmentContainer, new DailyFragment(dailyFragmentViewSetting))
                .setReorderingAllowed(true)
                .commit();

        gestureDetector = new GestureDetectorCompat(this, new MyGestureListener());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragmentOne = getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
                Fragment fragmentTwo;
                switch (tab.getPosition()) {
                    case MONTHLY_TAB:
                        fragmentTwo = new CalendarFragment();
                        break;
                    case SETTINGS_TAB:
                        fragmentTwo = new SettingsFragment();
                        break;
                    case DAILY_TAB:
                    default:
                        dailyFragmentViewSetting = sharedPref.getInt(getString(R.string.saved_daily_view_setting_key), R.layout.list_view);
                        fragmentTwo = new DailyFragment(dailyFragmentViewSetting);
                        break;
                }
                //fragmentOne.setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
                //fragmentTwo.setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
                //fragmentTwo.setAllowEnterTransitionOverlap(true);
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction()
                                .replace(R.id.mainFragmentContainer, fragmentTwo);
//                switch (tab.getPosition()) {
//                    case DAILY_TAB:
//                        fragmentTransaction.replace(R.id.mainFragmentContainer, fragmentTwo);
//                        break;
//                    case MONTHLY_TAB:
//                        fragmentTransaction.replace(R.id.mainFragmentContainer, fragmentTwo);
//                        break;
//                }
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
    }

    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.exit_app_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAndRemoveTask();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                })
                .show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        System.out.println("Hello1");
        return gestureDetector.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "gesture";
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        @Override
        public boolean onDown(MotionEvent event) {
            System.out.println("onDown Detected");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            System.out.println
                    ("onFling activated");
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        if (fragment instanceof  DailyFragment) {
                            ((DailyFragment) fragment).goYesterday();
                        } else if (fragment instanceof CalendarFragment) {
                            ((CalendarFragment) fragment).goLastMonth();
                        }
                    } else {
                        if (fragment instanceof DailyFragment) {
                            ((DailyFragment) fragment).goTomorrow();
                        } else if (fragment instanceof CalendarFragment) {
                            ((CalendarFragment) fragment).goNextMonth();
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }

}