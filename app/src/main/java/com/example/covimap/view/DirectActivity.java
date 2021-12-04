package com.example.covimap.view;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.manager.MapManager;
import com.example.covimap.model.CLocation;
import com.example.covimap.service.LocationService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.MapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class DirectActivity extends Fragment {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            view = (View)inflater.inflate(R.layout.direct_activity, null);
            prepareWidget();

            MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.direct_ggmap_api);
            mapManager = new MapManager();
            mapFragment.getMapAsync(mapManager);
            CLocation cLocation = new CLocation(60,60);
        }
        catch (InflateException e){
            Log.e("NEW RECORD ERROR", "onCreateView", e);
        }
        return view;
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


    private View.OnClickListener locateCurrentBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            requestCurrentLocation();
        }
    };

    private CLocation currentLocation;
    Intent intentcurrentlocation;
    public void requestCurrentLocation(){
        if (main.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent.getAction().equals("CURRENT_LOCATION")){
                        currentLocation = new CLocation(intent.getDoubleExtra("latitude", 0f), intent.getDoubleExtra("longitude", 0f));
                        mapManager.reset();
                        mapManager.animateCamera(currentLocation);
                        mapManager.addMarker(currentLocation, "Your Location");
                        main.stopService(intentcurrentlocation);
                        main.unregisterReceiver(this);
                    }
                }
            };
            main.registerReceiver(receiver, new IntentFilter("CURRENT_LOCATION"));
            intentcurrentlocation = new Intent(main, LocationService.class);
            main.startService(intentcurrentlocation);
        }
    }

    private String srcAddressStr;
    private Address srcAddress;
    private CLocation srcLocation;
    private SearchView.OnQueryTextListener searchSrcLocationEdtOnQueryTextListener = new SearchView.OnQueryTextListener() {
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
                    List<Address> geoResults = null;

                    if(locationName.equals("")){
                        resultSrcTextView.setVisibility(View.GONE);
                        resultSrcTextView.setText("");
                    }
                    else if (locationName != null) {
                        Geocoder geocoder = new Geocoder(DirectActivity.view.getContext(), Locale.getDefault());
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
                    else{
                        resultSrcTextView.setVisibility(View.GONE);
                        resultSrcTextView.setText("");
                    }
                }
            };
            countDownTimer.start();
            return false;
        }
    };

    private View.OnClickListener resultSrcTextViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setVisibility(View.GONE);
            srcLocation = new CLocation(srcAddress.getLatitude(), srcAddress.getLongitude());
            mapManager.addMarker(srcLocation, "Search");
            mapManager.animateCamera(srcLocation);
            searchSrcLocationEdt.clearFocus();
        }
    };

    private String destAddressStr;
    private Address destAddress;
    private CLocation destLocation;
    private SearchView.OnQueryTextListener searchDestLocationEdtOnQueryTextListener = new SearchView.OnQueryTextListener() {
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
                    String locationName = searchDestLocationEdt.getQuery().toString();
                    List<Address> geoResults = null;

                    if(locationName.equals("")){
                        resultDestTextView.setVisibility(View.GONE);
                        resultDestTextView.setText("");
                    }
                    else if (locationName != null) {
                        Geocoder geocoder = new Geocoder(DirectActivity.view.getContext(), Locale.getDefault());
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
                    else{
                        resultDestTextView.setVisibility(View.GONE);
                        resultDestTextView.setText("");
                    }
                }
            };
            countDownTimer.start();
            return false;
        }
    };

    private View.OnClickListener resultDestTextViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setVisibility(View.GONE);
            destLocation = new CLocation(destAddress.getLatitude(), destAddress.getLongitude());
            mapManager.addMarker(destLocation, "Search");
            mapManager.animateCamera(destLocation);
            searchDestLocationEdt.clearFocus();
        }
    };

    private View.OnClickListener yourLocationSrcTextViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setVisibility(View.GONE);
            requestCurrentLocation();
            srcLocation = currentLocation;
            String str = MainActivity.getStringByIdName(context, "your_location");
            searchSrcLocationEdt.setQuery(str, false);
            searchSrcLocationEdt.clearFocus();
        }
    };

    private View.OnClickListener yourLocationDestTextViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setVisibility(View.GONE);
            requestCurrentLocation();
            destLocation = currentLocation;
            String str = MainActivity.getStringByIdName(context, "your_location");
            searchDestLocationEdt.setQuery(str, false);
            searchDestLocationEdt.clearFocus();
        }
    };


    //Prepare for UI---------------------------------------------
    public void prepareWidget(){
        searchSrcLocationEdt = (SearchView) view.findViewById(R.id.search_src_location_edt);
        resultSrcTextView = (TextView) view.findViewById(R.id.result_src_location_text_view);

        searchDestLocationEdt = (SearchView) view.findViewById(R.id.search_dest_location_edt);
        resultDestTextView = (TextView) view.findViewById(R.id.result_dest_location_text_view);

        locateCurrentBtn = (FloatingActionButton) view.findViewById(R.id.locate_position_btn);
        locateCurrentBtn.setOnClickListener(locateCurrentBtnListener);

        searchSrcLocationEdt.setOnQueryTextListener(searchSrcLocationEdtOnQueryTextListener);
        resultSrcTextView.setOnClickListener(resultSrcTextViewListener);

        searchDestLocationEdt.setOnQueryTextListener(searchDestLocationEdtOnQueryTextListener);
        resultDestTextView.setOnClickListener(resultDestTextViewListener);

        yourLocationSrcTextView = (TextView) view.findViewById(R.id.your_location_src_text_view);
        yourLocationSrcTextView.setOnClickListener(yourLocationSrcTextViewListener);
        yourLocationDestTextView = (TextView) view.findViewById(R.id.your_location_dest_text_view);
        yourLocationDestTextView.setOnClickListener(yourLocationDestTextViewListener);

        srcImgView = (ImageView) view.findViewById(R.id.src_icon);
        srcImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yourLocationSrcTextView.setVisibility(View.VISIBLE);
            }
        });
        destImgView = (ImageView) view.findViewById(R.id.dest_icon);
        destImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yourLocationDestTextView.setVisibility(View.VISIBLE);
            }
        });

        swapSrcDest = (ImageView) view.findViewById(R.id.swap_src_dest);
        swapSrcDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CLocation location = srcLocation;
                srcLocation = destLocation;
                destLocation = location;

                CharSequence srcStr = searchSrcLocationEdt.getQuery();
                searchSrcLocationEdt.setQuery(searchDestLocationEdt.getQuery(), false);
                searchDestLocationEdt.setQuery(srcStr, false);
                isSwap = true;

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            isSwap = false;
                        }
                        catch (Exception e){}
                    }
                });
                thread.start();
            }
        });
    }
}
