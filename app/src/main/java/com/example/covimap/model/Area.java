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

    public void writeStatusToFile(FileOutputStream fileOutputStream){
        try{
            ObjectOutputStream os = new ObjectOutputStream(fileOutputStream);
            os.writeObject(this);
            os.close();
        }
        catch (Exception e){
            Log.d("Exception", e.getMessage());
        }
    }

    public void addChildArea(Area area){
        if(this.childAreas == null){
            this.childAreas = new HashMap<>();
        }
        this.childAreas.put(area.getName(), area);
    }

    @Override
    public String toString() {
        Log.d("---MyLog", level + " - " + name + " - " + color);
        if(childAreas != null && childAreas.isEmpty() == false){

        }
        return "Area{" +
                "level='" + level + '\'' +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", childAreas=" + childAreas.size() +
                '}';
    }
}
