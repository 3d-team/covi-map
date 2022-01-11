package com.example.covimap.manager;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.covimap.config.Config;
import com.example.covimap.config.MapConfig;
import com.example.covimap.model.Area;
import com.example.covimap.model.CLocation;
import com.example.covimap.utils.MapHelper;
import com.example.covimap.view.RedPlaceFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MapManager implements OnMapReadyCallback {
    private RedPlaceFragment redPlaceFragment;
    private GoogleMap map;

    public MapManager(RedPlaceFragment redPlaceFragment){
        this.redPlaceFragment = redPlaceFragment;
    }

    public Marker addMarker(CLocation location, String title) {
        return map.addMarker(new MarkerOptions().position(location.toLatLng()).title(title));
    }

    public void zoomIn(){
        map.moveCamera(CameraUpdateFactory.zoomBy(MapConfig.ZOOM_IN_OUT_RATIO));
    }

    public void zoomOut(){
        map.moveCamera(CameraUpdateFactory.zoomBy((-1) * MapConfig.ZOOM_IN_OUT_RATIO));
    }

    public void zoomToHome(){
        LatLng VN = new LatLng(15.843777859194317, 106.75648784826494);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(VN, 5.32f));
    }

    public void moveCamera(CLocation location) {
        map.moveCamera(CameraUpdateFactory.newLatLng(location.toLatLng()));
    }

    public void animateCamera(CLocation location) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location.toLatLng(), MapConfig.ZOOM));
    }

    public void animateCamera(CLocation location, int zoom) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location.toLatLng(), zoom));
    }

    public void drawRoute(CLocation start, CLocation end) {
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.add(start.toLatLng(), end.toLatLng());
        lineOptions.width(15);
        lineOptions.color(Color.RED);

        map.addPolyline(lineOptions);
    }

    public void drawArea(Area area){
        if(area == null) {
            return;
        }

        List<CLocation> bounds = area.getBoundaries();
        if (bounds == null) {
            return;
        }

        PolygonOptions polygonOptions = new PolygonOptions();
        for (CLocation c : bounds){
            polygonOptions.add(c.toLatLng());
        }
        polygonOptions.strokeWidth(5);
        polygonOptions.strokeColor(Color.parseColor("#000000"));
        polygonOptions.fillColor(Color.parseColor(Config.ALPHA_COLOR + area.getColor()));
        Polygon polygon = map.addPolygon(polygonOptions);
        polygon.setClickable(true);
        polygon.setTag(area);

        map.setOnPolygonClickListener(clickedPolygon -> {
            Area clickedArea = (Area) clickedPolygon.getTag();
            HashMap<String, Area> childAreas = Objects.requireNonNull(clickedArea).getChildAreas();
            if(childAreas == null) {
                return;
            }

            reset();

            redPlaceFragment.setStatusText(clickedArea.getName(), clickedArea.getNumberF0(), clickedArea.getColor());

            childAreas.forEach((s, childArea) -> drawArea(childArea));
        });
    }

    public void findRouteBetweenTwoLocations(CLocation start, CLocation end, DirectionMode mode) {
        String url = MapHelper.generateDirectionUrl(start, end, mode);
        new FetchAPI().execute(url);
    }

    public void reset() {
        map.clear();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        if (this.redPlaceFragment != null){
            zoomToHome();
        }
    }

    public class FetchAPI extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = "";

            try {
                data = MapHelper.downloadUrl(strings[0]);
                Log.d("mylog", "Background task data " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }

            return data;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new PointsParser().execute(s);
        }
    }

    public class PointsParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("mylog", jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("mylog", parser.toString());

                routes = parser.parse(jObject);
                Log.d("mylog", "Executing routes");
                Log.d("mylog", routes.toString());

            } catch (Exception e) {
                Log.d("mylog", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(20);
                lineOptions.color(Color.RED);

                Log.d("mylog", "onPostExecute lineoptions decoded");
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                map.addPolyline(lineOptions);
            } else {
                Log.d("mylog", "without Polylines drawn");
            }
        }
    }

    private static class DataParser {
        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

            List<List<HashMap<String, String>>> routes = new ArrayList<>();
            JSONArray jRoutes;
            JSONArray jLegs;
            JSONArray jSteps;

            try {
                jRoutes = jObject.getJSONArray("routes");
                /** Traversing all routes */
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<>();
                    /** Traversing all legs */
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline;
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude));
                                hm.put("lng", Double.toString((list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }


        /**
         * Method to decode polyline points
         * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
         */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }
}
