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
    private List<Route> routes;
    private int layout;

    public RouteAdapter(Context context, int layout, List<Route> routes){
        this.context = context;
        this.layout = layout;
        this.routes = routes;
    }

    @Override
    public int getCount() {
        return this.routes.size();
    }

    @Override
    public Object getItem(int i) {
        return this.routes.get(i);
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
        Route route = routes.get(i);
        holder.createdDayTextView.setText(route.getCreatedDay());
        holder.startLocationTextView.setText("Dak Lak");
        holder.endLocationTextView.setText("Binh Dinh");
        holder.distanceTextVew.setText(route.getDistance());
        holder.periodTextView.setText(route.getPeriod());

        return view;
    }
}
