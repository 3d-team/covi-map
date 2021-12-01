package com.example.covimap.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private String startAddress;
    private String endAddress;

    public static void addToFireBase(String phoneNumber, Route route){
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(phoneNumber).child("Routes");
        route.setUuid(mDatabaseReference.push().getKey());
        mDatabaseReference.child(route.uuid).setValue(route);
    }
}
