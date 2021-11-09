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


public class DirectActivity extends Fragment {
    private MapManager mapManager;
    private MainActivity main;
    private Context context;
    private static View view;

    private SearchView searchSrcLocationEdt;
    private TextView resultSrcTextView;

    private SearchView searchDestLocationEdt;
    private TextView resultDestTextView;

    private FloatingActionButton locateCurrentBtn;
//    private TextView distanceTextView;
//    private TextView timeTextView;
//    private Button stopRecordBtn;
//    private Button recordBtn;
//    private Button saveRecordBtn;

    private CLocation currentLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            view = (View)inflater.inflate(R.layout.direct_activity, null);
            prepareWidget();

            MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.direct_ggmap_api);
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
            registerLocationReceiver();
        }
    };

    private String srcAddressStr;
    private Address srcAddress;
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
    // Listener for resultTextView
    private View.OnClickListener resultSrcTextViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setVisibility(View.INVISIBLE);
            CLocation locationSearch = new CLocation(srcAddress.getLatitude(), srcAddress.getLongitude());
            mapManager.addMarker(locationSearch, "Search");
            mapManager.animateCamera(locationSearch);
        }
    };

    private String destAddressStr;
    private Address destAddress;
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
    // Listener for resultTextView
    private View.OnClickListener resultDestTextViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setVisibility(View.INVISIBLE);
            CLocation locationSearch = new CLocation(destAddress.getLatitude(), destAddress.getLongitude());
            mapManager.addMarker(locationSearch, "Search");
            mapManager.animateCamera(locationSearch);
        }
    };

    //Prepare for UI---------------------------------------------
    public void prepareWidget(){
        searchSrcLocationEdt = (SearchView) view.findViewById(R.id.search_src_location_edt);
        resultSrcTextView = (TextView) view.findViewById(R.id.result_src_location_text_view);

        searchDestLocationEdt = (SearchView) view.findViewById(R.id.search_dest_location_edt);
        resultDestTextView = (TextView) view.findViewById(R.id.result_dest_location_text_view);
//        distanceTextView = (TextView) view.findViewById(R.id.distance_text_view);
//        timeTextView = (TextView) view.findViewById(R.id.time_text_view);

        locateCurrentBtn = (FloatingActionButton) view.findViewById(R.id.locate_position_btn);
        locateCurrentBtn.setOnClickListener(locateCurrentBtnListener);

        searchSrcLocationEdt.setOnQueryTextListener(searchSrcLocationEdtOnQueryTextListener);
        resultSrcTextView.setOnClickListener(resultSrcTextViewListener);

        searchDestLocationEdt.setOnQueryTextListener(searchDestLocationEdtOnQueryTextListener);
        resultDestTextView.setOnClickListener(resultDestTextViewListener);
    }

}
