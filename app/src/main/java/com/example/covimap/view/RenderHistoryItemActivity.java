package com.example.covimap.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.covimap.R;
import com.example.covimap.config.MapConfig;
import com.example.covimap.model.CLocation;
import com.example.covimap.model.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

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

        closeBtn.setOnClickListener(view -> finish());
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
                    distanceTextView.setText(route.getDistance() + "km");
                    periodTextView.setText(route.getPeriod() +"min ");
                    List<CLocation> path = route.getPath();
                    polylineOptions = new PolylineOptions();
                    for (CLocation c : path) {
                        polylineOptions.add(c.toLatLng());
                    }
                    googleMap.addPolyline(polylineOptions.width(15).color(Color.parseColor("#FF0000")));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path.get(0).toLatLng(), MapConfig.ZOOM_STREET));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
