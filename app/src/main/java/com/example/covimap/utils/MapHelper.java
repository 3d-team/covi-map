package com.example.covimap.utils;

import android.util.Log;

import com.example.covimap.config.Config;
import com.example.covimap.manager.DirectionMode;
import com.example.covimap.model.CLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapHelper {
    public static String generateDirectionUrl(CLocation start, CLocation end, DirectionMode mode) {
        String startLocationClause = "origin=" + start.getLatitude() + ","
                + start.getLatitude();
        String endLocationClause = "destination=" + end.getLatitude() + ","
                + end.getLongitude();
        String sensorClause = "sensor=false";
        String modeClaude = "mode=" + mode.toString().toLowerCase();
        String parameters = startLocationClause + "&" + endLocationClause + "&"
                + sensorClause + "&" + modeClaude;
        String outputClause = "json";
        String apiKeyClause = Config.DIRECTION_API_KEY;
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + outputClause + "?"
                + parameters + "&key="
                + apiKeyClause;

        return url;
    }

    public static String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("mylog", "Downloaded URL: " + data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("mylog", "Exception downloading URL: " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
