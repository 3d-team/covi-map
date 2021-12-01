package com.example.covimap.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteLabel implements Serializable {
    private String uuid;
    private String period; // time amount
    private String distance; // distance amount
    private String createdDay;
    private String startAddress;
    private String endAddress;

    public String getUuid() {
        return uuid;
    }

    public String getPeriod() {
        return period + "min";
    }

    public String getDistance() {
        return distance + "km";
    }

    public String getCreatedDay() {
        return createdDay;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    @Override
    public String toString() {
        return "RouteLabel{" +
                "uuid='" + uuid + '\'' +
                ", period='" + period + '\'' +
                ", distance='" + distance + '\'' +
                ", createdDay='" + createdDay + '\'' +
                ", startAddress='" + startAddress + '\'' +
                ", endAddress='" + endAddress + '\'' +
                '}';
    }
}
