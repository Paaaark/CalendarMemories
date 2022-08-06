package com.example.calendarmemories.settings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.calendarmemories.DBHelper;
import com.example.calendarmemories.R;
import com.example.calendarmemories.ViewHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditUserFragment extends DialogFragment {

    public static final String USERNAME_KEY = "username";
    public static final String TAG = "EditUserFragment";
    private static final String COMPLETE_FIELD = "profileCompleted";
    private static final String COMPLETED = "completed";

    private View v;
    private TextInputEditText usernameFieldTxt, nameFieldTxt, emailFieldTxt, passwordFieldTxt,
            confirmPasswordFieldTxt;
    private MaterialButton applyBtn;
    private SharedPreferences sharedPref;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public EditUserFragment() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    public EditUserFragment(SharedPreferences sharedPref, FirebaseAuth mAuth) {
        this.sharedPref = sharedPref;
        this.mAuth = mAuth;
        this.user = mAuth.getCurrentUser();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_edit_user, container, false);

        usernameFieldTxt = v.findViewById(R.id.usernameFieldTxt);
        nameFieldTxt = v.findViewById(R.id.nameFieldTxt);
        emailFieldTxt = v.findViewById(R.id.emailFieldTxt);
        passwordFieldTxt = v.findViewById(R.id.passwordFieldTxt);
        confirmPasswordFieldTxt = v.findViewById(R.id.confirmPasswordFieldTxt);
        applyBtn = v.findViewById(R.id.applyBtn);

        setUsername();
        nameFieldTxt.setText(user.getDisplayName());
        emailFieldTxt.setText(user.getEmail());

        emailFieldTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {  }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String email = emailFieldTxt.getText().toString();
                if (!ViewHelper.isValidEmail(email)) {
                    emailFieldTxt.setError(getString(R.string.invalid_email));
                } else {
                    emailFieldTxt.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        confirmPasswordFieldTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String confirmPassword = confirmPasswordFieldTxt.getText().toString();
                String password = passwordFieldTxt.getText().toString();
                if (!confirmPassword.equals(password)) {
                    confirmPasswordFieldTxt.setError(getString(R.string.invalid_confirm_password));
                } else {
                    confirmPasswordFieldTxt.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {  }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(getContext())
                        .setMessage(R.string.apply_changes_title)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { }
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String username = usernameFieldTxt.getText().toString();
                                if (username != null && username.length() > 0) applyUsername();
                                else dismiss();
                            }
                        })
                        .show();
            }
        });

        return v;
    }

    public void applyUsername() {
        DocumentReference userDB = db.document(DBHelper.getPathForAccountInfo(user.getUid()));
        Map<String, Object> dataToModify = new HashMap<String, Object>();
        dataToModify.put(USERNAME_KEY, usernameFieldTxt.getText().toString());
        dataToModify.put(COMPLETE_FIELD, COMPLETED);
        userDB.set(dataToModify)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ((SettingsFragment) getParentFragment()).updateUI(user);
                            ViewHelper.getSnackbar(v, R.string.user_info_update_success,
                                    Snackbar.LENGTH_SHORT, null);
                        } else {
                            ViewHelper.getSnackbar(v, R.string.user_info_update_failure,
                                    Snackbar.LENGTH_LONG, null);
                        }
                        dismiss();
                    }
                });
    }

    public void setUsername() {
        DocumentReference userDB = db.document(DBHelper.getPathForAccountInfo(user.getUid()));
        userDB.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        String username = (String) data.get(USERNAME_KEY);
                        if (username != null && username.length() > 0) {
                            usernameFieldTxt.setText(username);
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getTheme() {
        return R.style.FullScreenDialog;
    }
}