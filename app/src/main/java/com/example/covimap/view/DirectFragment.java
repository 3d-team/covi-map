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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SearchView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class DirectFragment extends Fragment {
    private MapManager mapManager;
    private MainActivity main;
    private Context context;
    private static View view;

    private SearchView searchSrcLocationEdt;
    private TextView resultSrcTextView;
    private TextView yourLocationSrcTextView, yourLocationDestTextView;
    private ImageView srcImgView, destImgView, swapSrcDest;

    private SearchView searchDestLocationEdt;
    private TextView resultDestTextView;
    private FloatingActionButton locateCurrentBtn;
    private boolean isSwap = false;

    private RadioGroup directMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            view = inflater.inflate(R.layout.direct_activity, null);

            mappingUIComponent();
            subscribeEventButton();
            pluginGGMap();
        } catch (InflateException e) {
            Log.e("NEW RECORD ERROR", "onCreateView", e);
        }

        return view;
    }

    public void mappingUIComponent(){
        directMode = view.findViewById(R.id.direction_mode_group);
        searchSrcLocationEdt = view.findViewById(R.id.search_src_location_edt);
        resultSrcTextView = view.findViewById(R.id.result_src_location_text_view);
        searchDestLocationEdt = view.findViewById(R.id.search_dest_location_edt);
        resultDestTextView = view.findViewById(R.id.result_dest_location_text_view);
        locateCurrentBtn = view.findViewById(R.id.locate_position_btn);
        yourLocationSrcTextView = view.findViewById(R.id.your_location_src_text_view);
        yourLocationDestTextView = view.findViewById(R.id.your_location_dest_text_view);
        srcImgView = view.findViewById(R.id.src_icon);
        destImgView = view.findViewById(R.id.dest_icon);
        swapSrcDest = view.findViewById(R.id.swap_src_dest);
    }

    private void subscribeEventButton() {
        directMode.setOnCheckedChangeListener((radioGroup, i) -> {
            if (srcLocation == null || destLocation == null) {
                Toast.makeText(context, "Lack of start address or target address!", Toast.LENGTH_LONG).show();
                return;
            }

            switch (i){
                case R.id.walk_radiobutton:
                    mapManager.findRouteBetweenTwoLocations(srcLocation, destLocation, DirectionMode.WALKING);
                    break;
                case R.id.motobike_radiobutton:
                    mapManager.findRouteBetweenTwoLocations(srcLocation, destLocation, DirectionMode.BICYCLING);
                    break;
                case R.id.car_radiobutton:
                    mapManager.findRouteBetweenTwoLocations(srcLocation, destLocation, DirectionMode.DRIVING);
                    break;
                case R.id.bus_radiobutton:
                    mapManager.findRouteBetweenTwoLocations(srcLocation, destLocation, DirectionMode.TRANSIT);
                    break;
            }
        });

        locateCurrentBtn.setOnClickListener(v -> requestCurrentLocation());

        searchSrcLocationEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
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
                        if(isSwap) return;
                        String locationName = searchSrcLocationEdt.getQuery().toString();
                        List<Address> geoResults;

                        if(locationName.equals("")){
                            resultSrcTextView.setVisibility(View.GONE);
                            resultSrcTextView.setText("");
                        }
                        else {
                            Geocoder geocoder = new Geocoder(DirectFragment.view.getContext(), Locale.getDefault());
                            try {
                                geoResults = geocoder.getFromLocationName(locationName, 1);
                                if (geoResults.size() > 0) {
                                    srcAddress = geoResults.get(0);
                                    Log.d("LocationResultSetSize:", "" + geoResults.size());
                                    srcAddressStr = "";
                                    int max = srcAddress.getMaxAddressLineIndex();
                                    for (int i = 0; i <= max; i++) {
                                        srcAddressStr += srcAddress.getAddressLine(i);
                                    }
                                    resultSrcTextView.setText(srcAddressStr);
                                    resultSrcTextView.setVisibility(View.VISIBLE);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                countDownTimer.start();
                return false;
            }
        });

        resultSrcTextView.setOnClickListener(view -> {
            view.setVisibility(View.GONE);
            srcLocation = new CLocation(srcAddress.getLatitude(), srcAddress.getLongitude());
            mapManager.addMarker(srcLocation, "Search");
            mapManager.animateCamera(srcLocation);
            searchSrcLocationEdt.clearFocus();
        });

        searchDestLocationEdt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private CountDownTimer countDownTimer;

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(countDownTimer != null){
                    countDownTimer.cancel();
                }
                countDownTimer = new CountDownTimer(500, 1000) {
                    @Override
                    public void onTick(long l) {}

                    @Override
                    public void onFinish() {
                        if(isSwap) return;
                        String locationName = searchDestLocationEdt.getQuery().toString();
                        List<Address> geoResults;

                        if(locationName.equals("")){
                            resultDestTextView.setVisibility(View.GONE);
                            resultDestTextView.setText("");
                        }
                        else {
                            Geocoder geocoder = new Geocoder(DirectFragment.view.getContext(), Locale.getDefault());
                            try {
                                geoResults = geocoder.getFromLocationName(locationName, 1);
                                if (geoResults.size() > 0) {
                                    destAddress = geoResults.get(0);
                                    Log.d("LocationResultSetSize:", "" + geoResults.size());
                                    destAddressStr = "";
                                    int max = destAddress.getMaxAddressLineIndex();
                                    for (int i = 0; i <= max; i++) {
                                        destAddressStr += destAddress.getAddressLine(i);
                                    }
                                    resultDestTextView.setText(destAddressStr);
                                    resultDestTextView.setVisibility(View.VISIBLE);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                countDownTimer.start();
                return false;
            }
        });

        resultDestTextView.setOnClickListener(view -> {
            view.setVisibility(View.GONE);
            destLocation = new CLocation(destAddress.getLatitude(), destAddress.getLongitude());
            mapManager.addMarker(destLocation, "Search");
            mapManager.animateCamera(destLocation);
            searchDestLocationEdt.clearFocus();
        });

        yourLocationSrcTextView.setOnClickListener(view -> {
            view.setVisibility(View.GONE);
            requestCurrentLocation();
            srcLocation = currentLocation;
            String str = MainActivity.getStringByIdName(context, "your_location");
            searchSrcLocationEdt.setQuery(str, false);
            searchSrcLocationEdt.clearFocus();
        });

        yourLocationDestTextView.setOnClickListener(view -> {
            view.setVisibility(View.GONE);
            requestCurrentLocation();
            destLocation = currentLocation;
            String str = MainActivity.getStringByIdName(context, "your_location");
            searchDestLocationEdt.setQuery(str, false);
            searchDestLocationEdt.clearFocus();
        });

        srcImgView.setOnClickListener(view -> yourLocationSrcTextView.setVisibility(View.VISIBLE));

        destImgView.setOnClickListener(view -> yourLocationDestTextView.setVisibility(View.VISIBLE));

        swapSrcDest.setOnClickListener(view -> {
            CLocation location = srcLocation;
            srcLocation = destLocation;
            destLocation = location;

            CharSequence srcStr = searchSrcLocationEdt.getQuery();
            searchSrcLocationEdt.setQuery(searchDestLocationEdt.getQuery(), false);
            searchDestLocationEdt.setQuery(srcStr, false);
            isSwap = true;

            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    isSwap = false;
                } catch (Exception e){
                  Log.d("MyTag", e.getMessage());
                }
            });
            thread.start();
        });
    }

    private void pluginGGMap() {
        MapFragment mapFragment = (MapFragment) main.getFragmentManager()
                .findFragmentById(R.id.direct_ggmap_api);
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

    private CLocation currentLocation;
    Intent intentCurrentLocation;
    public void requestCurrentLocation(){
        if (main.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            registerLocationReceiver();
            startLocationService();
        }
    }

    private void registerLocationReceiver() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("CURRENT_LOCATION")){
                    currentLocation = new CLocation(intent.getDoubleExtra("latitude", 0f),
                            intent.getDoubleExtra("longitude", 0f));

                    mapManager.reset();
                    mapManager.animateCamera(currentLocation);
                    mapManager.addMarker(currentLocation, "Your Location");

                    main.stopService(intentCurrentLocation);
                    main.unregisterReceiver(this);
                }
            }
        };
        main.registerReceiver(receiver, new IntentFilter("CURRENT_LOCATION"));
    }

    private void startLocationService() {
        intentCurrentLocation = new Intent(main, LocationService.class);
        main.startService(intentCurrentLocation);
    }

    private String srcAddressStr;
    private Address srcAddress;
    private CLocation srcLocation;

    private String destAddressStr;
    private Address destAddress;
    private CLocation destLocation;
}
