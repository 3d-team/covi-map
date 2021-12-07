package com.example.covimap.service;

import android.graphics.Bitmap;

import com.example.covimap.model.AppStatus;
import com.example.covimap.model.User;

public interface PersonalFragmentCallbacks {
    void setStatus(AppStatus appStatus);
    void setMyAccount(User user);
    void setUpCovidPassPort(String color, Bitmap qrCode);
}
