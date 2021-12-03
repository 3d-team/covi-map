package com.example.covimap.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.config.Config;
import com.example.covimap.config.MapConfig;
import com.example.covimap.manager.MapManager;
import com.example.covimap.model.Area;
import com.example.covimap.model.CLocation;
import com.example.covimap.service.LocationService;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EpidemicZoneActivity extends Fragment implements com.example.covimap.service.EpidemicZoneActivity {
    private MapManager mapManager;
    private MainActivity main;
    private Context context;
    private static View view;
    private FloatingActionButton currentLocationButton;
    private Area vietnam;

    private ImageButton zoomInImgBtn, homeImgBtn, zoomOutImgBtn, playShowImgBtn;
    private Button provinceBtn, districtBtn, communceBtn, numberF0Btn;

    private ImageButton noteButton;
    private TextView note_1, note_2, note_3, note_4, note_5;
    private TextView statusTextView;
    ArrayList<TextView> noteTVList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            view = (View) inflater.inflate(R.layout.epidemic_zone, null);
            prepareWidget();

            MapFragment mapFragment = (MapFragment) main.getFragmentManager().findFragmentById(R.id.epidemic_ggmap_api);
            mapManager = new MapManager(this);
            mapFragment.getMapAsync(mapManager);
        } catch (InflateException e) {
            Log.e("NEW RECORD ERROR", "onCreateView", e);
        }
        return view;
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

    public void startTextViewAnimation(TextView tv, Animation animation) {
        tv.startAnimation(animation);
    }

    private class ShowAnimation implements Runnable {
        @Override
        public void run()
        {
            if (isNoteShowed) {
                for (TextView tv : noteTVList) {
                    Animation hide_animation = AnimationUtils.loadAnimation(context, R.anim.hide_down);
                    startTextViewAnimation(tv, hide_animation);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.d("My Log", e.getMessage());
                    }
                }
                isNoteShowed = false;
            } else {
                for (TextView tv : noteTVList) {
                    Animation show_animation = AnimationUtils.loadAnimation(context, R.anim.show_up);
                    startTextViewAnimation(tv, show_animation);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.d("My Log", e.getMessage());
                    }
                }
                isNoteShowed = true;
            }
        }
    }

    public void prepareWidget() {
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
        noteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                ShowAnimation showAnimation = new ShowAnimation();
                Thread t = new Thread(showAnimation);
                t.start();
            }
        });

        currentLocationButton = (FloatingActionButton) view.findViewById(R.id.get_current_location_btn);
        currentLocationButton.setOnClickListener(locateCurrentBtnListener);

        zoomInImgBtn = view.findViewById(R.id.zoom_in_img_button);
        zoomInImgBtn.setOnClickListener(zoomInAction);
        zoomOutImgBtn = view.findViewById(R.id.zoom_out_img_button);
        zoomOutImgBtn.setOnClickListener(zoomOutAction);
        homeImgBtn = view.findViewById(R.id.home_img_button);
        homeImgBtn.setOnClickListener(zoomToHomeAction);
        playShowImgBtn = view.findViewById(R.id.play_show_button);

        provinceBtn = view.findViewById(R.id.province_button);
        provinceBtn.setOnClickListener(provinceBtnAction);
        districtBtn = view.findViewById(R.id.district_button);
        districtBtn.setOnClickListener(districtBtnAction);
        communceBtn = view.findViewById(R.id.ward_button);
        communceBtn.setOnClickListener(communeBtnAction);
        numberF0Btn = view.findViewById(R.id.f0_button);

        statusTextView = view.findViewById(R.id.province_name_text_view);
    }

    Intent intentcurrentlocation;
    private View.OnClickListener locateCurrentBtnListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if (main.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else {
                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if(intent.getAction().equals("CURRENT_LOCATION")){
                            CLocation location = new CLocation(intent.getDoubleExtra("latitude", 0f), intent.getDoubleExtra("longitude", 0f));
                            mapManager.reset();
                            mapManager.animateCamera(location);
                            mapManager.addMarker(location, "Your Location");
                            main.stopService(intentcurrentlocation);
                            main.unregisterReceiver(this);
                        }
                    }
                };
                main.registerReceiver(receiver, new IntentFilter("CURRENT_LOCATION"));
                intentcurrentlocation = new Intent(main, LocationService.class);
                main.startService(intentcurrentlocation);
            }
        }
    };

    private View.OnClickListener zoomInAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mapManager.zoomIn();
        }
    };

    private View.OnClickListener zoomOutAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mapManager.zoomOut();
        }
    };

    private View.OnClickListener zoomToHomeAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mapManager.zoomToHome();
        }
    };

    private View.OnClickListener provinceBtnAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mapManager.reset();
            if (vietnam == null) return;
            HashMap<String, Area> provinces = vietnam.getChildAreas();
            if(provinces == null) return;
            provinces.forEach((s, area) -> {
                mapManager.drawArea(area);
            });
        }
    };

    private View.OnClickListener districtBtnAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mapManager.reset();
            HashMap<String, Area> provinces = vietnam.getChildAreas();
            if(provinces == null){return;}

            provinces.forEach((s, area) -> {
                HashMap<String, Area> districts = area.getChildAreas();
                if (districts == null){return;}

                districts.forEach((s1, area1) -> {
                    mapManager.drawArea(area1);
                });

            });
        }
    };

    private View.OnClickListener communeBtnAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mapManager.reset();
            HashMap<String, Area> provinces = vietnam.getChildAreas();
            if(provinces == null){return;}

            provinces.forEach((s, area) -> {
                HashMap<String, Area> districts = area.getChildAreas();
                if (districts == null){return;}

                districts.forEach((s1, area1) -> {
                    HashMap<String, Area> communes = area.getChildAreas();
                    if (communes == null){return;}

                    districts.forEach((s2, area2) -> {
                        mapManager.drawArea(area2);
                    });
                });

            });
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if(isNoteShowed == false) {
            ShowAnimation showAnimation = new ShowAnimation();
            Thread t = new Thread(showAnimation);
            t.start();
        }
    }

    @Override
    public void getMapArea(Area area) {
        this.vietnam = area;
    }

    @Override
    public void setStatusText(String address, String color) {
        if(statusTextView != null){
            statusTextView.setText(Html.fromHtml("<b>" + address + "</b><br />" +
                    "<i>" + "Chưa cập nhật" + "</i>", Html.FROM_HTML_MODE_LEGACY));
            statusTextView.setTextColor(Color.parseColor(color));
        }
    }
}

