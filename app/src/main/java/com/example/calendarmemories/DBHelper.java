package com.example.calendarmemories;

import java.util.Random;

public class DBHelper {

    public static final String USERS_DIR = "users";
    public static final int COUPLE_CODE_LEN = 6;
    private static final String ALPHABETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    public static String getPathForAccountInfo(String uid) {
        return joinPath(USERS_DIR, uid);
    }

    public static String getPathForUsernames() {
        return joinPath(USERS_DIR, "usernames");
    }

    public static String joinPath(String ... dirs) {
        String path = "";
        for (int i = 0; i < dirs.length; i++) {
            if (i != 0) path += "/";
            path += dirs[i];
        }
        return path;
    }

    public static String generateCoupleCode() {
        Random random = new Random(System.currentTimeMillis());
        String code = "";
        for (int i = 0; i < COUPLE_CODE_LEN; i++) {
            code += ALPHABETS.charAt(random.nextInt(ALPHABETS.length()));
        }
        return code;
    }
}
