package com.example.covimap.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.manager.MapManager;
import com.example.covimap.model.CLocation;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EpidemicZoneActivity extends Fragment {
    private MapManager mapManager;
    private MainActivity main;
    private Context context;
    private static View view;
    private FloatingActionButton currentLocationButton;

    private ImageButton noteButton;
    private TextView note_1, note_2, note_3, note_4, note_5;
    ArrayList<TextView> noteTVList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            view = (View) inflater.inflate(R.layout.epidemic_zone, null);
            prepareWidget();

            MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.epidemic_ggmap_api);
            mapManager = new MapManager();
            mapFragment.getMapAsync(mapManager);
        } catch (InflateException e) {
            Log.e("NEW RECORD ERROR", "onCreateView", e);
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            context = getActivity();
            main = (MainActivity) getActivity();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Error");
        }
    }

    private static boolean isNoteShowed = true;

    public void startTextViewAnimation(TextView tv, Animation animation) {
        tv.startAnimation(animation);
    }

    private class ShowAnimation implements Runnable {
        @Override
        public void run() {
            if (isNoteShowed) {
                for (TextView tv : noteTVList) {
                    Animation hide_animation = AnimationUtils.loadAnimation(context, R.anim.hide_down);
                    startTextViewAnimation(tv, hide_animation);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.d("My Log", e.getMessage());
                    }
                }
                isNoteShowed = false;
            } else {
                for (TextView tv : noteTVList) {
                    Animation show_animation = AnimationUtils.loadAnimation(context, R.anim.show_up);
                    startTextViewAnimation(tv, show_animation);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.d("My Log", e.getMessage());
                    }
                }
                isNoteShowed = true;
            }
        }
    }

    public void prepareWidget() {

        noteButton = (ImageButton) view.findViewById(R.id.note_img_button);
        note_1 = (TextView) view.findViewById(R.id.note_1);
        note_2 = (TextView) view.findViewById(R.id.note_2);
        note_3 = (TextView) view.findViewById(R.id.note_3);
        note_4 = (TextView) view.findViewById(R.id.note_4);
        note_5 = (TextView) view.findViewById(R.id.note_5);
        noteTVList = new ArrayList<>();
        noteTVList.add(note_1);
        noteTVList.add(note_2);
        noteTVList.add(note_3);
        noteTVList.add(note_4);
        noteTVList.add(note_5);
        noteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowAnimation showAnimation = new ShowAnimation();
                Thread t = new Thread(showAnimation);
                t.start();
            }
        });

        currentLocationButton = (FloatingActionButton) view.findViewById(R.id.get_current_location_btn);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationManager locationManager = (LocationManager) main.getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                CLocation cLocation = new CLocation(location.getLatitude(), location.getLongitude());
                mapManager.reset();
                mapManager.animateCamera(cLocation);
                mapManager.addMarker(cLocation, "");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isNoteShowed == false) {
            ShowAnimation showAnimation = new ShowAnimation();
            Thread t = new Thread(showAnimation);
            t.start();
        }
    }
}

