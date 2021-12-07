package com.example.covimap.repository;

import com.example.covimap.model.Identity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public abstract class Repository<E extends Identity> {
    protected String collection;

    public void add(E data) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(collection);
        String uuid = mDatabase.push().getKey();
        data.setUuid(uuid);
        mDatabase.child(uuid).setValue(data);
    }

    public void update(E data) {
        FirebaseDatabase.getInstance().getReference().child(collection)
                .child(data.getUuid()).setValue(data);
    }

    public void delete(String uuid) {
        FirebaseDatabase.getInstance().getReference().child(collection)
                .child(uuid).removeValue();
    }
}
