package com.example.covimap.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Facility extends Identity {
    private CLocation location;
    private String title;
    private String summary;
    private String type;
}
