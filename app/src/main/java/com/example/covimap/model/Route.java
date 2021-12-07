package com.example.covimap.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route extends Identity {
    private List<CLocation> path;
    private String period;
    private String distance;
    private String createdDay;
    private String startAddress;
    private String endAddress;

    @Override
    public String toString() {
        return "Route{" +
                "path=" + path +
                ", period='" + period + '\'' +
                ", distance='" + distance + '\'' +
                ", createdDay='" + createdDay + '\'' +
                ", startAddress='" + startAddress + '\'' +
                ", endAddress='" + endAddress + '\'' +
                '}';
    }
}
