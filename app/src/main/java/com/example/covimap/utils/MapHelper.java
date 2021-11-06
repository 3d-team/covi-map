package com.example.covimap.utils;

import android.content.res.Resources;

import com.example.covimap.R;
import com.example.covimap.manager.DirectionMode;
import com.example.covimap.model.CLocation;

public class MapHelper {
    public static String generateDirectionUrl(CLocation start, CLocation end, DirectionMode mode) {
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
}
