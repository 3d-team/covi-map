package com.example.covimap.repository;

import com.example.covimap.model.Facility;

public class FacilityRepository extends Repository<Facility> {
    public FacilityRepository() {
        collection = "services";
    }
}
