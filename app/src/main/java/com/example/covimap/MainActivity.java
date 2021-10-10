package com.example.covimap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private BottomNavigationView bottomNavigationView;
    FloatingActionButton floatingActionButton;
    private GoogleMap map;
    private boolean isShow = true;

//    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.bottom_sheet)
    LinearLayout bottomSheetLayout;

    BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mappingWidget();
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
//        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                switch (newState){
//                    case BottomSheetBehavior.STATE_HIDDEN:
//                        break;
//                    case BottomSheetBehavior.STATE_EXPANDED:
//                        break;
//                    case BottomSheetBehavior.STATE_COLLAPSED:
//                        break;
//                    case BottomSheetBehavior.STATE_DRAGGING:
//                        break;
//                    case BottomSheetBehavior.STATE_SETTLING:
//                        break;
//                }
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//
//            }
//        });
//        bottomSheetLayout.
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(10.762622, 106.660172);
        map.addMarker(new MarkerOptions().position(sydney).title("Marker in Viet Nam"));
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if(isShow){
                    bottomNavigationView.setVisibility(View.GONE);
//                    floatingActionButton.setVisibility(View.GONE);
//                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    bottomSheetLayout.setVisibility(View.GONE);
                    isShow = false;
                }
                else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
//                    floatingActionButton.setVisibility(View.VISIBLE);
//                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    bottomSheetLayout.setVisibility(View.VISIBLE);
                    isShow = true;
                }
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

//    @OnClick(R.id.b)
}