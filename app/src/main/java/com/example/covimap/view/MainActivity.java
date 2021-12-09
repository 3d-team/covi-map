package com.example.covimap.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.model.AppStatus;
import com.example.covimap.model.Area;
import com.example.covimap.model.CLocation;
import com.example.covimap.model.Language;
import com.example.covimap.model.User;
import com.example.covimap.service.MainCallbacks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MainActivity extends FragmentActivity implements MainCallbacks {
    private RecordingFragment recordingFragment;
    private DirectFragment directFragment;
    private HistoryJourneyFragment historyJourneyFragment;
    private RedPlaceFragment redPlaceFragment;
    private PersonalFragment personalFragment;
    private Fragment currentFragment;

    private Area vietnam;
    private AppStatus appStatus;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receiveAppStatus();
        prepareAppStatus();
        prepareSubActivity();
        mappingBottomNavBar();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment).commit();
        }
    }

    private void receiveAppStatus() {
        Intent intent = getIntent();
        appStatus = (AppStatus) intent.getSerializableExtra("AppStatus");
        user = (User)intent.getSerializableExtra("AccountData");
    }

    public void prepareAppStatus() {
        if (appStatus == null) {
            return;
        }

        Locale locale = new Locale(appStatus.getLanguage().name());
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.locale = locale;

        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private void prepareSubActivity() {
        recordingFragment = new RecordingFragment();
        directFragment = new DirectFragment();
        historyJourneyFragment = new HistoryJourneyFragment();
        redPlaceFragment = new RedPlaceFragment();
        personalFragment = new PersonalFragment();

        recordingFragment.getPhoneNumber(user.getPhoneNumber());
        currentFragment = recordingFragment;
    }

    private void mappingBottomNavBar() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);

        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.nav_new_record:
                    recordingFragment.getPhoneNumber(user.getPhoneNumber());
                    currentFragment = recordingFragment;
                    break;
                case R.id.nav_direct_location:
                    currentFragment = directFragment;
                    break;
                case R.id.nav_history_record:
                    historyJourneyFragment.getPhoneNumber(user.getPhoneNumber());
                    currentFragment = historyJourneyFragment;
                    break;
                case R.id.nav_epidemic_zone:
                    if(vietnam != null) {
                        redPlaceFragment.getMapArea(vietnam);
                    }
                    currentFragment = redPlaceFragment;
                    break;
                case R.id.nav_peronal:
                    personalFragment.setMyAccount(user);
                    personalFragment.setStatus(appStatus);
                    currentFragment = personalFragment;
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment).commit();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        LoadBoundariesData loadBoundariesData = new LoadBoundariesData();
        Thread thread = new Thread(loadBoundariesData);
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            appStatus.writeStatusToFile(this.openFileOutput(Config.STATUS_FILE_DIR, MODE_PRIVATE));
        } catch (Exception e){
            Log.d("IOException:", e.getMessage());
        }
    }

    @Override
    public void onChangeLanguage(Language lang){
        if (appStatus == null){
            return;
        }
        appStatus.setLanguage(lang);
        restartApp();
    }

    private void restartApp() {
        Intent intent = new Intent(MainActivity.this, PrepareActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onChangeLoginStatus(boolean isLogged) {
        if (appStatus == null){
            return;
        }

        appStatus.setLogged(isLogged);
        String color = "#" + Config.GRAY_ZONE_COLOR;
        appStatus.setColor(color);

        SharedPreferences preferences = getSharedPreferences(Config.SHARE_PREF_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ColorVaccine", color);
        editor.commit();

        finish();
    }

    @Override
    public void onColorChange(String color) {
        appStatus.setColor(color);
    }

    @Override
    public void onQRCodeChange(String QRCode) {
        appStatus.setQRCode(QRCode);
    }

    public static String getStringByIdName(Context context, String idName) {
        Resources res = context.getResources();
        return res.getString(res.getIdentifier(idName, "string", context.getPackageName()));
    }

    private class LoadBoundariesData implements Runnable {
        @Override
        public void run() {
            SharedPreferences preferences = getSharedPreferences(Config.SHARE_PREF_NAME, Activity.MODE_PRIVATE);
            String key = "VietNamJSON";

            if (preferences != null && preferences.contains(key)){
                String vietnamJSON = preferences.getString(key, "");
                if(!vietnamJSON.isEmpty()){
                    Gson gson = new Gson();
                    vietnam = gson.fromJson(vietnamJSON, Area.class);
                    getProvince(vietnam.getChildAreas());
                }
            }
        }

        private void getProvince(HashMap<String, Area> provinces){
            if (provinces == null) {
                provinces = new HashMap<>();
            }

            XmlPullParser parser = getResources().getXml(R.xml.gadm36_1);

            String nodeName, nodeText;
            String provinceName = "";
            List<CLocation> boundaries = new ArrayList<>();

            try {
                int eventType = -1;
                while(eventType != XmlPullParser.END_DOCUMENT) {
                    eventType = parser.next();
                    switch(eventType) {
                        case XmlPullParser.START_DOCUMENT:
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            nodeName = parser.getName();
                            if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_1")){
                                provinceName = parser.nextText();
                            } else if(nodeName.equals("coordinates")){
                                nodeText = parser.nextText();
                                String[] coordinatePairs = nodeText.split(" ");
                                for (String coord : coordinatePairs) {
                                    boundaries.add(new CLocation(Double.parseDouble(coord.split(",")[1]),
                                            Double.parseDouble(coord.split(",")[0])));
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            nodeName = parser.getName();
                            if(nodeName.equals("Placemark")) {
                                List<CLocation> temp = new ArrayList<>(boundaries);

                                if (provinces.get(provinceName) != null){
                                    provinces.get(provinceName).setBoundaries(temp);
                                }

                                boundaries.clear();
                            }
                            break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            getDistrict(provinces);
        }

        private void getDistrict(HashMap<String, Area> provinces){
            XmlPullParser parser = getResources().getXml(R.xml.gadm36_2);

            String nodeName, nodeText;
            List<CLocation> boundaries = new ArrayList<>();
            String provinceName = "";
            String districtName = "";

            try{
                int eventType = -1;
                while(eventType != XmlPullParser.END_DOCUMENT) {
                    eventType=parser.next();
                    switch(eventType) {
                        case XmlPullParser.START_DOCUMENT:
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            nodeName = parser.getName();
                            if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_1")){
                                provinceName = parser.nextText();
                            } else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_2")){
                                districtName = parser.nextText();
                            } else if(nodeName.equals("coordinates")){
                                nodeText = parser.nextText();
                                String[] coordinatePairs = nodeText.split(" ");
                                for(String coord : coordinatePairs){
                                    boundaries.add(new CLocation(Double.parseDouble(coord.split(",")[1]),
                                            Double.parseDouble(coord.split(",")[0])));
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            nodeName=parser.getName();
                            if(nodeName.equals("Placemark")) {
                                List<CLocation> temp = new ArrayList<>(boundaries);
                                Area district = provinces.get(provinceName).getChildAreas().get(districtName);
                                if(district != null){
                                    district.setBoundaries(temp);
                                }
                                boundaries.clear();
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            getCommnune(provinces);
        }

        private void getCommnune(HashMap<String, Area> provinces){
            XmlPullParser parser = getResources().getXml(R.xml.gadm36_3);

            String nodeName, nodeText;
            List<CLocation> boundaries = new ArrayList<>();
            String provinceName = "";
            String districtName = "";
            String communeName = "";

            try{
                int eventType = -1;
                while(eventType != XmlPullParser.END_DOCUMENT) {
                    eventType=parser.next();
                    switch(eventType)
                    {
                        case XmlPullParser.START_DOCUMENT:
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            nodeName = parser.getName();
                            if(nodeName.equals("SimpleData") && parser.getAttributeValue(0)
                                    .equals("NAME_1")){
                                provinceName = parser.nextText();
                            } else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0)
                                    .equals("NAME_2")){
                                districtName = parser.nextText();
                            } else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0)
                                    .equals("NAME_3")){
                                communeName = parser.nextText();
                            } else if(nodeName.equals("coordinates")){
                                nodeText = parser.nextText();
                                String[] coordinatePairs = nodeText.split(" ");
                                for(String coord : coordinatePairs){
                                    boundaries.add(new CLocation(Double.parseDouble(coord.split(",")[1]),
                                            Double.parseDouble(coord.split(",")[0])));
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            nodeName=parser.getName();
                            if(nodeName.equals("Placemark")){
                                List<CLocation> temp = new ArrayList<>(boundaries);
                                Area commune = provinces
                                        .get(provinceName)
                                        .getChildAreas()
                                        .get(districtName)
                                        .getChildAreas()
                                        .get(communeName);
                                if(commune != null){
                                    commune.setBoundaries(temp);
                                }

                                boundaries.clear();
                            }
                            break;
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }

            redPlaceFragment.getMapArea(vietnam);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AreaLabel implements Serializable {
        private String level;
        private String name;
        private String color;
        private String numberF0;
    }
}