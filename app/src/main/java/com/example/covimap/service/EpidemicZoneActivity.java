package com.example.covimap.service;

import com.example.covimap.model.Area;

import java.util.HashMap;

public interface EpidemicZoneActivity {
    public void getMapArea(Area area);
    public void setStatusText(String address, String numberF0, String color);
}
