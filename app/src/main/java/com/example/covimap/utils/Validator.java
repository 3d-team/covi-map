package com.example.covimap.utils;

import com.example.covimap.config.Config;

public class Validator {

    public static boolean isEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPassword(String password) {
        return !password.isEmpty() && password.length() > Config.PASSWORD_LENGTH;
    }

    public static boolean isUsername(String userName) {
        return !userName.isEmpty() && userName.length() > Config.USERNAME_LENGTH;
    }
}
