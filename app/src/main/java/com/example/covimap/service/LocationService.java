package com.example.covimap.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service {
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public void onCreate() {
        super.onCreate();

        locationRequest = LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        requestLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    private void requestLocation() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                sendLocationBroadcast(locationResult);
                writeLog(locationResult);
            }
        }, Looper.getMainLooper());
    }

    private void sendLocationBroadcast(LocationResult locationResult) {
        Intent intent = new Intent("CURRENT_LOCATION")
                .putExtra("latitude", locationResult.getLastLocation().getLatitude())
                .putExtra("longitude", locationResult.getLastLocation().getLongitude());
        sendBroadcast(intent);
    }

    private void writeLog(LocationResult locationResult) {
        Log.d("covi-map", "Lat is: " + locationResult.getLastLocation().getLatitude() + ", "
                + "Lng is: " + locationResult.getLastLocation().getLongitude());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}