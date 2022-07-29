package com.example.calendarmemories;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class SettingsFragment extends Fragment {

    public static final String defaultViewSettingVal = "List view";
    private static final String USERS_DIR = "users";
    private static final String COMPLETE_FIELD = "profileCompleted";
    private static final String COMPLETED = "completed";

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
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                                AuthUI.getInstance().signOut(getContext())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                ((MainActivity) getActivity()).updateUI(null);
                                            }
                                        });
                            }
                        })
                        .show();
            }
        });
        userEditBtn = v.findViewById(R.id.userEditBtn);
        updateUI(user);

        return v;
    }

    public String helloName() {
        if (user == null || user.isAnonymous()) {
            return "Hello, Anonymous!";
        } else {
            return "Hello, " + user.getDisplayName() + "!";
        }
    }

    public void updateUI(FirebaseUser user) {
        System.out.println("SettingsFragment updateUI called");
        helloUserTxt.setText(helloName());
        if (user == null || user.isAnonymous()) {
            logoutBtn.setVisibility(View.GONE);
            userEditBtn.setVisibility(View.GONE);
            loginBtn.setVisibility(View.VISIBLE);
            showAnonymousAlert();
            hideIncompleteAlert();
        } else {
            logoutBtn.setVisibility(View.VISIBLE);
            userEditBtn.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);
            (v.findViewById(R.id.anonymousLayout)).setVisibility(View.GONE);
            didCompleteProfile();
        }
    }

    private void didCompleteProfile() {
        DocumentReference userDB = db.document(getPathForAccountInfo());
        userDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) { showIncompleteAlert(); return; }
                    Map<String, Object> data = document.getData();
                    String completed = (String) data.get(COMPLETE_FIELD);
                    if (completed == null || completed.equals(COMPLETED)) { showIncompleteAlert(); return; }
                    hideIncompleteAlert();
                } else {
                    // #TODO: task not successful response
                }
            }
        });
    }

    private void showIncompleteAlert() {
        v.findViewById(R.id.userInfoIncompleteLayout).setVisibility(View.VISIBLE);
        v.findViewById(R.id.incompleteLearnMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(getContext())
                        .setMessage(R.string.info_incomplete_detail)
                        .show();
            }
        });
    }

    private void hideIncompleteAlert() {
        v.findViewById(R.id.userInfoIncompleteLayout).setVisibility(View.GONE);
    }

    private void showAnonymousAlert() {
        v.findViewById(R.id.anonymousLayout).setVisibility(View.VISIBLE);
        v.findViewById(R.id.anonymousLearnMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(getContext())
                        .setMessage(R.string.anonymous_learn_more_msg)
                        .show();
            }
        });
    }

    private void hideAnonymousAlert() {
        v.findViewById(R.id.anonymousLayout).setVisibility(View.GONE);
    }

    private void saveChanges() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.saved_daily_view_setting_key),
                stoiViewSetting(defaultViewSettingText.getText().toString()));
        editor.apply();
    }

    public String getPathForAccountInfo() {
        return ViewHelper.joinPath(USERS_DIR, user.getUid());
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