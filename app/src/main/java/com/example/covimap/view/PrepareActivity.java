package com.example.covimap.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.model.AppStatus;
import com.example.covimap.model.MyAccount;
import com.example.covimap.utils.Validator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.Locale;

public class PrepareActivity extends Activity {
    private AppStatus appStatus;
    private MyAccount myAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareStatus();
        setContentView(R.layout.prepare_layout);
    }

    public void prepareStatus(){
        try {
            FileInputStream inputStream = this.openFileInput(Config.STATUS_FILE_DIR);
            ObjectInputStream os = new ObjectInputStream(inputStream);
            appStatus = (AppStatus) os.readObject();
            os.close();
        }
        catch (Exception e){
            try {
                FileOutputStream outputStream = this.openFileOutput(Config.STATUS_FILE_DIR, MODE_PRIVATE);
                appStatus = new AppStatus();
                appStatus.writeStatusToFile(outputStream);
            }
            catch (Exception e1){
                Log.d("MyLog", e1.getMessage());
            }
            Log.d("MyLog", e.getMessage());
        }
        if(appStatus != null){
            Locale locale = new Locale(appStatus.getLanguage());
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

            if(appStatus.isLogged() && appStatus.getPhoneNumber().isEmpty() == false){
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(appStatus.getPhoneNumber());
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            myAccount = snapshot.getValue(MyAccount.class);
                            Intent intent = new Intent(PrepareActivity.this, MainActivity.class);
                            intent.putExtra("AccountData", myAccount);
                            intent.putExtra("AppStatus", appStatus);
                            startActivity(intent);
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else {
                Intent intent = new Intent(PrepareActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
