package com.example.covimap.model;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location extends Identity implements Serializable {
    private double latitude;
    private double longitude;

    public LatLng toLatLng() {
        return new LatLng(getLatitude(), getLongitude());
    }

    public static double getDistance(Location start, Location end){
        int Radius = 6371;// radius of earth in Km
        double lat1 = start.latitude;
        double lat2 = end.latitude;
        double lon1 = start.longitude;
        double lon2 = end.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;

        return (double) Math.round(valueResult*100)/100;
    }

    @NonNull
    @Override
    public String toString() {
        return latitude + ", " + longitude;
    }
}
