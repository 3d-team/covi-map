package com.example.covimap.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Route {
    private List<CLocation> path;
    private LocalDateTime createdDay;
    private String uuid;
    private String userId;
}
