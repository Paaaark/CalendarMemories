package com.example.calendarmemories;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPageFragment extends DialogFragment {

    public static final String TAG = "LoginPageFragment";
    private static final int LOGIN_TAB = 0;
    private static final int REGISTER_TAB = 1;

    private View v;
    private FragmentContainerView fragmentContainerView;
    private TabLayout tabLayout;
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    public LoginPageFragment() {
        // Required empty public constructor
    }

    public LoginPageFragment(FirebaseAuth mAuth, FirebaseUser user) {
        this.mAuth = mAuth;
        this.user = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_login_page, container, false);

        fragmentContainerView = v.findViewById(R.id.loginPageFragmentContainer);

        // On default, inflate login fragment
        getChildFragmentManager().beginTransaction()
                .replace(R.id.loginPageFragmentContainer, new LoginFragment())
                .addToBackStack(null)
                .setReorderingAllowed(true)
                .commit();

        tabLayout = v.findViewById(R.id.loginPageTabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                switch (tab.getPosition()) {
                    case REGISTER_TAB:
                        fragment = new RegisterFragment(user);
                        break;
                    default:
                        fragment = new LoginFragment();
                        break;
                }
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.loginPageFragmentContainer, fragment)
                        .addToBackStack(null)
                        .setReorderingAllowed(true)
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

    public void dismissLoginPage() {
        dismiss();
    }

    @Override
    public int getTheme() {
        return R.style.FullScreenDialog;
    }
}