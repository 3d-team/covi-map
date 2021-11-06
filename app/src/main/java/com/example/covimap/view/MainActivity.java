package com.example.covimap.view;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.covimap.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends FragmentActivity {
    private BottomNavigationView bottomNav;

    private NewRecordActivity newRecordActivity;
    private DirectActivity directActivity;
    private HistoryJourneyActivity historyJourneyActivity;
    private DiscoverActivity discoverActivity;
    private PersonalActivity personalActivity;

    private Fragment currentFragment;

//    private boolean stateDestroy;

    public MainActivity() {
        super();
//        this.stateDestroy = false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newRecordActivity = new NewRecordActivity();
        directActivity = new DirectActivity();
        historyJourneyActivity = new HistoryJourneyActivity();
        discoverActivity = new DiscoverActivity();
        personalActivity = new PersonalActivity();


        currentFragment = null;

        bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_new_record:
                        currentFragment = newRecordActivity;
                        break;
                    case R.id.nav_direct_location:
                        currentFragment = directActivity;
                        break;
                    case R.id.nav_history_record:
                        currentFragment = historyJourneyActivity;
                        break;
                    case R.id.nav_discover:
                        currentFragment = discoverActivity;
                        break;
                    case R.id.nav_peronal:
                        currentFragment = personalActivity;
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, currentFragment).commit();
                return true;
            }
        });

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, newRecordActivity).commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



//    FloatingActionButton floatingActionButton;
//    @BindView(R.id.bottom_sheet)
//    LinearLayout bottomSheetLayout;
//    BottomSheetBehavior bottomSheetBehavior;
//
//    private MapManager mapManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        mappingWidget();
//        requestCurrentLocation();
//
//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.ggmap_api);
//        mapManager = new MapManager();
//        mapFragment.getMapAsync(mapManager);
//    }
//
//    private void mappingWidget(){
//        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav_view);
//        bottomNavigationView.setBackground(null);
//        floatingActionButton = (FloatingActionButton) findViewById(R.id.start_record_btn);
//        bottomSheetLayout = (LinearLayout) findViewById(R.id.bottom_sheet);
//        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
//        bottomSheetBehavior.setPeekHeight(175, true);
//    }
//
//    private void requestCurrentLocation() {
//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//        } else {
//            registerLocationReceiver();
//            startLocationService();
//        }
//    }
//
//    private void registerLocationReceiver() {
//        BroadcastReceiver locationReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (intent.getAction().equals("CURRENT_LOCATION")) {
//                    CLocation currentLocation = new CLocation(
//                            intent.getDoubleExtra("latitude", 0f),
//                            intent.getDoubleExtra("longitude", 0f));
//
//                    mapManager.reset();
//                    mapManager.addMarker(currentLocation, "Here");
//                    mapManager.animateCamera(currentLocation);
//
//                    Toast.makeText(MainActivity.this, "Location: " + currentLocation, Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
//
//        registerReceiver(locationReceiver, new IntentFilter("CURRENT_LOCATION"));
//    }
//
//    private void startLocationService() {
//        Intent intent = new Intent(MainActivity.this, LocationService.class);
//        startService(intent);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startLocationService();
//            } else {
//                Toast.makeText(this, "Required GPS", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}