package com.example.calendarmemories.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.calendarmemories.MainActivity;
import com.example.calendarmemories.R;
import com.example.calendarmemories.ViewHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends DialogFragment {

    public static final String TAG = "LoginFragment";

    private View v;
    private Dialog dialog;
    private TextInputEditText emailFieldTxt, passwordFieldTxt;
    private MaterialButton loginBtn;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    public LoginFragment() {
        mAuth = FirebaseAuth.getInstance();
    }

    public LoginFragment(FirebaseUser user) {
        this.currentUser = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         v = inflater.inflate(R.layout.fragment_login, container, false);

        emailFieldTxt = v.findViewById(R.id.emailFieldTxt);
        emailFieldTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {  }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!Patterns.EMAIL_ADDRESS.matcher(emailFieldTxt.getText().toString()).matches()) {
                    emailFieldTxt.setError("Invalid email address", null);
                } else {
                    emailFieldTxt.setError(null);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {  }
        });
        passwordFieldTxt = v.findViewById(R.id.passwordFieldTxt);
        passwordFieldTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = passwordFieldTxt.getText().toString().trim();
                if (password.length() == 0) {
                    passwordFieldTxt.setError(getString(R.string.valid_password), null);
                } else {
                    passwordFieldTxt.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        loginBtn = v.findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    String email = emailFieldTxt.getText().toString();
                    String password = passwordFieldTxt.getText().toString();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        ((MainActivity) getActivity()).updateUI(user);
                                        ((LoginPageFragment) getParentFragment()).dismissLoginPage();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        new MaterialAlertDialogBuilder(getContext())
                                                .setTitle(R.string.login_failed_title)
                                                .setMessage(R.string.login_failed_msg)
                                                .show();
                                    }
                                }
                            });
                } else {
                    ViewHelper.getSnackbar(v, R.string.check_required_fields, Snackbar.LENGTH_LONG, null)
                            .show();
                }
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        dialog = getDialog();
        if (dialog != null) {
//            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
//                    WindowManager.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public int getTheme() {
        return R.style.FullScreenDialog;
    }

    public boolean validateFields() {
        String email = emailFieldTxt.getText().toString();
        String password = passwordFieldTxt.getText().toString().trim();
        boolean valid = true;
        if (email.length() == 0) {
            emailFieldTxt.setError("Please enter an email", null);
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailFieldTxt.getText().toString()).matches()) {
            valid = false;
        }
        if (password.length() == 0) {
            passwordFieldTxt.setError("Please enter a password", null);
            valid = false;
        }
        return valid;
    }
}