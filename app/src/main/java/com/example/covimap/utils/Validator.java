package com.example.covimap.utils;

import com.example.covimap.config.Config;

public class Validator {

    public static boolean isPhoneNumber(String phone) {
        return !phone.isEmpty() && phone.length() > Config.PHONE_NUMBER_LENGTH;
    }

    public static boolean isPassword(String password) {
        return !password.isEmpty() && password.length() > Config.PASSWORD_LENGTH;
    }
}
