package com.example.covimap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.covimap.service.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private BottomNavigationView bottomNavigationView;
    FloatingActionButton floatingActionButton;
    private boolean isShow = true;
    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheetLayout;
    BottomSheetBehavior bottomSheetBehavior;

    private GoogleMap map;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mappingWidget();
        requestCurrentLocation();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.ggmap_api);
        mapFragment.getMapAsync(this);
    }

    private void mappingWidget(){
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setBackground(null);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.start_record_btn);
        bottomSheetLayout = (LinearLayout) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setPeekHeight(175, true);
    }

    private void requestCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            startLocationService();
        }
    }

    private void startLocationService() {
        registerLocationReceiver();

        Intent intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);
    }

    private void registerLocationReceiver() {
        BroadcastReceiver locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("CURRENT_LOCATION")) {
                    double lat = intent.getDoubleExtra("latitude", 0f);
                    double longitude = intent.getDoubleExtra("longitude", 0f);
                    if (map != null) {
                        LatLng latLng = new LatLng(lat, longitude);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        if (marker != null)
                            marker.setPosition(latLng);
                        else
                            marker = map.addMarker(markerOptions);
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                    }
                    Toast.makeText(MainActivity.this, "Latitude is: " + lat + ", Longitude is " + longitude, Toast.LENGTH_SHORT).show();
                }
            }
        };

        registerReceiver(locationReceiver, new IntentFilter("CURRENT_LOCATION"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                Toast.makeText(this, "Required GPS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        map.setOnMapClickListener(latLng -> {
            if(isShow){
                bottomNavigationView.setVisibility(View.GONE);
                bottomSheetLayout.setVisibility(View.GONE);
                isShow = false;
            }
            else {
                bottomNavigationView.setVisibility(View.VISIBLE);
                bottomSheetLayout.setVisibility(View.VISIBLE);
                isShow = true;
            }
        });
    }
}