package com.example.covimap.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.model.AppStatus;
import com.example.covimap.model.Area;
import com.example.covimap.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.Locale;

public class PrepareActivity extends Activity {
    private AppStatus appStatus;
    private User user;
    private Area vietnam;
    private String vietnamJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prepare_layout);

        prepareAppStatus();
        prepareAppLanguage();
        prepareData();
    }

    public void prepareAppStatus() {
        try (FileInputStream inputStream = this.openFileInput(Config.STATUS_FILE_DIR);
             ObjectInputStream os = new ObjectInputStream(inputStream)) {
            appStatus = (AppStatus) os.readObject();
        } catch (Exception e){
            try (FileOutputStream outputStream = this.openFileOutput(Config.STATUS_FILE_DIR, MODE_PRIVATE)) {
                appStatus = new AppStatus();
                appStatus.writeStatusToFile(outputStream);
            } catch (Exception e1){
                Log.d("MyLog", e1.getMessage());
            }
        }
    }

    private void prepareAppLanguage() {
        if (appStatus == null) {
            return;
        }

        Locale locale = new Locale(appStatus.getLanguage());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private void prepareData() {
        if (appStatus == null) {
            return;
        }

        queryFirebaseData();
    }

    private void queryFirebaseData() {
        DatabaseReference data = FirebaseDatabase.getInstance().getReference();
        data.child("VietNam").child("numberF0").setValue("1,27Tr");
        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String key = dataSnapshot.getKey();

                    if (key.equals("Users")) {
                        prepareLoggedUserData(dataSnapshot);
                    } else if (key.equals("VietNam")){
                        prepareGeographyData(dataSnapshot);
                    }
                }

                triggerNextActivity();
            }

            private void prepareLoggedUserData(DataSnapshot dataSnapshot) {
                user = dataSnapshot.child(appStatus.getPhoneNumber()).getValue(User.class);
            }

            private void prepareGeographyData(DataSnapshot dataSnapshot) {
                vietnam = dataSnapshot.getValue(Area.class);
                Gson gson = new Gson();
                vietnamJSON = gson.toJson(vietnam);

                SharedPreferences preferences = getSharedPreferences(Config.SHARE_PREF_NAME, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("VietNamJSON", vietnamJSON);
                editor.commit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void triggerNextActivity(){
        if (verifyLoggedUser()) {
            switchToApp();
        } else {
            switchToLogin();
        }
    }

    private boolean verifyLoggedUser() {
        return appStatus.isLogged() && !appStatus.getPhoneNumber().isEmpty();
    }

    private void switchToApp() {
        Intent intent = new Intent(PrepareActivity.this, MainActivity.class);
        intent.putExtra("AccountData", user);
        intent.putExtra("AppStatus", appStatus);
        startActivity(intent);
        finish();
    }

    private void switchToLogin() {
        Intent intent = new Intent(PrepareActivity.this, LoginActivity.class);
        intent.putExtra("AppStatus", appStatus);
        startActivity(intent);
        finish();
    }
}
