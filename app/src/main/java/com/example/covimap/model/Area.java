package com.example.covimap.model;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Area implements Serializable {
    private String level;
    private String name;
    private String color;
    private String numberF0;
    private List<CLocation> boundaries;
    private HashMap<String, Area> childAreas;

    @Override
    public String toString() {
        Log.d("---MyLog", level + " - " + name + " - " + color);

        return ("Area{" +
                "level='" + level + '\'' +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", childAreas=" + childAreas.size() +
                '}');
    }
}
