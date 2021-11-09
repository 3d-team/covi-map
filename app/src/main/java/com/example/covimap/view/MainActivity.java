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

}