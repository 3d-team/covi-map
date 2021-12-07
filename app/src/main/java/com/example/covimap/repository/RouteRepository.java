package com.example.covimap.repository;

import com.example.covimap.model.Route;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RouteRepository extends Repository<Route> {
    public RouteRepository() {
        collection = "routes";
    }

    public void addByPhoneNumber(String phoneNumber, Route route) {
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(phoneNumber)
                .child("Routes");
        route.setUuid(mDatabaseReference.push().getKey());
        mDatabaseReference.child(route.getUuid()).setValue(route);
    }
}
