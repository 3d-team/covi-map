package com.example.covimap.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.manager.DirectionMode;
import com.example.covimap.manager.MapManager;
import com.example.covimap.model.CLocation;
import com.example.covimap.service.LocationService;
import com.google.android.gms.maps.MapFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;


public class NewRecordActivity extends Fragment {
    private MapManager mapManager;
    private MainActivity main;
    private Context context;
    private static View view;

    private SearchView searchLocationEdt;
    private TextView resultTextView;

    private FloatingActionButton locateCurrentBtn;
    private TextView distanceTextView;
    private TextView timeTextView;
    private Button stopRecordBtn;
    private Button recordBtn;
    private Button saveRecordBtn;

    private CLocation lastLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            view = (View)inflater.inflate(R.layout.new_record_activity, null);
            prepareWidget();

            requestCurrentLocation();
            MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.record_ggmap_api);
            mapManager = new MapManager();
            mapFragment.getMapAsync(mapManager);
        }
        catch (InflateException e){
            Log.e("NEW RECORD ERROR", "onCreateView", e);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            context = getActivity();
            main = (MainActivity) getActivity();
        }
        catch (IllegalStateException e){
            throw new IllegalStateException("Error");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void requestCurrentLocation() {
        if (main.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            registerLocationReceiver();
            startLocationService();
        }
    }

    private void registerLocationReceiver() {
        BroadcastReceiver locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("CURRENT_LOCATION")) {
                    CLocation currentLocation = new CLocation(
                            intent.getDoubleExtra("latitude", 0f),
                            intent.getDoubleExtra("longitude", 0f));

                    if (lastLocation != null) {
                        mapManager.drawRoute(lastLocation, currentLocation);
                    }
                    lastLocation = currentLocation;
                    mapManager.addMarker(currentLocation, "Here");
                    mapManager.animateCamera(currentLocation);

//                    Toast.makeText(context, "Location: " + currentLocation, Toast.LENGTH_SHORT).show();
                }
            }
        };

        main.registerReceiver(locationReceiver, new IntentFilter("CURRENT_LOCATION"));
    }

    private void startLocationService() {
        Intent intent = new Intent(main, LocationService.class);
        main.startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                Toast.makeText(context, "Required GPS", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //Listener for searchLocationBtn
    private View.OnClickListener searchLocationBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
        }
    };
    //Listener for locateCurrentbtn
    private View.OnClickListener locateCurrentBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(main != null) {
                requestCurrentLocation();
                MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.record_ggmap_api);
                mapManager = new MapManager();
                mapFragment.getMapAsync(mapManager);
            }
        }
    };
    //Listener for stopRecordBtn
    private View.OnClickListener stopRecordBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (statusRecord){
                case NOT_START:
                    break;
                case RUNNING:
                    statusRecord = StatusRecord.NOT_START;
                    recordBtn.setText("Record");
                    break;
                case PAUSED:
                    statusRecord = StatusRecord.NOT_START;
                    recordBtn.setText("Record");
                    break;
            }
        }
    };
    //Listener for recordBtn
    private enum StatusRecord{
        NOT_START, RUNNING, PAUSED
    }
    private static StatusRecord statusRecord = StatusRecord.NOT_START;
    private View.OnClickListener recordBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (statusRecord){
                case NOT_START:
                    stopRecordBtn.setVisibility(View.VISIBLE);
                    saveRecordBtn.setVisibility(View.VISIBLE);
                    statusRecord = StatusRecord.RUNNING;
                    recordBtn.setText("Pause");
                    break;
                case RUNNING:
                    statusRecord = StatusRecord.PAUSED;
                    recordBtn.setText("Resume");
                    break;
                case PAUSED:
                    statusRecord = StatusRecord.RUNNING;
                    recordBtn.setText("Pause");
                    break;
            }
        }
    };
    //Listener for saveRecordBtn
    private View.OnClickListener saveRecordBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {

        }
    };
    //Listener for searchLoctionEdt
    private String resultLocationStr;
    private Address address;
    private SearchView.OnQueryTextListener searchLocationEdtOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
////                        CLocation locationSearch = new CLocation(address.getLatitude(), address.getLongitude());
////                        mapManager.addMarker(locationSearch, "Search");
////                        mapManager.animateCamera(locationSearch);
            return false;
        }
        private CountDownTimer countDownTimer;
        @Override
        public boolean onQueryTextChange(String s) {
            if(countDownTimer != null){
                countDownTimer.cancel();
            }
            countDownTimer = new CountDownTimer(500, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    String locationName = searchLocationEdt.getQuery().toString();
                    List<Address> geoResults = null;

                    if(locationName.equals("")){
                        resultTextView.setVisibility(View.GONE);
                        resultTextView.setText("");
                    }
                    else if (locationName != null) {
                        Geocoder geocoder = new Geocoder(NewRecordActivity.view.getContext(), Locale.getDefault());
                        try {
                            geoResults = geocoder.getFromLocationName(locationName, 1);
                            if (geoResults.size() > 0) {
                                address = geoResults.get(0);
                                Log.d("LocationResultSetSize:", "" + geoResults.size());
                                resultLocationStr = "";
                                int max = address.getMaxAddressLineIndex();
                                for (int i = 0; i <= max; i++) {
                                    resultLocationStr += address.getAddressLine(i);
                                }
                                resultTextView.setText(resultLocationStr);
                                resultTextView.setVisibility(View.VISIBLE);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        resultTextView.setVisibility(View.GONE);
                        resultTextView.setText("");
                    }
                }
            };
            countDownTimer.start();
            return false;
        }
    };
    // Listener for resultTextView
    private View.OnClickListener resultTextViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setVisibility(View.INVISIBLE);
                CLocation locationSearch = new CLocation(address.getLatitude(), address.getLongitude());
                mapManager.addMarker(locationSearch, "Search");
                mapManager.animateCamera(locationSearch);
        }
    };

    //Prepare for UI---------------------------------------------
    public void prepareWidget(){
        searchLocationEdt = (SearchView) view.findViewById(R.id.search_location_edt);
        resultTextView = (TextView) view.findViewById(R.id.result_location_text_view);
        distanceTextView = (TextView) view.findViewById(R.id.distance_text_view);
        timeTextView = (TextView) view.findViewById(R.id.time_text_view);

        locateCurrentBtn = (FloatingActionButton) view.findViewById(R.id.locate_position_btn);
        stopRecordBtn = (Button) view.findViewById(R.id.stop_record_btn);
        recordBtn = (Button) view.findViewById(R.id.record_btn);
        saveRecordBtn = (Button) view.findViewById(R.id.save_record_btn);

        locateCurrentBtn.setOnClickListener(locateCurrentBtnListener);
        stopRecordBtn.setOnClickListener(stopRecordBtnListener);
        recordBtn.setOnClickListener(recordBtnListener);
        saveRecordBtn.setOnClickListener(saveRecordBtnListener);

        searchLocationEdt.setOnQueryTextListener(searchLocationEdtOnQueryTextListener);
        resultTextView.setOnClickListener(resultTextViewListener);
    }

}
