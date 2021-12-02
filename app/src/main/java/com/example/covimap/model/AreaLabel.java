package com.example.covimap.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AreaLabel implements Serializable {
    private String level;
    private String name;
    private String color;
    private String numberF0;

    public AreaLabel(Area area){
        if(area != null){
            this.level = area.getLevel();
            this.color = area.getColor();
            this.name = area.getName();
            this.numberF0 = area.getNumberF0();
        }
    }
}
