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
import android.os.SystemClock;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.manager.MapManager;
import com.example.covimap.model.CLocation;
import com.example.covimap.model.Route;
import com.example.covimap.repository.RouteRepository;
import com.example.covimap.service.LocationService;
import com.example.covimap.service.RecordingFragmentCallBacks;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RecordingFragment extends Fragment implements RecordingFragmentCallBacks {
    private String phoneNumber;
    private MapManager mapManager;
    private MainActivity main;
    private Context context;
    private static View view;

    private FloatingActionButton locateCurrentBtn;
    private TextView distanceTextView;
    private Chronometer timeTextView;
    private Button recordBtn;
    private Button saveRecordBtn;

    private Marker currentMarker = null;
    private List<CLocation> path = new ArrayList<>();
    private CLocation lastLocation;
    private double distance = 0;
    private String createdTime = "";
    private long PauseOffSet = 0;
    private SimpleDateFormat sdf = new SimpleDateFormat(Config.DATETIME_FORMAT, Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            view = (View)inflater.inflate(R.layout.new_record_activity, null);

            mappingUIComponent();
            subscribeEventButton();
            pluginGGMap();
        } catch (InflateException e){
            Log.e("NEW RECORD ERROR", "onCreateView", e);
        }
        return view;
    }

    public void mappingUIComponent(){
        distanceTextView = (TextView) view.findViewById(R.id.distance_text_view);
        timeTextView = (Chronometer) view.findViewById(R.id.time_text_view);
        locateCurrentBtn = (FloatingActionButton) view.findViewById(R.id.locate_position_btn);
        recordBtn = (Button) view.findViewById(R.id.record_btn);
        saveRecordBtn = (Button) view.findViewById(R.id.save_record_btn);
    }

    private void subscribeEventButton() {
        locateCurrentBtn.setOnClickListener(v -> {
            if(main != null) {
                requestCurrentLocation();
            }
        });

        recordBtn.setOnClickListener(v -> {
            switch (statusRecord){
                case NOT_START:
                    requestCurrentLocation();
                    timeTextView.setBase(SystemClock.elapsedRealtime()-PauseOffSet);
                    timeTextView.start();
                    createdTime = sdf.format(Calendar.getInstance().getTime());

                    saveRecordBtn.setVisibility(View.VISIBLE);
                    statusRecord = StatusRecord.RUNNING;
                    recordBtn.setText(R.string.paused_button);
                    break;
                case RUNNING:
                    timeTextView.stop();
                    PauseOffSet = SystemClock.elapsedRealtime() - timeTextView.getBase();

                    statusRecord = StatusRecord.PAUSED;
                    recordBtn.setText(R.string.resume_button);
                    break;
                case PAUSED:
                    timeTextView.setBase(SystemClock.elapsedRealtime()-PauseOffSet);
                    timeTextView.start();

                    statusRecord = StatusRecord.RUNNING;
                    recordBtn.setText(R.string.paused_button);
                    break;
            }
        });

        saveRecordBtn.setOnClickListener(v -> {
            String period = timeTextView.getText().toString();
            String startAddress = getAddress(path.get(0));
            String endAddress = getAddress(path.get(path.size() - 1));
            Route route = Route.builder()
                    .path(path)
                    .period(period)
                    .distance(String.format("%.2f",distance))
                    .createdDay(createdTime)
                    .startAddress(startAddress)
                    .endAddress(endAddress)
                    .build();
            RouteRepository routeRepository = new RouteRepository();
            routeRepository.addByPhoneNumber(phoneNumber, route);

            Toast.makeText(context, "Time :" + period + ", Distance: " + String.format("%.2f km",distance) + "Created Time: " + createdTime, Toast.LENGTH_SHORT).show();
            path.removeAll(path);
            mapManager.reset();
            statusRecord = StatusRecord.NOT_START;
            recordBtn.setText("Record");
            timeTextView.setBase(SystemClock.elapsedRealtime());
            distanceTextView.setText("0.00 km");
            PauseOffSet = 0;
            timeTextView.stop();
            distance = 0;
        });
    }

    private void pluginGGMap() {
        MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.ggmap_api);
        mapManager = new MapManager();
        mapFragment.getMapAsync(mapManager);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            context = getActivity();
            main = (MainActivity) getActivity();
        } catch (IllegalStateException e){
            throw new IllegalStateException("Error");
        }
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
                if (!intent.getAction().equals("CURRENT_LOCATION")) {
                    return;
                }

                CLocation currentLocation = new CLocation(
                        intent.getDoubleExtra("latitude", 0f),
                        intent.getDoubleExtra("longitude", 0f));

                if (statusRecord == StatusRecord.RUNNING){
                    recordNewPosition(currentLocation);
                };

                if (lastLocation != null) {
                    currentMarker.remove();
                }

                lastLocation = currentLocation;
                drawCurrentPosition(currentLocation);
            }
        };

        main.registerReceiver(locationReceiver, new IntentFilter("CURRENT_LOCATION"));
    }

    private void recordNewPosition(CLocation currentLocation) {
        if (lastLocation != null) {
            mapManager.drawRoute(lastLocation, currentLocation);
            distance += CLocation.getDistance(lastLocation, currentLocation);
            distanceTextView.setText(String.format("%.2f km", distance));
        }

        path.add(lastLocation);
    }

    private void drawCurrentPosition(CLocation currentLocation) {
        currentMarker = mapManager.addMarker(currentLocation, "Here");
        mapManager.animateCamera(currentLocation);
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

    private enum StatusRecord{
        NOT_START, RUNNING, PAUSED
    }

    private static StatusRecord statusRecord = StatusRecord.NOT_START;

    public String getAddress(CLocation location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            return obj.getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void getPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
