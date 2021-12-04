package com.example.covimap.manager;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.covimap.config.Config;
import com.example.covimap.config.MapConfig;
import com.example.covimap.model.Area;
import com.example.covimap.model.CLocation;
import com.example.covimap.utils.MapHelper;
import com.example.covimap.view.EpidemicZoneActivity;
import com.example.covimap.view.MainActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MapManager implements OnMapReadyCallback {
    private EpidemicZoneActivity epidemicZoneActivity;
    private GoogleMap map;

    public MapManager(EpidemicZoneActivity epidemicZoneActivity){
        this.epidemicZoneActivity = epidemicZoneActivity;
    }

    public Marker addMarker(CLocation location, String title) {
        return map.addMarker(new MarkerOptions().position(location.toLatLng()).title(title));
    }

    public void zoomIn(){
        map.moveCamera(CameraUpdateFactory.zoomBy(0.1f));
    }

    public void zoomOut(){
        map.moveCamera(CameraUpdateFactory.zoomBy(-0.1f));
    }

    public void zoomToHome(){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(15.843777859194317, 106.75648784826494), 5.32f));
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
        if(area == null) return;
        List<CLocation> bounds = area.getBoundaries();
        if(bounds == null) return;

        PolygonOptions polygonOptions = new PolygonOptions();
        for(CLocation c:bounds){
            polygonOptions.add(c.toLatLng());
        }
        polygonOptions.strokeWidth(3);
        polygonOptions.strokeColor(Color.parseColor("#" + area.getColor()));
        polygonOptions.fillColor(Color.parseColor(Config.ALPHA_COLOR + area.getColor()));
        Polygon polygon = map.addPolygon(polygonOptions);
        polygon.setClickable(true);
        polygon.setTag(area);
        map.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                Area area1 = (Area)polygon.getTag();
                HashMap<String, Area> childAreas = area1.getChildAreas();
                if(childAreas == null) return;// end recursive here

                reset();
                epidemicZoneActivity.setStatusText(area1.getName(), area1.getNumberF0(), area1.getColor());
                childAreas.forEach((s, area2) -> {
                    drawArea(area2);
                });
            }
        });
    }

    public void findRouteBetweenTwoLocations(CLocation start, CLocation end, DirectionMode mode) {
        String url = MapHelper.generateDirectionUrl(start, end, mode);
        new FetchURL().execute(url);
    }

    public void findRouteBetweenTwoLocations(CLocation start, CLocation end){
        LatLng barcelona = new LatLng(41.385064,2.173403);
        map.addMarker(new MarkerOptions().position(barcelona).title("Marker in Barcelona"));

        LatLng madrid = new LatLng(40.416775,-3.70379);
        map.addMarker(new MarkerOptions().position(madrid).title("Marker in Madrid"));

        LatLng zaragoza = new LatLng(41.648823,-0.889085);

        //Define list to get all latlng for the route
        List<LatLng> path = new ArrayList();


        //Execute Directions API request
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyDE1hSjk381yOSSqnR9khTVsNdg9A83tYE")
                .build();
        DirectionsApiRequest req = DirectionsApi.getDirections(context, "41.385064,2.173403", "40.416775,-3.70379");
        try {
            DirectionsResult res = req.await();
            if(res.routes == null || res.routes.length == 0){
            }
            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {
            Log.e("MyLog", ex.getLocalizedMessage());
        }

        //Draw the polyline
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
            map.addPolyline(opts);
        }

        map.getUiSettings().setZoomControlsEnabled(true);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(zaragoza, 6));
    }

    public void reset() {
        map.clear();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        if(this.epidemicZoneActivity != null){
            zoomToHome();
        }
    }

    public class FetchURL extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = "";

            try {
                data = MapHelper.downloadUrl(strings[0]);
                Log.d("mylog", "Background task data " + data.toString());
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

    public class DataParser {
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
                            String polyline = "";
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

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
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
