package com.example.covimap.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.manager.MapManager;
import com.example.covimap.model.CLocation;
import com.google.android.gms.maps.MapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DiscoverActivity extends Fragment {
    private MapManager mapManager;
    private MainActivity main;
    private Context context;
    private static View view;

    private SearchView searchDiscoverLocationEdt;
    private TextView resultTextView;
    private FloatingActionButton locateCurrentBtn;
    private CLocation currentLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            view = (View)inflater.inflate(R.layout.discover_activity, null);
            prepareWidget();

            MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.discover_ggmap_api);
            mapManager = new MapManager();
            mapFragment.getMapAsync(mapManager);
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

    private void registerLocationReceiver() {
        BroadcastReceiver locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("CURRENT_LOCATION")) {
                    CLocation currentLocation = new CLocation(
                            intent.getDoubleExtra("latitude", 0f),
                            intent.getDoubleExtra("longitude", 0f));

                    mapManager.addMarker(currentLocation, "Here");
                    mapManager.animateCamera(currentLocation);
                }
            }
        };
    }

    //Listener for locateCurrentbtn
    private View.OnClickListener locateCurrentBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            registerLocationReceiver();
        }
    };

    private String addressStr;
    private Address address;
    private SearchView.OnQueryTextListener searchLocationEdtOnQueryTextListener = new SearchView.OnQueryTextListener() {
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
                    String locationName = searchDiscoverLocationEdt.getQuery().toString();
                    List<Address> geoResults = null;

                    if(locationName.equals("")){
                        resultTextView.setVisibility(View.GONE);
                        resultTextView.setText("");
                    }
                    else if (locationName != null) {
                        Geocoder geocoder = new Geocoder(DiscoverActivity.view.getContext(), Locale.getDefault());
                        try {
                            geoResults = geocoder.getFromLocationName(locationName, 1);
                            if (geoResults.size() > 0) {
                                address = geoResults.get(0);
                                Log.d("LocationResultSetSize:", "" + geoResults.size());
                                addressStr = "";
                                int max = address.getMaxAddressLineIndex();
                                for (int i = 0; i <= max; i++) {
                                    addressStr += address.getAddressLine(i);
                                }
                                resultTextView.setText(addressStr);
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
    private View.OnClickListener resultSrcTextViewListener = new View.OnClickListener() {
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
        searchDiscoverLocationEdt = (SearchView) view.findViewById(R.id.search_discover_edt);
        resultTextView = (TextView) view.findViewById(R.id.result_discover_location_text_view);

        locateCurrentBtn = (FloatingActionButton) view.findViewById(R.id.locate_position_btn);
        locateCurrentBtn.setOnClickListener(locateCurrentBtnListener);

        searchDiscoverLocationEdt.setOnQueryTextListener(searchLocationEdtOnQueryTextListener);
        resultTextView.setOnClickListener(resultSrcTextViewListener);
    }
}
