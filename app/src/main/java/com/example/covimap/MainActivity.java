package com.example.covimap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.example.covimap.manager.MapManager;
import com.example.covimap.model.CLocation;
import com.example.covimap.service.LocationService;
import com.google.android.gms.maps.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;

public class MainActivity extends FragmentActivity {
    FloatingActionButton floatingActionButton;
    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheetLayout;
    BottomSheetBehavior bottomSheetBehavior;

    private MapManager mapManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mappingWidget();
        requestCurrentLocation();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.ggmap_api);
        mapManager = new MapManager();
        mapFragment.getMapAsync(mapManager);
    }

    private void mappingWidget(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setBackground(null);
        floatingActionButton = findViewById(R.id.start_record_btn);
        bottomSheetLayout = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setPeekHeight(175, true);
    }

    private void requestCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
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

                    mapManager.reset();
                    mapManager.addMarker(currentLocation, "Here");
                    mapManager.animateCamera(currentLocation);

                    Toast.makeText(MainActivity.this, "Location: " + currentLocation, Toast.LENGTH_SHORT).show();
                }
            }
        };

        registerReceiver(locationReceiver, new IntentFilter("CURRENT_LOCATION"));
    }

    private void startLocationService() {
        Intent intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);
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
}