package com.example.calendarmemories;

import android.util.Patterns;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class ViewHelper {


    public static Snackbar getSnackbar(View v, int msgRes, int duration, View anchor) {
        if (anchor == null) {
            return Snackbar.make(v, msgRes, duration);
        } else {
            return Snackbar.make(v, msgRes, duration).setAnchorView(anchor);
        }
    }


    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
