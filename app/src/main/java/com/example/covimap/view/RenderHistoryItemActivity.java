package com.example.covimap.view;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.covimap.R;
import com.example.covimap.config.MapConfig;
import com.example.covimap.manager.MapManager;
import com.example.covimap.model.CLocation;
import com.example.covimap.model.DataRenderRoute;
import com.example.covimap.service.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlPolygon;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.transform.stream.StreamSource;

public class RenderHistoryItemActivity extends Activity implements OnMapReadyCallback {
    private FloatingActionButton closeBtn;
    private TextView createdDateTV;
    private TextView distanceTextView;
    private TextView periodTextView;
    private ArrayList<CLocation> routes;
    private PolylineOptions polylineOptions;
    private PolygonOptions polygonOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_journey_item_activity);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.history_item_ggmap_api);
        mapFragment.getMapAsync(this);

        distanceTextView = (TextView) findViewById(R.id.distance_text_view_item);
        periodTextView = (TextView) findViewById(R.id.period_text_view_item);
        closeBtn = (FloatingActionButton) findViewById(R.id.close_render_history_item);
        createdDateTV = (TextView) findViewById(R.id.created_day_text_view);
        createdDateTV.setText("9h30 25/11/2021");
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        distanceTextView.setText(args.getString("DISTANCE"));
        periodTextView.setText(args.getString("PERIOD"));

        routes = (ArrayList<CLocation>) args.getSerializable("DATA-ROUTE");
        polylineOptions = new PolylineOptions();
        polygonOptions = new PolygonOptions();
        for (CLocation c : routes) {
            polylineOptions.add(c.toLatLng());
            polygonOptions.add(c.toLatLng());
        }
        polylineOptions.add(routes.get(0).toLatLng());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.addPolyline(polylineOptions.width(1).color(Color.parseColor("#00C277")));
        googleMap.addPolygon(polygonOptions.strokeWidth(3).strokeColor(Color.RED).fillColor(Color.argb(50, 255, 0, 0)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.get(0).toLatLng(), MapConfig.ZOOM_CITY));
    }
}
