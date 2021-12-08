package com.example.covimap.repository;

import com.example.covimap.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserRepository extends Repository<User>{
    public UserRepository() {
        collection = "Users";
    }

    public void addByPhoneNumber(User user, String phoneNumber) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(phoneNumber);
        String uuid = mDatabase.push().getKey();
        user.setUuid(uuid);
        mDatabase.child(uuid).setValue(user);
    }
}
