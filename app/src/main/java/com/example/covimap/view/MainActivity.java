package com.example.covimap.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.model.AppStatus;
import com.example.covimap.service.MainCallbacks;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements MainCallbacks {
    private BottomNavigationView bottomNav;
    private AppStatus appStatus;

    private NewRecordActivity newRecordActivity;
    private DirectActivity directActivity;
    private HistoryJourneyActivity historyJourneyActivity;
    private DiscoverActivity discoverActivity;
    private PersonalActivity personalActivity;
    private Fragment currentFragment;

    public MainActivity() {
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareStatus();
        setContentView(R.layout.activity_main);

        newRecordActivity = new NewRecordActivity();
        directActivity = new DirectActivity();
        historyJourneyActivity = new HistoryJourneyActivity();
        discoverActivity = new DiscoverActivity();
        personalActivity = new PersonalActivity();


        currentFragment = null;

        bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_new_record:
                        currentFragment = newRecordActivity;
                        break;
                    case R.id.nav_direct_location:
                        currentFragment = directActivity;
                        break;
                    case R.id.nav_history_record:
                        currentFragment = historyJourneyActivity;
                        break;
                    case R.id.nav_discover:
                        currentFragment = discoverActivity;
                        break;
                    case R.id.nav_peronal:
                        personalActivity.setStatus(appStatus);
                        currentFragment = personalActivity;
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment).commit();
                return true;
            }
        });

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newRecordActivity).commit();
        }
        Log.d("STATUS:", "CREATE");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("STATUS:", "START");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("STATUS:", "RESTART");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("STATUS:", "RESUME");
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            appStatus.writeStatusToFile(this.openFileOutput(Config.STATUS_FILE_DIR, MODE_PRIVATE));
        }
        catch (Exception e){
            Log.d("IOException:", e.getMessage());
        }
        Toast.makeText(this, "Bạn cần khởi động lại ứng dụng", Toast.LENGTH_LONG);
        Log.d("STATUS:", "PAUSE");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d("STATUS:", "STOP");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("STATUS:", "DESTROY");
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
                Log.d("__477_484_495", e1.getMessage());
            }
            Log.d("__477_484_495", e.getMessage());
        }
        if(appStatus != null){
            Locale locale = new Locale(appStatus.getLanguage());
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    public void onChangeLanguage(String lang){
        if(appStatus == null){
            Log.d("__477_484_495", "APP_STATUS_NULL");
            return;
        }
        appStatus.setLanguage(lang);
        finish();
    }
}