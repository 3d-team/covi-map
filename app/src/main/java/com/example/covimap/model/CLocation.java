package com.example.covimap.model;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

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

    public static double getDistance(CLocation start, CLocation end){
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
        //double km = valueResult / 1;
        //DecimalFormat newFormat = new DecimalFormat("####");
//        double meter = valueResult % 1000;
//        int meterInDec = Integer.valueOf(newFormat.format(meter));

        return (double) Math.round(valueResult*100)/100;
    }
}
