package com.example.covimap.model;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CLocation {
    private double latitude;
    private double longitude;

    public static CLocation fromNativeLocation(Location data) {
        return new CLocation(data.getLatitude(), data.getLongitude());
    }

    public LatLng toLatLng() {
        return new LatLng(getLatitude(), getLongitude());
    }

    @NonNull
    @Override
    public String toString() {
        return latitude + ", " + longitude;
    }
}
