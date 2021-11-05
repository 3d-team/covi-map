package com.example.covimap.manager;

import androidx.annotation.NonNull;

import com.example.covimap.config.MapConfig;
import com.example.covimap.model.CLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MapManager implements OnMapReadyCallback {
    private GoogleMap map;

    public void addMarker(CLocation location, String title) {
        map.addMarker(new MarkerOptions().position(location.toLatLng()).title(title));
    }

    public void moveCamera(CLocation location) {
        map.moveCamera(CameraUpdateFactory.newLatLng(location.toLatLng()));
    }

    public void animateCamera(CLocation location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location.toLatLng(), MapConfig.ZOOM));
    }

    public void reset() {
        map.clear();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
    }
}
