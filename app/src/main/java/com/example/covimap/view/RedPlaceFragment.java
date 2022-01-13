package com.example.covimap.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.manager.MapManager;
import com.example.covimap.model.Area;
import com.example.covimap.model.Location;
import com.example.covimap.service.LocationService;
import com.example.covimap.service.RedPlaceFragmentCallBacks;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class RedPlaceFragment extends Fragment implements RedPlaceFragmentCallBacks {
    private MapManager mapManager;
    private MainActivity main;
    private Context context;
    private static View view;
    private FloatingActionButton currentLocationButton;
    private Area vietnam;
    private Marker currentMarker = null;

    private ImageButton zoomInImgBtn, homeImgBtn, zoomOutImgBtn, refreshImgBtn;
    private Button provinceBtn, districtBtn, communeBtn, numberF0Btn;

    private ImageButton noteButton;
    private TextView note_1, note_2, note_3, note_4, note_5;
    private TextView statusTextView, currentDayTextView;
    ArrayList<TextView> noteTVList;

    Intent intentCurrentLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.epidemic_zone, null);

            mappingUIComponent();
            subscribeEventButton();
            pluginGGMap();
        } catch (InflateException e) {
            Log.e("NEW RECORD ERROR", "onCreateView", e);
        }
        return view;
    }

    public void mappingUIComponent() {
        noteButton = (ImageButton) view.findViewById(R.id.note_img_button);
        note_1 = (TextView) view.findViewById(R.id.note_1);
        note_2 = (TextView) view.findViewById(R.id.note_2);
        note_3 = (TextView) view.findViewById(R.id.note_3);
        note_4 = (TextView) view.findViewById(R.id.note_4);
        note_5 = (TextView) view.findViewById(R.id.note_5);
        noteTVList = new ArrayList<>();
        noteTVList.add(note_1);
        noteTVList.add(note_2);
        noteTVList.add(note_3);
        noteTVList.add(note_4);
        noteTVList.add(note_5);
        noteButton.setOnClickListener(view -> {
            ShowAnimation showAnimation = new ShowAnimation();
            Thread t = new Thread(showAnimation);
            t.start();
        });
        currentLocationButton = (FloatingActionButton) view.findViewById(R.id.get_current_location_btn);
        zoomInImgBtn = view.findViewById(R.id.zoom_in_img_button);
        zoomOutImgBtn = view.findViewById(R.id.zoom_out_img_button);
        homeImgBtn = view.findViewById(R.id.home_img_button);
        refreshImgBtn = view.findViewById(R.id.refresh_img_button);
        provinceBtn = view.findViewById(R.id.province_button);
        districtBtn = view.findViewById(R.id.district_button);
        communeBtn = view.findViewById(R.id.ward_button);
        numberF0Btn = view.findViewById(R.id.f0_button);
        statusTextView = view.findViewById(R.id.province_name_text_view);
        currentDayTextView = view.findViewById(R.id.current_day_text_view);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        currentDayTextView.setText(sdf.format(calendar.getTime()));
    }

    private void subscribeEventButton() {
        currentLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (main.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    BroadcastReceiver receiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if(intent.getAction().equals("CURRENT_LOCATION")){
                                Location location = new Location(intent.getDoubleExtra("latitude", 0f),
                                        intent.getDoubleExtra("longitude", 0f));

                                if (currentMarker != null){
                                    currentMarker.remove();
                                }

                                movingCameraTo(location);

                                main.stopService(intentCurrentLocation);
                                main.unregisterReceiver(this);
                            }
                        }
                    };

                    main.registerReceiver(receiver, new IntentFilter("CURRENT_LOCATION"));
                    intentCurrentLocation = new Intent(main, LocationService.class);
                    main.startService(intentCurrentLocation);
                }
            }

            private void movingCameraTo(Location location) {
                mapManager.animateCamera(location);
                currentMarker = mapManager.addMarker(location, "Your Location");
            }
        });

        zoomInImgBtn.setOnClickListener(view -> mapManager.zoomIn());

        zoomOutImgBtn.setOnClickListener(view -> mapManager.zoomOut());

        homeImgBtn.setOnClickListener(view -> mapManager.zoomToHome());

        refreshImgBtn.setOnClickListener(view -> mapManager.reset());

        provinceBtn.setOnClickListener(view -> {
            mapManager.reset();

            if (vietnam == null) {
                return;
            }

            HashMap<String, Area> provinces = vietnam.getChildAreas();
            if (provinces == null) {
                return;
            }

            provinces.forEach((s, area) -> {
                mapManager.drawArea(area);
            });
        });

        districtBtn.setOnClickListener(view -> {
            mapManager.reset();

            HashMap<String, Area> provinces = vietnam.getChildAreas();
            if (provinces == null) {
                return;
            }

            provinces.forEach((s, area) -> {
                HashMap<String, Area> districts = area.getChildAreas();
                if (districts == null) {
                    return;
                }

                districts.forEach((s1, area1) -> {
                    mapManager.drawArea(area1);
                });
            });
        });

        communeBtn.setOnClickListener(view -> {
            mapManager.reset();

            HashMap<String, Area> provinces = vietnam.getChildAreas();
            if(provinces == null) {
                return;
            }

            provinces.forEach((s, area) -> {
                HashMap<String, Area> districts = area.getChildAreas();
                if (districts == null) {
                    return;
                }

                districts.forEach((s1, area1) -> {
                    HashMap<String, Area> communes = area.getChildAreas();
                    if (communes == null) {
                        return;
                    }

                    districts.forEach((s2, area2) -> {
                        mapManager.drawArea(area2);
                    });
                });

            });
        });
    }

    private void pluginGGMap() {
        MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.epidemic_ggmap_api);
        mapManager = new MapManager(this);
        mapFragment.getMapAsync(mapManager);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            context = getActivity();
            main = (MainActivity) getActivity();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Error");
        }
    }

    private static boolean isNoteShowed = true;

    private class ShowAnimation implements Runnable {
        @Override
        public void run()
        {
            handleNotesAnimation();

            isNoteShowed = !isNoteShowed;
        }

        private void handleNotesAnimation() {
            for (TextView tv: noteTVList) {
                handleNoteAnimation(tv);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.d("My Log", e.getMessage());
                }
            }
        }

        private void handleNoteAnimation(TextView textView) {
            if (isNoteShowed) {
                hideAnimation(textView);
            } else {
                showAnimation(textView);
            }
        }

        private void hideAnimation(TextView textView) {
            Animation hide_animation = AnimationUtils.loadAnimation(context, R.anim.hide_down);
            startTextViewAnimation(textView, hide_animation);
        }

        private void startTextViewAnimation(TextView tv, Animation animation) {
            tv.startAnimation(animation);
        }

        private void showAnimation(TextView textView) {
            Animation show_animation = AnimationUtils.loadAnimation(context, R.anim.show_up);
            startTextViewAnimation(textView, show_animation);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isNoteShowed) {
            return;
        }

        ShowAnimation showAnimation = new ShowAnimation();
        Thread t = new Thread(showAnimation);
        t.start();
    }

    @Override
    public void getMapArea(Area area) {
        this.vietnam = area;
    }

    @Override
    public void setStatusText(String address, String numberF0, String color) {
        if(statusTextView != null){
            String state = MainActivity.getStringByIdName(context, "gray_zone_danger");
            int background = R.drawable.gray_zone_style;
            switch (color){
                case Config.RED_ZONE_COLOR:
                    state = MainActivity.getStringByIdName(context, "red_zone_danger");
                    background = R.drawable.red_zone_style;
                    break;
                case Config.ORANGE_ZONE_COLOR:
                    state = MainActivity.getStringByIdName(context, "orange_zone_danger");
                    background = R.drawable.orange_zone_style;
                    break;
                case Config.YELLOW_ZONE_COLOR:
                    state = MainActivity.getStringByIdName(context, "yellow_zone_danger");
                    background = R.drawable.yellow_zone_style;
                    break;
                case Config.GREEN_ZONE_COLOR:
                    state = MainActivity.getStringByIdName(context, "green_zone_danger");
                    background = R.drawable.green_zone_style;
                    break;
            }


            statusTextView.setText(Html.fromHtml("<b>" + address + "</b><br />" +
                      "F0: " + numberF0 + "<br />" +
                      "<i>" + state + "</i>", Html.FROM_HTML_MODE_LEGACY));
            statusTextView.setBackgroundResource(background);
        }
    }
}

