package com.example.covimap.model;

import android.util.Log;

import com.example.covimap.config.LanguageConfig;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class AppStatus implements Serializable {
    private String language;

    public AppStatus(){
        this.language = LanguageConfig.VI;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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
}
