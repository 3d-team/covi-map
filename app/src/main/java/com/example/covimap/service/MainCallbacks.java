package com.example.covimap.service;

import com.example.covimap.model.Language;

public interface MainCallbacks {
    void onChangeLanguage(Language lang);
    void onChangeLoginStatus(boolean isLogged);
    void onColorChange(String color);
    void onQRCodeChange(String QRCode);
}