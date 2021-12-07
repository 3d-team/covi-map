package com.example.covimap.repository;

import com.example.covimap.model.CLocation;

public class RedPlaceRepository extends Repository<CLocation> {
    public RedPlaceRepository() {
        collection = "redzones";
    }
}
