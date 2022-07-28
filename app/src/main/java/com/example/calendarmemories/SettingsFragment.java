package com.example.calendarmemories;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {

    public static final String defaultViewSettingVal = "List view";

    private View v;
    private LinearLayout anonymousLayout, mainContainer;
    private AutoCompleteTextView defaultViewSettingText;
    private MaterialButton applyBtn, userEditBtn, loginBtn, logoutBtn;
    private TextView helloUserTxt, anonymousLearnMore;
    private Context context;
    private SharedPreferences sharedPref;
    private String viewSettingVal;
    private String[] viewOptions;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public SettingsFragment(FirebaseAuth mAuth) {
        this.mAuth = mAuth;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_settings, container, false);

        user = mAuth.getCurrentUser();

        viewOptions = getResources().getStringArray(R.array.dailyFragmentViewOptions);
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), R.layout.dropdown_items, viewOptions);
        defaultViewSettingText = v.findViewById(R.id.defaultViewSettingText);
        defaultViewSettingText.setAdapter(arrayAdapter);

        context = getActivity();
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        viewSettingVal = itosViewSetting(sharedPref.getInt(getString(R.string.saved_daily_view_setting_key), R.layout.list_view));

        defaultViewSettingText.setText(viewSettingVal, false);

        applyBtn = v.findViewById(R.id.applyBtn);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.apply_changes_title)
                        .setPositiveButton(R.string.yes_apply, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveChanges();
                            }
                        })
                        .setNegativeButton(R.string.no_apply, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { }
                        })
                        .show();
            }
        });

        helloUserTxt = v.findViewById(R.id.helloUserTxt);
        if (user != null) {
            helloUserTxt.setText("Hello, " +  user.getDisplayName() + "!");
            if (user.isAnonymous()) {
                helloUserTxt.setText("Hello, Anonymous!");
                anonymousLayout = v.findViewById(R.id.anonymousLayout);
                anonymousLayout.setVisibility(View.VISIBLE);
                anonymousLearnMore = v.findViewById(R.id.anonymousLearnMore);
                anonymousLearnMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new MaterialAlertDialogBuilder(getContext())
                                .setMessage(R.string.anonymous_learn_more_msg)
                                .show();
                    }
                });
            }
        }

        loginBtn = v.findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginPageFragment loginPageFragment = new LoginPageFragment(mAuth, user);
                loginPageFragment.show(
                        getChildFragmentManager(), LoginPageFragment.TAG
                );
            }
        });
        logoutBtn = v.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(getContext())
                        .setMessage(R.string.logout_msg)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { }
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mAuth.signOut();
                                ((MainActivity) getActivity()).updateUI(null);
                            }
                        })
                        .show();
            }
        });
        userEditBtn = v.findViewById(R.id.userEditBtn);
        if (user != null && !user.isAnonymous()) {
            logoutBtn.setVisibility(View.VISIBLE);
            userEditBtn.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);
        }

        return v;
    }

    private void saveChanges() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.saved_daily_view_setting_key),
                stoiViewSetting(defaultViewSettingText.getText().toString()));
        editor.apply();
    }

    public String itosViewSetting(int val) {
        if (val == R.layout.gallery_view) {
            return viewOptions[1];
        } else {
            return viewOptions[0];
        }
    }

    public int stoiViewSetting(String val) {
        if (val.equals(viewOptions[1])) {
            return R.layout.gallery_view;
        } else {
            return R.layout.list_view;
        }
    }
}