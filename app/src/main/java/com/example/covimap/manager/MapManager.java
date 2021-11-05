package com.example.covimap.manager;

import android.content.res.Resources;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.example.covimap.R;
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

    public void drawRoute(CLocation start, CLocation end, DirectionMode mode) {
        String url = getDirectionUrl(start, end, mode);
    }

    private String getDirectionUrl(CLocation start, CLocation end, DirectionMode mode) {
        String startLocationClause = "origin=" + start.getLatitude() + ","
                + start.getLatitude();
        String endLocationClause = "destination=" + end.getLatitude() + ","
                + end.getLongitude();
        String sensorClause = "sensor=false";
        String modeClaude = "mode=" + mode;
        String parameters = startLocationClause + "&" + endLocationClause + "&"
                + sensorClause + "&" + modeClaude;
        String outputClause = "json";
        String apiKeyClause = Resources.getSystem().getString(R.string.map_api_key);
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + outputClause + "?"
                + parameters + "&key="
                + apiKeyClause;

        return url;
    }

    public void reset() {
        map.clear();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
    }

    private class FetchUrlTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            return null;
        }

        @Override
        protected void onPostExecute(String o) {
            super.onPostExecute(o);
        }
    }
}
