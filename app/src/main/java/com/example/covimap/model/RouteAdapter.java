package com.example.covimap.model;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.covimap.R;

import java.util.List;

public class RouteAdapter extends BaseAdapter {
    private class ViewHolder{
        private TextView createdDayTextView;
        private TextView startLocationTextView;
        private TextView endLocationTextView;
        private TextView distanceTextVew;
        private TextView periodTextView;
    }

    private Context context;
    private List<RouteLabel> routeLabels;
    private int layout;

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

            // mapping attributes
            holder.createdDayTextView = (TextView) view.findViewById(R.id.created_day_text_view);
            holder.startLocationTextView = (TextView) view.findViewById(R.id.strart_point_text_view);
            holder.endLocationTextView = (TextView) view.findViewById(R.id.end_point_text_view);
            holder.distanceTextVew = (TextView) view.findViewById(R.id.distance_text_view_item);
            holder.periodTextView = (TextView) view.findViewById(R.id.period_text_view_item);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }
        // assignment values
        RouteLabel routeLabel = routeLabels.get(i);
        holder.createdDayTextView.setText(routeLabel.getCreatedDay());
        holder.startLocationTextView.setText(routeLabel.getStartAddress());
        holder.endLocationTextView.setText(routeLabel.getEndAddress());
        holder.distanceTextVew.setText(routeLabel.getDistance());
        holder.periodTextView.setText(routeLabel.getPeriod());

        return view;
    }
}
