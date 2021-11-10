package com.example.covimap.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covimap.R;
import com.example.covimap.model.CLocation;
import com.example.covimap.model.Route;
import com.example.covimap.model.RouteAdapter;

import java.util.ArrayList;
import java.util.List;

public class HistoryJourneyActivity extends Fragment {
    private static View view;
    private List<Route> routes;
    private ListView routeListView;
    private RouteAdapter routeAdapter;
    private EditText beginDay;
    private EditText endDay;

    private Context context;
    private MainActivity main;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.history_journey_activity, null);
        prepareUI();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            context = getActivity();
            main = (MainActivity) getActivity();
        }
        catch (IllegalStateException e){
            throw new IllegalStateException("Error");
        }
    }

    private void createRouteList(){
//        List<CLocation> temp = new List<CLocation>();
        routes.add(new Route(null, "1h30m", "77.5km", "7h30 01/01/2021", 1));
        routes.add(new Route(null, "1h30m", "77.5km", "7h30 01/01/2021", 1));
        routes.add(new Route(null, "1h30m", "77.5km", "7h30 01/01/2021", 1));
        routes.add(new Route(null, "1h30m", "77.5km", "7h30 01/01/2021", 1));
        routes.add(new Route(null, "1h30m", "77.5km", "7h30 01/01/2021", 1));
        routes.add(new Route(null, "1h30m", "77.5km", "7h30 01/01/2021", 1));
        routes.add(new Route(null, "1h30m", "77.5km", "7h30 01/01/2021", 1));
        routes.add(new Route(null, "1h30m", "77.5km", "7h30 01/01/2021", 1));
        routes.add(new Route(null, "1h30m", "77.5km", "7h30 01/01/2021", 1));
        routes.add(new Route(null, "1h30m", "77.5km", "7h30 01/01/2021", 1));
    }

    DatePickerDialog beginDPDialog = null;
    DatePickerDialog endDPDialog = null;

    // Prepare UI before start fragment
    public void prepareUI(){
        routeListView = (ListView) view.findViewById(R.id.history_list_view);
        routes = new ArrayList<>();
        createRouteList();
        routeAdapter = new RouteAdapter(context, R.layout.item_list_history_journey_layout, routes);
        routeListView.setAdapter(routeAdapter);

        routeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(context, "CMNasdfsadfsadfasdfsad ", Toast.LENGTH_LONG);
                Log.d("AAAAAAAA", "AAAAAAAAAAAAAAAA");
            }
        });

        beginDay = (EditText) view.findViewById(R.id.begin_day_edt);
        endDay = (EditText) view.findViewById(R.id.end_day_edt);

        beginDPDialog = new DatePickerDialog(context);
        beginDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("AAAAAAAAAAAAAAAAAAAAA", "BBBBBBBBBBBBBBBBBB");
                beginDPDialog.show();
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int day, int month, int year) {
                        beginDay.setText(day + "/" + (month + 1) + "/" + year);
                    }
                };
            }
        });
    }

}
