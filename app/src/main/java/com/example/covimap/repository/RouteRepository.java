package com.example.covimap.repository;

import com.example.covimap.model.Route;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RouteRepository extends Repository<Route> {
    public RouteRepository() {
        field = "routes";
    }
}
