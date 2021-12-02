package com.example.covimap.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.model.AppStatus;
import com.example.covimap.model.Area;
import com.example.covimap.model.CLocation;
import com.example.covimap.model.MyAccount;
import com.example.covimap.utils.Validator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class PrepareActivity extends Activity {
    private AppStatus appStatus;
    private MyAccount myAccount;
    List<Area> provinces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prepare_layout);
        prepareStatus();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//            try {
//                FileInputStream inputStream = openFileInput("area.dat");
//                ObjectInputStream os = new ObjectInputStream(inputStream);
//                Area area = (Area) os.readObject();
//                os.close();
//                area.toString();
//            }
//            catch (Exception e){}
//    }
//
//
//    private class ReadXMLFileLevel1 extends AsyncTask<Void, Void, List<Area>> {
//        @Override
//        protected List<Area> doInBackground(Void... voids) {
//            int xmlResFile = R.xml.gadm36_1;
//            XmlPullParser parser = getResources().getXml(xmlResFile);
//            String nodeName;
//            String nodeText;
//            Area province = new Area();
//            List<Area> provinces = new ArrayList<>();
//            List<CLocation> boundaries = new ArrayList<>();
//            try{
//                int eventType = -1;
//                while(eventType != XmlPullParser.END_DOCUMENT)
//                {
//                    eventType=parser.next();
//                    switch(eventType)
//                    {
//                        case XmlPullParser.START_DOCUMENT:
//                            break;
//                        case XmlPullParser.END_DOCUMENT:
//                            break;
//                        case XmlPullParser.START_TAG:
//                            nodeName = parser.getName();
//                            if(nodeName.equals("Placemark")){
//                                province.setLevel("1");
//                            }
//                            else if(nodeName.equals("color")){
//                                province.setColor(parser.nextText());
//                            }
//                            else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_1")){
//                                province.setName(parser.nextText());
//                            }
//                            else if(nodeName.equals("coordinates")){
//                                nodeText = parser.nextText();
//                                String[] coordinatePairs = nodeText.split(" ");
//                                for(String coord : coordinatePairs){
//                                    boundaries.add(new CLocation(Double.parseDouble(coord.split(",")[1]),
//                                            Double.parseDouble(coord.split(",")[0])));
//                                }
//                            }
//                            break;
//                        case XmlPullParser.END_TAG:
//                            nodeName=parser.getName();
//                            if(nodeName.equals("Placemark")){
//                                List<CLocation> temp = new ArrayList<>(boundaries);
//                                province.setBoundaries(temp);
//                                provinces.add(province);
//
//                                province = new Area();
//                                boundaries.clear();
//                            }
//                            break;
//                    }
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (XmlPullParserException e) {
//                e.printStackTrace();
//            }
//            return provinces;
//        }
//    }
//
//    private class ReadXMLFileLevel2 extends AsyncTask<List<Area>, Void, List<Area>> {
//        protected List<Area> doInBackground(List<Area>... lists) {
//            int xmlResFile = R.xml.gadm36_2;
//            int i = 0;
//            XmlPullParser parser = getResources().getXml(xmlResFile);
//            List<Area> provinces = lists[0];
//            String nodeName;
//            String nodeText;
//            String provinceName = "";
//            Area district = new Area();
//            List<Area> districts = new ArrayList<>();
//            List<CLocation> boundaries = new ArrayList<>();
//
//            try{
//                int eventType = -1;
//                while(eventType != XmlPullParser.END_DOCUMENT)
//                {
//                    eventType=parser.next();
//                    switch(eventType)
//                    {
//                        case XmlPullParser.START_DOCUMENT:
//                            break;
//                        case XmlPullParser.END_DOCUMENT:
//                            for(Area p : provinces){// Add districts to Yen Bai
//                                if(p.getName().equals(provinceName)){
//                                    p.setChildAreas(districts);
//                                }
//                            }
//                            break;
//                        case XmlPullParser.START_TAG:
//                            nodeName = parser.getName();
//                            if(nodeName.equals("Placemark")){
//                                district.setLevel("2");
//                            }
//                            else if(nodeName.equals("color")){
//                                district.setColor(parser.nextText());
//                            }
//                            else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_1")){
//                                String temp = parser.nextText();
//                                if(!provinceName.isEmpty() && !temp.equals(provinceName)){
//                                    List<Area> temp_districts = new ArrayList<>(districts);
//                                    for(Area p : provinces){
//                                        if(p.getName().equals(provinceName)){
//                                            p.setChildAreas(temp_districts);
//                                        }
//                                    }
//                                    districts.clear();
//                                }
//                                provinceName = temp;
//                            }
//                            else if(nodeName.equals("SimpleData") && parser.getAttributeValue(0).equals("NAME_2")){
//                                district.setName(parser.nextText());
//                            }
//                            else if(nodeName.equals("coordinates")){
//                                nodeText = parser.nextText();
//                                String[] coordinatePairs = nodeText.split(" ");
//                                for(String coord : coordinatePairs){
//                                    boundaries.add(new CLocation(Double.parseDouble(coord.split(",")[1]),
//                                            Double.parseDouble(coord.split(",")[0])));
//                                }
//                            }
//                            break;
//                        case XmlPullParser.END_TAG:
//                            nodeName=parser.getName();
//                            if(nodeName.equals("Placemark")){
//                                List<CLocation> temp = new ArrayList<>(boundaries);
//                                district.setBoundaries(temp);
//                                districts.add(district);
//
//                                district = new Area();
//                                boundaries.clear();
//                            }
//                            break;
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (XmlPullParserException e) {
//                e.printStackTrace();
//            }
//            for(Area area: provinces){
//                area.toString();
//            }
//
////            Area vietnam = new Area("0", "VietNam", "000000", new ArrayList<>(), provinces);
////            try{
////                FileOutputStream outputStream = openFileOutput("area.dat", MODE_PRIVATE);
////                vietnam.writeStatusToFile(outputStream);
////            }
////            catch (Exception e){}
//
//            return provinces;
//        }
//    }

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
//            setContentView(R.layout.prepare_layout);

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
                intent.putExtra("AppStatus", appStatus);
                startActivity(intent);
                finish();
            }
        }
    }


}
