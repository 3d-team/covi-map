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
}
