package com.example.covimap.model;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CLocation extends Identity {
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
