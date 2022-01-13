package com.example.covimap.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.covimap.R;
import com.example.covimap.service.HistoryJourneyFragmentCallBacks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class HistoryJourneyFragment extends Fragment implements HistoryJourneyFragmentCallBacks {
    private String phoneNumber;
    private static View view;
    private ArrayList<RouteLabel> originRouteLbList;
    private ArrayList<RouteLabel> routeLbs;
    private ListView routeListView;
    private RouteAdapter routeLbAdapter;
    private TextView noDataText;

    private Context context;
    private MainActivity main;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.history_journey_activity, null);

        mappingUIComponent();
        subscribeEventButton();

        return view;
    }

    public void mappingUIComponent(){
        routeListView = view.findViewById(R.id.history_list_view);
        beginDayTextView = view.findViewById(R.id.begin_day_edt);
        endDayTextView = view.findViewById(R.id.end_day_edt);
        noDataText = view.findViewById(R.id.noDataText);
    }

    private void subscribeEventButton() {
        originRouteLbList = new ArrayList<>();
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(phoneNumber)
                .child("Routes");
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RouteLabel routeLabel = dataSnapshot.getValue(RouteLabel.class);
                    originRouteLbList.add(routeLabel);
                }

                routeLbs = new ArrayList<>(originRouteLbList);
                if (routeLbs.isEmpty()) {
                    noDataText.setText("Không có dữ liệu.");
                    return;
                }
                routeListView.setOnItemClickListener((adapterView, view, i, l) -> {
                    Intent intent = new Intent(context, RenderHistoryItemActivity.class);
                    intent.putExtra("PHONE-NUMBER", phoneNumber);
                    intent.putExtra("UUID", routeLbs.get(i).getUuid());
                    main.startActivity(intent);
                });

                routeLbAdapter = new RouteAdapter(context, R.layout.item_list_history_journey_layout, routeLbs);
                routeListView.setAdapter(routeLbAdapter);

                beginDayTextView.setOnClickListener(view -> {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(context);
                    DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
                        beginDayCalendar.set(year, month, day, 0, 0);
                        updateDateEditText(beginDayTextView, beginDayCalendar);
                        Log.d("DAY-BEGIN", beginDayCalendar.getTime().toString());
                        if(!endDayTextView.getText().toString().isEmpty()){
                            filterListView();
                        }
                    };
                    datePickerDialog.setOnDateSetListener(dateSetListener);
                    datePickerDialog.show();
                });

                endDayTextView.setOnClickListener(view -> {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(context);
                    DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
                        endDayCalendar.set(year, month, day, 23, 59);
                        updateDateEditText(endDayTextView, endDayCalendar);
                        Log.d("DAY-END", endDayCalendar.getTime().toString());
                        if(!endDayTextView.getText().toString().isEmpty()){
                            filterListView();
                        }
                    };
                    datePickerDialog.setOnDateSetListener(dateSetListener);
                    datePickerDialog.show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            context = getActivity();
            main = (MainActivity) getActivity();
        } catch (IllegalStateException e){
            throw new IllegalStateException("Error");
        }
    }

    private EditText beginDayTextView;
    private EditText endDayTextView;
    private final Calendar beginDayCalendar = Calendar.getInstance();
    private final Calendar endDayCalendar = Calendar.getInstance();

    private void updateDateEditText(EditText editText, Calendar calendar) {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editText.setText(sdf.format(calendar.getTime()));
    }

    public void filterListView() {
        routeLbs = new ArrayList<>();
        int n = originRouteLbList.size();

        for (int i = 0; i < n; ++i){
            RouteLabel route = originRouteLbList.get(i);
            String datetimeStr = route.getCreatedDay();

            try {
                Date dateItem = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy").parse(datetimeStr);
                if(dateItem.after(beginDayCalendar.getTime()) && dateItem.before(endDayCalendar.getTime())){
                    routeLbs.add(route);
                    Log.d("DATE-IN-RANGE", dateItem.toString());
                }
            } catch (Exception e){
                Log.d("DATE-FORMAT-ERROR", e.getMessage());
            }
        }

        RouteAdapter adapter = new RouteAdapter(context, R.layout.item_list_history_journey_layout, routeLbs);
        routeListView.setAdapter(adapter);
    }

    @Override
    public void getPhoneNumber(String phone) {
        this.phoneNumber = phone;
    }

    public static class RouteAdapter extends BaseAdapter {
        private class ViewHolder{
            private TextView createdDayTextView;
            private TextView startLocationTextView;
            private TextView endLocationTextView;
            private TextView distanceTextVew;
            private TextView periodTextView;
        }

        private final Context context;
        private final List<RouteLabel> routeLabels;
        private final int layout;

        public RouteAdapter(Context context, int layout, List<RouteLabel> routeLabels){
            this.context = context;
            this.layout = layout;
            this.routeLabels = routeLabels;
        }

        @Override
        public int getCount() {
            return this.routeLabels.size();
        }

        @Override
        public Object getItem(int i) {
            return this.routeLabels.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if(view == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(layout, null);
                holder = new ViewHolder();

                holder.createdDayTextView = view.findViewById(R.id.created_day_text_view);
                holder.startLocationTextView = view.findViewById(R.id.strart_point_text_view);
                holder.endLocationTextView = view.findViewById(R.id.end_point_text_view);
                holder.distanceTextVew = view.findViewById(R.id.distance_text_view_item);
                holder.periodTextView = view.findViewById(R.id.period_text_view_item);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            RouteLabel routeLabel = routeLabels.get(i);
            holder.createdDayTextView.setText(routeLabel.getCreatedDay());
            holder.startLocationTextView.setText(routeLabel.getStartAddress());
            holder.endLocationTextView.setText(routeLabel.getEndAddress());
            holder.distanceTextVew.setText(routeLabel.getDistance());
            holder.periodTextView.setText(routeLabel.getPeriod());

            return view;
        }
    }

    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteLabel implements Serializable {
        private String uuid;
        private String period; // time amount
        private String distance; // distance amount
        private String createdDay;
        private String startAddress;
        private String endAddress;

        public String getUuid() {
            return uuid;
        }

        public String getPeriod() {
            return period + "min";
        }

        public String getDistance() {
            return distance + "km";
        }

        public String getCreatedDay() {
            return createdDay;
        }

        public String getStartAddress() {
            return startAddress;
        }

        public String getEndAddress() {
            return endAddress;
        }

        @Override
        public String toString() {
            return "RouteLabel{" +
                    "uuid='" + uuid + '\'' +
                    ", period='" + period + '\'' +
                    ", distance='" + distance + '\'' +
                    ", createdDay='" + createdDay + '\'' +
                    ", startAddress='" + startAddress + '\'' +
                    ", endAddress='" + endAddress + '\'' +
                    '}';
        }
    }
}
