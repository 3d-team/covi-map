package com.example.covimap.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class User {
    private String id;
    private String userName;
    private String email;
    private String password;
//    private String birthday;


    public boolean matchedPassword(User user) {
        return this.password.equals(user.getPassword());
    }
}
