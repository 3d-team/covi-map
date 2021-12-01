package com.example.covimap.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyAccount {
    private String phoneNumber;
    private String password;
    private String fullname;
    private String birthday;
    private String gender;
}
