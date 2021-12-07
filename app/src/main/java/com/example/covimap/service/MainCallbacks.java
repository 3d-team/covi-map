package com.example.covimap.service;

public interface MainCallbacks {
    void onChangeLanguage(String lang);
    void onChangeLoginStatus(boolean isLogged);
    void onColorChange(String color);
    void onQRCodeChange(String QRCode);
}