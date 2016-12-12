package com.asoee.widitorrent;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.asoee.widitorrent.data.File;

import java.util.ArrayList;
import java.util.List;

public class FileList extends AppCompatActivity implements OnListInteractionListener {

    public static RecyclerView recyclerView;
    public static OnListInteractionListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        // Set the group_list
        Context context = findViewById(R.id.file_list).getContext();
        recyclerView = (RecyclerView) findViewById(R.id.file_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        listener = this;
        recyclerView.setAdapter(new MyFileViewAdapter(new ArrayList<File>(), listener));
    }


    public static void refreshList(List<File> files){
        recyclerView.setAdapter(new MyFileViewAdapter(files, listener));
    }

    @Override
    public void onListInteraction(Object b) {

    }
}
