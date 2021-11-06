package com.example.covimap.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

import butterknife.BindView;


public class NewRecordActivity extends Fragment {
    private MapManager mapManager;
    private MainActivity main;
    private Context context;
    private static View view;

    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheetLayout;
    private BottomSheetBehavior bottomSheetBehavior;

    private CLocation lastLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            view = (View)inflater.inflate(R.layout.new_record_activity, null);
            prepareWidget();

            requestCurrentLocation();
            MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.ggmap_api);
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

                    Toast.makeText(context, "Location: " + currentLocation, Toast.LENGTH_SHORT).show();
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

    //UI---------------------------------------------
    public void prepareWidget(){
        bottomSheetLayout = (LinearLayout) view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setPeekHeight(175, true);
    }
}
