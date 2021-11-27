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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EpidemicZoneActivity extends Fragment {
    private MapManager mapManager;
    private MainActivity main;
    private Context context;
    private static View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            view = (View)inflater.inflate(R.layout.epidemic_zone, null);
            prepareWidget();

            MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.epidemic_ggmap_api);
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

    //Prepare for UI---------------------------------------------
    public void prepareWidget(){

    }
}

