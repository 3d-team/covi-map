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
import com.example.covimap.model.Location;
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
    private final List<Location> path = new ArrayList<>();
    private Location lastLocation;
    private double distance = 0;
    private String createdTime = "";
    private long PauseOffSet = 0;
    private final SimpleDateFormat sdf = new SimpleDateFormat(Config.DATETIME_FORMAT, Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.new_record_activity, null);

            mappingUIComponent();
            subscribeEventButton();
            pluginGGMap();
        } catch (InflateException e){
            Log.e("NEW RECORD ERROR", "onCreateView", e);
        }
        return view;
    }

    public void mappingUIComponent(){
        distanceTextView = view.findViewById(R.id.distance_text_view);
        timeTextView = view.findViewById(R.id.time_text_view);
        locateCurrentBtn = view.findViewById(R.id.locate_position_btn);
        recordBtn = view.findViewById(R.id.record_btn);
        saveRecordBtn = view.findViewById(R.id.save_record_btn);
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
            Route route = Route.builder()
                    .path(path)
                    .period(timeTextView.getText().toString())
                    .distance(String.format("%.2f", distance))
                    .createdDay(createdTime)
                    .startAddress(getAddress(path.get(0)))
                    .endAddress(getAddress(path.get(path.size() - 1)))
                    .build();
            RouteRepository routeRepository = new RouteRepository();
            routeRepository.addByPhoneNumber(phoneNumber, route);

            resetStateApp();
            notifyEndRecording(timeTextView.getText().toString());
        });
    }

    private void resetStateApp() {
        statusRecord = StatusRecord.NOT_START;
        path.removeAll(path);
        recordBtn.setText("Record");
        
        mapManager.reset();

        resetTime();
        resetDistance();
    }

    private void resetDistance() {
        distance = 0;
        distanceTextView.setText("0.00 km");
        PauseOffSet = 0;
    }

    private void resetTime() {
        timeTextView.setBase(SystemClock.elapsedRealtime());
        timeTextView.stop();
    }

    private void notifyEndRecording( String period) {
        Toast.makeText(context, "Time :" + period + ", " +
                "Distance: " + String.format("%.2f km",distance) +
                "Created Time: " + createdTime, Toast.LENGTH_SHORT).show();
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

                Location currentLocation = new Location(
                        intent.getDoubleExtra("latitude", 0f),
                        intent.getDoubleExtra("longitude", 0f));

                if (statusRecord == StatusRecord.RUNNING){
                    recordNewPosition(currentLocation);
                }

                if (lastLocation != null) {
                    currentMarker.remove();
                }

                lastLocation = currentLocation;
                drawCurrentPosition(currentLocation);
            }
        };

        main.registerReceiver(locationReceiver, new IntentFilter("CURRENT_LOCATION"));
    }

    private void recordNewPosition(Location currentLocation) {
        if (lastLocation == null) {
            return;
        }
        mapManager.drawRoute(lastLocation, currentLocation);
        distance += Location.getDistance(lastLocation, currentLocation);
        distanceTextView.setText(String.format("%.2f km", distance));
        path.add(lastLocation);
    }

    private void drawCurrentPosition(Location currentLocation) {
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

    public String getAddress(Location location) {
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
