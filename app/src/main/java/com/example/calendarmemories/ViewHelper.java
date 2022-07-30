package com.example.calendarmemories;

import android.util.Patterns;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class ViewHelper {

    public static final String USERS_DIR = "users";

    public static Snackbar getSnackbar(View v, int msgRes, int duration, View anchor) {
        if (anchor == null) {
            return Snackbar.make(v, msgRes, duration);
        } else {
            return Snackbar.make(v, msgRes, duration).setAnchorView(anchor);
        }
    }

    public static String getPathForAccountInfo(String uid) {
        return joinPath(USERS_DIR, uid);
    }

    public static String joinPath(String ... dirs) {
        String path = "";
        for (int i = 0; i < dirs.length; i++) {
            if (i != 0) path += "/";
            path += dirs[i];
        }
        return path;
    }

    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
