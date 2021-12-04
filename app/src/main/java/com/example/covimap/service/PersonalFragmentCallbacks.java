package com.example.covimap.service;

import android.graphics.Bitmap;

import com.example.covimap.model.AppStatus;
import com.example.covimap.model.MyAccount;

public interface PersonalFragmentCallbacks {
    public void setStatus(AppStatus appStatus);
    public void setMyAccount(MyAccount myAccount);
    public void setUpCovidPassPort(String color, Bitmap qrCode);
}
