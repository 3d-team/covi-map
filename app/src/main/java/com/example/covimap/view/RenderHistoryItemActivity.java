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
import com.example.covimap.model.Route;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private PolylineOptions polylineOptions;
    private PolygonOptions polygonOptions;
    private Route route;
    private String phoneNumber;
    private String uuid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_journey_item_activity);
        // init map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.history_item_ggmap_api);
        mapFragment.getMapAsync(this);

        //init data
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("PHONE-NUMBER");
        uuid = intent.getStringExtra("UUID");


        distanceTextView = (TextView) findViewById(R.id.distance_text_view_item);
        periodTextView = (TextView) findViewById(R.id.period_text_view_item);
        closeBtn = (FloatingActionButton) findViewById(R.id.close_render_history_item);
        createdDateTV = (TextView) findViewById(R.id.created_day_text_view);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if(phoneNumber != null && uuid != null){
            DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("Users").child(phoneNumber).child("Routes").child(uuid);
            data.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    route = (Route)snapshot.getValue(Route.class);
                    createdDateTV.setText(route.getCreatedDay());
                    distanceTextView.setText(route.getDistance());
                    periodTextView.setText(route.getPeriod());
                    List<CLocation> path = route.getPath();
                    polylineOptions = new PolylineOptions();
                    for (CLocation c : path) {
                        polylineOptions.add(c.toLatLng());
                    }
                    googleMap.addPolyline(polylineOptions.width(5).color(Color.parseColor("#00C277")));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path.get(0).toLatLng(), MapConfig.ZOOM_CITY));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
