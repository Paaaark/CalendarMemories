package com.example.calendarmemories;

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
}
