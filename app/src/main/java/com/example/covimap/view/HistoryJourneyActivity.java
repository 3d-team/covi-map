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
import com.example.covimap.model.RouteLabel;
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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class HistoryJourneyActivity extends Fragment implements HistoryJourneyFragmentCallBacks {
    private String phoneNumber;
    private static View view;
    private ArrayList<RouteLabel> originRouteLbList;
    private ArrayList<RouteLabel> routeLbs;
    private ListView routeListView;
    private RouteAdapter routeLbAdapter;

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

    private void initOriginRouteList(){
        originRouteLbList = new ArrayList<>();
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(phoneNumber).child("Routes");
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RouteLabel routeLabel = dataSnapshot.getValue(RouteLabel.class);
                    Log.d("MyLog-Fragment History", routeLabel.toString());
                    originRouteLbList.add(routeLabel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
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
        routeLbs = new ArrayList<RouteLabel>();
        int n = originRouteLbList.size();
        for(int i = 0; i < n; ++i){
            RouteLabel route = originRouteLbList.get(i);
            String datetimeStr = route.getCreatedDay();

            try {
                Date dateItem = new SimpleDateFormat("HH:mm:ss - dd/MM/yyyy").parse(datetimeStr);
                if(dateItem.after(beginDayCalendar.getTime()) && dateItem.before(endDayCalendar.getTime())){
                    routeLbs.add(route);
                    Log.d("DATE-IN-RANGE", dateItem.toString());
                }
            }
            catch (Exception e){
                Log.d("DATE-FORMAT-ERROR", e.getMessage());
            }
        }
        RouteAdapter adapter = new RouteAdapter(context, R.layout.item_list_history_journey_layout, routeLbs);
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
            intent.putExtra("PHONE-NUMBER", phoneNumber);
            intent.putExtra("UUID", routeLbs.get(i).getUuid());
            main.startActivity(intent);
        }
    };

    // Prepare UI before start fragment
    public void prepareUI(){
        routeListView = (ListView) view.findViewById(R.id.history_list_view);

        originRouteLbList = new ArrayList<>();
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(phoneNumber).child("Routes");
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RouteLabel routeLabel = dataSnapshot.getValue(RouteLabel.class);
                    originRouteLbList.add(routeLabel);
                }

                routeLbs = new ArrayList<>(originRouteLbList);
                routeListView.setOnItemClickListener(routeListViewAction);
                routeLbAdapter = new RouteAdapter(context, R.layout.item_list_history_journey_layout, routeLbs);
                routeListView.setAdapter(routeLbAdapter);

                beginDayTextView = (EditText) view.findViewById(R.id.begin_day_edt);
                endDayTextView = (EditText) view.findViewById(R.id.end_day_edt);
                beginDayTextView.setOnClickListener(getBeginDate);
                endDayTextView.setOnClickListener(getEndDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void getPhoneNumber(String phone) {
        this.phoneNumber = phone;
    }
}
