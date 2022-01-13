package com.example.covimap.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.covimap.R;
import com.example.covimap.config.MapConfig;
import com.example.covimap.model.Location;
import com.example.covimap.model.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RenderHistoryItemActivity extends Activity implements OnMapReadyCallback {
    private TextView createdDateTV;
    private TextView distanceTextView;
    private TextView periodTextView;
    private PolylineOptions polylineOptions;
    private Route route;
    private String phoneNumber;
    private String uuid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_journey_item_activity);

        mappingUIComponent();
        pluginGGMap();
        receiveLoggedUserData();
    }

    private void mappingUIComponent() {
        distanceTextView = (TextView) findViewById(R.id.distance_text_view_item);
        periodTextView = (TextView) findViewById(R.id.period_text_view_item);
        FloatingActionButton closeBtn = (FloatingActionButton) findViewById(R.id.close_render_history_item);
        createdDateTV = (TextView) findViewById(R.id.created_day_text_view);

        closeBtn.setOnClickListener(view -> finish());
    }

    private void pluginGGMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.history_item_ggmap_api);
        mapFragment.getMapAsync(this);
    }

    private void receiveLoggedUserData() {
        Intent intent = getIntent();
        phoneNumber = intent.getStringExtra("PHONE-NUMBER");
        uuid = intent.getStringExtra("UUID");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (phoneNumber == null || uuid == null) {
            return;
        }

        DatabaseReference data = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(phoneNumber)
                .child("Routes")
                .child(uuid);

        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                route = (Route)snapshot.getValue(Route.class);

                renderRoute();
                renderInformation();
            }

            private void renderRoute() {
                List<Location> path = route.getPath();

                renderPolylineOfRoute(path);
                drawRouteOnGGMap(path);
            }

            private void renderPolylineOfRoute(List<Location> path) {
                polylineOptions = new PolylineOptions();
                for (Location c : path) {
                    polylineOptions.add(c.toLatLng());
                }
            }

            private void drawRouteOnGGMap(List<Location> path) {
                googleMap.addPolyline(polylineOptions.width(15).color(Color.parseColor("#FF0000")));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path.get(0).toLatLng(), MapConfig.ZOOM_STREET));
            }

            private void renderInformation() {
                createdDateTV.setText(route.getCreatedDay());
                distanceTextView.setText(route.getDistance() + "km");
                periodTextView.setText(route.getPeriod() +"min ");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
