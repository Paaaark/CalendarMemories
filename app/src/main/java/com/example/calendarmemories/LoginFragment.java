package com.example.calendarmemories;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


public class LoginFragment extends DialogFragment {

    public static final String TAG = "LoginFragment";

    private View v;
    private Dialog dialog;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_login, container, false);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
    }
}