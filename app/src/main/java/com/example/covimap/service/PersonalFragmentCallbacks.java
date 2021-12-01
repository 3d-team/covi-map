package com.example.covimap.service;

import com.example.covimap.model.AppStatus;
import com.example.covimap.model.MyAccount;

public interface PersonalFragmentCallbacks {
    public void setStatus(AppStatus appStatus);
    public void setMyAccount(MyAccount myAccount);
}
