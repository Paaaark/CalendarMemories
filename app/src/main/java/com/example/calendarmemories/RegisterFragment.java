package com.example.calendarmemories;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFragment extends DialogFragment {

    public static final String TAG = "RegisterFragment";

    private TextInputEditText nameFieldTxt, emailFieldTxt, passwordFieldTxt, confirmPasswordFieldTxt;
    private MaterialButton registerBtn;
    private View v;


    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_register, container, false);

        nameFieldTxt = v.findViewById(R.id.nameFieldTxt);
        addTextListener(nameFieldTxt, getString(R.string.valid_name));
        emailFieldTxt = v.findViewById(R.id.emailFieldTxt);
        addTextListener(emailFieldTxt, getString(R.string.valid_email));
        passwordFieldTxt = v.findViewById(R.id.passwordFieldTxt);
        addTextListener(passwordFieldTxt, getString(R.string.valid_password));
        confirmPasswordFieldTxt = v.findViewById(R.id.confirmPasswordFieldTxt);
        addTextListener(confirmPasswordFieldTxt, getString(R.string.valid_confirm_password));

        registerBtn = v.findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateFields()) {
                    ViewHelper.getSnackbar(v, R.string.check_required_fields, Snackbar.LENGTH_LONG, null)
                            .show();
                } else {
                    // #TODO: Continue logging in
                }
            }
        });

        return v;
    }

    @Override
    public int getTheme() {
        return R.style.FullScreenDialog;
    }

    public void addTextListener(TextInputEditText view, String errorMsg) {
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {  }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = view.getText().toString().trim();
                switch (view.getId()) {
                    case R.id.emailFieldTxt:
                        if (text.length() == 0 || !checkEmail(text)) view.setError(errorMsg);
                        else view.setError(null);
                        break;
                    case R.id.confirmPasswordFieldTxt:
                        String password = ((TextInputEditText) v.findViewById(R.id.passwordFieldTxt)).
                                getText().toString().trim();
                        if (text.length() == 0 || !password.equals(text)) view.setError(errorMsg);
                        else view.setError(null);
                        break;
                    default:
                        if (text.length() == 0) view.setError(errorMsg);
                        else view.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    public boolean validateFields() {
        String name = nameFieldTxt.getText().toString().trim();
        String email = emailFieldTxt.getText().toString().trim();
        String password = passwordFieldTxt.getText().toString().trim();
        String passwordConfirm = confirmPasswordFieldTxt.getText().toString().trim();
        boolean valid = true;
        if (name.length() == 0) {
            nameFieldTxt.setError(getString(R.string.valid_name));
            valid = false;
        }
        if (email.length() == 0 || !checkEmail(email)) {
            emailFieldTxt.setError(getString(R.string.valid_email));
            valid = false;
        }
        if (password.length() == 0) {
            passwordFieldTxt.setError(getString(R.string.valid_password));
            valid = false;
        }
        if (!passwordConfirm.equals(password)) {
            confirmPasswordFieldTxt.setError(getString(R.string.valid_confirm_password));
            valid = false;
        }
        return valid;
    }

    public boolean checkEmail(String target) {
        return Patterns.EMAIL_ADDRESS.matcher(emailFieldTxt.getText().toString()).matches();
    }
}