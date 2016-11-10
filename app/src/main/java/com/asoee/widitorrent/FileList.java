package com.asoee.widitorrent;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class FileList extends AppCompatActivity implements OnListInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        // Set the adapter
        Context context = findViewById(R.id.file_list).getContext();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.file_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        recyclerView.setAdapter(new MyFileViewAdapter(new ArrayList<Object>(), this));

    }

    @Override
    public void onListInteraction(Object b) {

    }
}
