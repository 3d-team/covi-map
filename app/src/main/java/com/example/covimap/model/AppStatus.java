package com.example.covimap.model;

import android.util.Log;

import com.example.covimap.config.LanguageConfig;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppStatus implements Serializable {
    private String language;
    private boolean isLogged;
    private String phoneNumber;

    public AppStatus(){
        this.language = LanguageConfig.VI;
        this.isLogged = false;
        this.phoneNumber = "";
    }

    public void writeStatusToFile(FileOutputStream fileOutputStream){
        try{
            ObjectOutputStream os = new ObjectOutputStream(fileOutputStream);
            os.writeObject(this);
            os.close();
        }
        catch (Exception e){
            Log.d("Exception", e.getMessage());
        }
    }

    public boolean isLogged() {
        return isLogged;
    }

    @Override
    public String toString() {
        return "AppStatus{" +
                "language='" + language + '\'' +
                ", isLogged=" + isLogged +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
