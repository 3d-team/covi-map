package com.example.covimap.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.model.CLocation;
import com.example.covimap.model.DataRenderRoute;
import com.example.covimap.model.Route;
import com.example.covimap.model.RouteAdapter;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class HistoryJourneyActivity extends Fragment {
    private static View view;
    private ArrayList<Route> originRouteList;
    private ArrayList<Route> routes;
    private ListView routeListView;
    private RouteAdapter routeAdapter;

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
        originRouteList.add(new Route(null, "1h30m", "77.5km", "07:00:00 - 01/11/2021", 1));
        originRouteList.add(new Route(null, "1h30m", "17.5km", "07:00:00 - 02/11/2021", 1));
        originRouteList.add(new Route(null, "1h30m", "27.5km", "07:00:00 - 03/11/2021", 1));
        originRouteList.add(new Route(null, "1h30m", "37.5km", "07:00:00 - 04/11/2021", 1));
        originRouteList.add(new Route(null, "1h30m", "47.5km", "07:00:00 - 05/11/2021", 1));
        originRouteList.add(new Route(null, "1h30m", "77.5km", "07:00:00 - 06/11/2021", 1));
        originRouteList.add(new Route(null, "1h30m", "67.5km", "07:00:00 - 07/11/2021", 1));
        originRouteList.add(new Route(null, "1h30m", "77.5km", "07:00:00 - 08/11/2021", 1));
        originRouteList.add(new Route(null, "1h30m", "87.5km", "07:00:00 - 09/11/2021", 1));
        originRouteList.add(new Route(null, "1h30m", "97.5km", "07:00:00 - 10/11/2021", 1));
    }

    private EditText beginDayTextView;
    private EditText endDayTextView;
    private Calendar beginDayCalendar = Calendar.getInstance();
    private Calendar endDayCalendar = Calendar.getInstance();

    private void updateDateEditText(EditText editText, Calendar calendar) {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editText.setText(sdf.format(calendar.getTime()));
    }

    public void filterListView() {
        routes = new ArrayList<Route>();
        int n = originRouteList.size();
        for(int i = 0; i < n; ++i){
            Route route = originRouteList.get(i);
            String datetimeStr = route.getCreatedDay();

            try {
                Date dateItem = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy").parse(datetimeStr);
                if(dateItem.after(beginDayCalendar.getTime()) && dateItem.before(endDayCalendar.getTime())){
                    routes.add(route);
                    Log.d("DATE-IN-RANGE", dateItem.toString());
                }
            }
            catch (Exception e){
                Log.d("DATE-FORMAT-ERROR", e.getMessage());
            }
        }
        RouteAdapter adapter = new RouteAdapter(context, R.layout.item_list_history_journey_layout, routes);
        routeListView.setAdapter(adapter);
    }

    View.OnClickListener getBeginDate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context);
            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    beginDayCalendar.set(year, month, day, 0, 0);
                    updateDateEditText(beginDayTextView, beginDayCalendar);
                    Log.d("DAY-BEGIN", beginDayCalendar.getTime().toString());
                    if(endDayTextView.getText().toString().isEmpty() == false){
                        filterListView();
                    }
                }
            };
            datePickerDialog.setOnDateSetListener(dateSetListener);
            datePickerDialog.show();
        }
    };

    View.OnClickListener getEndDate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context);
            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    endDayCalendar.set(year, month, day, 23, 59);
                    updateDateEditText(endDayTextView, endDayCalendar);
                    Log.d("DAY-END", endDayCalendar.getTime().toString());
                    if(endDayTextView.getText().toString().isEmpty() == false){
                        filterListView();
                    }
                }
            };
            datePickerDialog.setOnDateSetListener(dateSetListener);
            datePickerDialog.show();
        }
    };

    private AdapterView.OnItemClickListener routeListViewAction = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(context, RenderHistoryItemActivity.class);
            DataRenderRoute dataRenderRoute = new DataRenderRoute();
            Bundle args = new Bundle();
            args.putString("DISTANCE",routes.get(i).getDistance());
            args.putString("PERIOD",routes.get(i).getPeriod());
            args.putSerializable("DATA-ROUTE", (Serializable) dataRenderRoute.getData());
            intent.putExtra("BUNDLE", args);
            main.startActivity(intent);
        }
    };

    // Prepare UI before start fragment
    public void prepareUI(){
        routeListView = (ListView) view.findViewById(R.id.history_list_view);
        originRouteList = new ArrayList<>();
        createRouteList();
        routes = new ArrayList<>(originRouteList);

        routeAdapter = new RouteAdapter(context, R.layout.item_list_history_journey_layout, routes);
        routeListView.setOnItemClickListener(routeListViewAction);
        routeListView.setAdapter(routeAdapter);

        beginDayTextView = (EditText) view.findViewById(R.id.begin_day_edt);
        endDayTextView = (EditText) view.findViewById(R.id.end_day_edt);

        beginDayTextView.setOnClickListener(getBeginDate);
        endDayTextView.setOnClickListener(getEndDate);
    }
}
