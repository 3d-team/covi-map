package com.example.covimap.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.covimap.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RenderHistoryItemActivity extends Activity {
    private FloatingActionButton closeBtn;
    private TextView createdDateTV;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_journey_item_activity);
        closeBtn = (FloatingActionButton) findViewById(R.id.close_render_history_item);
        createdDateTV = (TextView) findViewById(R.id.created_day_text_view);
        createdDateTV.setText("ABC");
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
