package com.example.pratham.jsoup;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class Report extends AppCompatActivity {

    RecyclerView recycler;
    FloatingActionButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        recycler = findViewById(R.id.recycler);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String id = getIntent().getStringExtra("id");
        CustomGridLayoutManager customGridLayoutManager = new CustomGridLayoutManager(getApplicationContext());
        customGridLayoutManager.setScrollEnabled(false);
        recycler.setLayoutManager(customGridLayoutManager);
        ReportAdapter adapter = new ReportAdapter(getApplicationContext(),id);
        recycler.setAdapter(adapter);
    }
}
