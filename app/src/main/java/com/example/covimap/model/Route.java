package com.example.covimap.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Route extends Identity {
    private List<CLocation> path;
    private String period; // time amount
    private String distance; // distance amount
    private String createdDay;
    private long userId;

    public List<CLocation> getPath() {
        return path;
    }

    public void setPath(List<CLocation> path) {
        this.path = path;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCreatedDay() {
        return createdDay;
    }

    public void setCreatedDay(String createdDay) {
        this.createdDay = createdDay;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
