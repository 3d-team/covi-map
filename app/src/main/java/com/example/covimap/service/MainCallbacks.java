package com.example.covimap.service;

public interface MainCallbacks {
//    public void onMsgFromFragToMain(String mess, String info);
    public void onChangeLanguage(String lang);
    public void onChangeLoginStatus(boolean islogged);
    public void onColorChange(String color);
    public void onQRCodeChange(String QRCode);
}