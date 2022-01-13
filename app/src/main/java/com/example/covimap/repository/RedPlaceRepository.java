package com.example.covimap.repository;

import com.example.covimap.model.Location;

public class RedPlaceRepository extends Repository<Location> {
    public RedPlaceRepository() {
        collection = "redzones";
    }
}
