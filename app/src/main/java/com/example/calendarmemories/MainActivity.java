package com.example.calendarmemories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private GestureDetectorCompat gestureDetector;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private MaterialToolbar topAppBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFragmentContainer, new PersonalFragment(sharedPref, mAuth))
                .setReorderingAllowed(true)
                .commit();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationDrawer = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.open();
            }
        });
        navigationDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // #TODO: Edit this when we have social tab
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.menuSettings:
                        fragment = new SettingsFragment(mAuth);
                        break;
                    case R.id.menuCouple:
                        fragment = new CoupleFragment(sharedPref, mAuth);
                        break;
                    case R.id.menuSocial:
                        fragment = new SocialFragment(sharedPref, mAuth);
                        break;
                    case R.id.menuPersonal:
                    default:
                        fragment = new PersonalFragment(sharedPref, mAuth);
                        break;
                }
                drawerLayout.close();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainFragmentContainer, fragment)
                        .setReorderingAllowed(true)
                        .commit();
                return true;
            }
        });


        if (user == null) {
            signInAnonymously();
        }
        updateUI(user);

        gestureDetector = new GestureDetectorCompat(this, new MainActivity.MyGestureListener());
    }

    public void updateUI(FirebaseUser user) {
        // #TODO: updateUI in MainActivity
        this.user = user;
        if (user == null) {
            signInAnonymously();
        }
        View headerView = navigationDrawer.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.navigationDrawerUsername);
        TextView emailAddress = headerView.findViewById(R.id.navigationDrawerEmail);
        if (user == null || user.isAnonymous()) {
            userName.setText("Anonymous");
            emailAddress.setText("example@example.com");
        } else {
            userName.setText(user.getDisplayName());
            emailAddress.setText(user.getEmail());
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
        if (fragment instanceof PersonalFragment) {
            ((PersonalFragment) fragment).updateUI(user);
        } else if (fragment instanceof SettingsFragment) {
            ((SettingsFragment) fragment).updateUI(user);
        }
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
        return gestureDetector.onTouchEvent(event);
    }

    public void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                        }
                    }
                });
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
                        if (fragment instanceof PersonalFragment) {
                            ((PersonalFragment) fragment).swipeRight();
                        }

                    } else {
                        if (fragment instanceof PersonalFragment) {
                            ((PersonalFragment) fragment).swipeLeft();
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }

}