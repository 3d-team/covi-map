package com.example.covimap.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.example.covimap.model.Area;
import com.example.covimap.model.AreaLabel;
import com.example.covimap.model.CLocation;
import com.example.covimap.model.MyAccount;
import com.example.covimap.service.MainCallbacks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements MainCallbacks {
    private BottomNavigationView bottomNav;
    private AppStatus appStatus;
    private MyAccount myAccount;

    private NewRecordActivity newRecordActivity;
    private DirectActivity directActivity;
    private HistoryJourneyActivity historyJourneyActivity;
    private EpidemicZoneActivity epidemicZoneActivity;
    private PersonalActivity personalActivity;
    private Fragment currentFragment;
    private Area vietnam;

    public MainActivity() {
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        appStatus = (AppStatus) intent.getSerializableExtra("AppStatus");
        myAccount = (MyAccount)intent.getSerializableExtra("AccountData");

        prepareStatus();
        setContentView(R.layout.activity_main);

        newRecordActivity = new NewRecordActivity();
        directActivity = new DirectActivity();
        historyJourneyActivity = new HistoryJourneyActivity();
        epidemicZoneActivity = new EpidemicZoneActivity();
        personalActivity = new PersonalActivity();

        newRecordActivity.getPhoneNumber(myAccount.getPhoneNumber());
        currentFragment = newRecordActivity;

        bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_new_record:
                        newRecordActivity.getPhoneNumber(myAccount.getPhoneNumber());
                        currentFragment = newRecordActivity;
                        break;
                    case R.id.nav_direct_location:
                        currentFragment = directActivity;
                        break;
                    case R.id.nav_history_record:
                        historyJourneyActivity.getPhoneNumber(myAccount.getPhoneNumber());
                        currentFragment = historyJourneyActivity;
                        break;
                    case R.id.nav_epidemic_zone:
                        if(vietnam != null){epidemicZoneActivity.getMapArea(vietnam);}
                        currentFragment = epidemicZoneActivity;
                        break;
                    case R.id.nav_peronal:
                        personalActivity.setMyAccount(myAccount);
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
        LoadBoundariesData loadBoundariesData = new LoadBoundariesData();
        Thread thread = new Thread(loadBoundariesData);
        thread.start();
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
        Log.d("STATUS:", "PAUSE");
    }

    public void prepareStatus(){
        if(appStatus != null){
            Log.d("MyLog", appStatus.toString());
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

    @Override
    public void onChangeLoginStatus(boolean islogged) {
        if(appStatus == null){
            return;
        }
        appStatus.setLogged(islogged);
        finish();
    }

    public static String getStringByIdName(Context context, String idName) {
        Resources res = context.getResources();
        return res.getString(res.getIdentifier(idName, "string", context.getPackageName()));
    }

    private class LoadBoundariesData implements Runnable{
        @Override
        public void run() {
//            vietnam = new Area("0", "VietNam", Config.GRAY_ZONE_COLOR, null,null, null);
//            DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("VietNam");
//            data.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    vietnam = snapshot.getValue(Area.class);
////                    Log.d("MyLog", vietnam.toString());
//                    getProvince(vietnam.getChildAreas());
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
            SharedPreferences preferences = getSharedPreferences(Config.SHARE_PREF_NAME, Activity.MODE_PRIVATE);
            String key = "VietNamJSON";
            if(preferences != null && preferences.contains(key)){
                String vietnamJSON = preferences.getString(key, "");
                if(!vietnamJSON.isEmpty()){
                    Gson gson = new Gson();
                    vietnam = gson.fromJson(vietnamJSON, Area.class);
                    getProvince(vietnam.getChildAreas());
                }
            }
        }

        private void getProvince(HashMap<String, Area> provinces){
            if(provinces == null) {provinces = new HashMap<>(); Log.d("MyTag", "NULL POINTER");}

            XmlPullParser parser = getResources().getXml(R.xml.gadm36_1);

            String nodeName, nodeText;
            String provinceName = "";
            List<CLocation> boundaries = new ArrayList<>();

            try{
                int eventType = -1;
                while(eventType != XmlPullParser.END_DOCUMENT)
                {
                    eventType = parser.next();
                    switch(eventType)
                    {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            nodeName = parser.getName();
                            if(nodeName.equals("Placemark")){
//                                province.setLevel("1");
                            }
                            else if(nodeName.equals("color")){
//                                province.setColor(Config.GRAY_ZONE_COLOR);
//                                province.setNumberF0("0");
                            }
                            else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_1")){
                                provinceName = parser.nextText();
                            }
                            else if(nodeName.equals("coordinates")){
                                nodeText = parser.nextText();
                                String[] coordinatePairs = nodeText.split(" ");
                                for(String coord : coordinatePairs){
                                    boundaries.add(new CLocation(Double.parseDouble(coord.split(",")[1]),
                                            Double.parseDouble(coord.split(",")[0])));
                                }
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            nodeName = parser.getName();
                            if(nodeName.equals("Placemark")){
                                List<CLocation> temp = new ArrayList<>(boundaries);

                                if(provinces.get(provinceName) != null){
                                    provinces.get(provinceName).setBoundaries(temp);
                                }
//                                AreaLabel areaLabel = new AreaLabel(province);
//                                FirebaseDatabase.getInstance().getReference().child("VietNam").child("childAreas").child(province.getName()).setValue(areaLabel);
//                                province = new Area();
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
//            return provinces;
        }

        private void getDistrict(HashMap<String, Area> provinces){
            XmlPullParser parser = getResources().getXml(R.xml.gadm36_2);

            String nodeName, nodeText;
//            Area district = new Area();
            List<CLocation> boundaries = new ArrayList<>();
            String provinceName = "";
            String districtName = "";

            try{
                int eventType = -1;
                while(eventType != XmlPullParser.END_DOCUMENT)
                {
                    eventType=parser.next();
                    switch(eventType)
                    {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            nodeName = parser.getName();
                            if(nodeName.equals("Placemark")){
//                                district.setLevel("2");
                            }
                            else if(nodeName.equals("color")){
//                                district.setColor(Config.GRAY_ZONE_COLOR);
//                                district.setNumberF0("0");
                            }
                            else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_1")){
                                provinceName = parser.nextText();
                            }
                            else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_2")){
//                                district.setName(parser.nextText());
                                districtName = parser.nextText();
                            }
                            else if(nodeName.equals("coordinates")){
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
                                Area district = provinces.get(provinceName).getChildAreas().get(districtName);
                                if(district != null){
                                    district.setBoundaries(temp);
                                }

//                                AreaLabel areaLabel = new AreaLabel(district);
//                                FirebaseDatabase.getInstance().getReference().child("VietNam").child("childAreas").child(provinceName).child("childAreas").child(district.getName()).setValue(areaLabel);
//                                district = new Area();
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
//            Area commune = new Area();
            List<CLocation> boundaries = new ArrayList<>();
            String provinceName = "";
            String districtName = "";
            String communeName = "";

            try{
                int eventType = -1;
                while(eventType != XmlPullParser.END_DOCUMENT)
                {
                    eventType=parser.next();
                    switch(eventType)
                    {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.END_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            nodeName = parser.getName();
                            if(nodeName.equals("Placemark")){
//                                commune.setLevel("3");
                            }
                            else if(nodeName.equals("color")){
//                                commune.setColor(Config.GRAY_ZONE_COLOR);
//                                commune.setNumberF0("0");
                            }
                            else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_1")){
                                provinceName = parser.nextText();
                            }
                            else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_2")){
                                districtName = parser.nextText();
                            }
                            else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_3")){
//                                commune.setName(parser.nextText());
                                communeName = parser.nextText();
                            }
                            else if(nodeName.equals("coordinates")){
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
                                Area commune = provinces.get(provinceName).getChildAreas().get(districtName).getChildAreas().get(communeName);
                                if(commune != null){
                                    commune.setBoundaries(temp);
                                }
//                                AreaLabel areaLabel = new AreaLabel(commune);
//                                FirebaseDatabase.getInstance().getReference().child("VietNam")
//                                        .child("childAreas").child(provinceName)
//                                        .child("childAreas").child(districtName)
//                                        .child("childAreas").child(commune.getName()).setValue(areaLabel);

//                                commune = new Area();
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
            Log.d("MyLog", "--- DONE! ---");
            epidemicZoneActivity.getMapArea(vietnam);
        }
    }
}