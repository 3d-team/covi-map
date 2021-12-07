package com.example.covimap.service;

import com.example.covimap.model.Area;

public interface RedPlaceActivity {
    void getMapArea(Area area);
    void setStatusText(String address, String numberF0, String color);
}
