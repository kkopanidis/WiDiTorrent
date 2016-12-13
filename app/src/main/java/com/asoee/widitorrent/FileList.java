package com.asoee.widitorrent;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.asoee.widitorrent.data.ConnectionSpeed;
import com.asoee.widitorrent.data.File;
import com.peak.salut.SalutDevice;

import java.util.ArrayList;
import java.util.List;

public class FileList extends AppCompatActivity implements OnListInteractionListener {

    public static RecyclerView recyclerView, recyclerView2;
    public static OnListInteractionListener listener;
    public static MyFileViewAdapter list_adapter;
    public static DeviceAdapter list_adapter2;
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
        recyclerView2 = (RecyclerView) findViewById(R.id.device_list);
        recyclerView2.setLayoutManager(new LinearLayoutManager(context));
        listener = this;
        list_adapter2 = new DeviceAdapter(new ArrayList<SalutDevice>(), listener);
        recyclerView2.setAdapter(list_adapter2);
        String req = getIntent().getStringExtra("FileUrl");
        if (req != null && req.length() != 0) {
            File tmp = new File();
            tmp.url = req;
            tmp.downloaders = new ArrayList<>();
            tmp.downloaders.add(MainActivity.network.thisDevice.readableName);
            ((ClientProcess) MainActivity.mManager).requestFile(tmp);
        }
    }


    public static void refreshList(File file) {
        list_adapter.refreshList(file);
    }

    public static void refreshDeviceList(List<SalutDevice> devices, List<ConnectionSpeed> speeds) {
        recyclerView2.findViewById(R.id.device_list).setVisibility(View.VISIBLE);
        list_adapter2.refreshList(devices, speeds);
    }

    @Override
    public void onListInteraction(Object b) {
        //---> oti kaneis tik prepei na mpei sto want enw oti kaneis untik prepei na vgei apo to want
    }

    public static void refreshList(List<File> fileList) {
        for (File f : fileList) {
            list_adapter.refreshList(f);
        }
    }
}
