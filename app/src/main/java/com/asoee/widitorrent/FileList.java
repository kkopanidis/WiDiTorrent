package com.asoee.widitorrent;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.asoee.widitorrent.data.File;
import com.asoee.widitorrent.data.RequestList;

import java.util.ArrayList;
import java.util.List;

public class FileList extends AppCompatActivity implements OnListInteractionListener {

    public static RecyclerView recyclerView;
    public static OnListInteractionListener listener;
    public static MyFileViewAdapter list_adapter;
    public static List<File> want = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        // Set the group_list
        Context context = findViewById(R.id.file_list).getContext();
        recyclerView = (RecyclerView) findViewById(R.id.file_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        listener = this;
        list_adapter = new MyFileViewAdapter(new ArrayList<File>(), listener);
        recyclerView.setAdapter(list_adapter);
    }


    public static void refreshList(File file){
        list_adapter.refreshList(file);
    }

    @Override
    public void onListInteraction(Object b) {
        //---> oti kaneis tik prepei na mpei sto want enw oti kaneis untik prepei na vgei apo to want
    }

    public static void refreshList(List<File> fileList) {
        for(File f : fileList){
            list_adapter.refreshList(f);
        }
    }
}
