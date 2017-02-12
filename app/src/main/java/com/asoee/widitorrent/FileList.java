package com.asoee.widitorrent;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

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
    public static Activity fileList;

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
        fileList = this;
        if (req != null && req.length() != 0) {

            File tmp = new File();
            tmp.url = req;
            tmp.removal = false;
            tmp.downloaders = new ArrayList<>();
            tmp.downloaders.add(MainActivity.network.thisDevice.readableName);
            ((ClientProcess) MainActivity.mManager).mine = tmp;
            Commons.requestFile(MainActivity.network, tmp);
        }

        boolean isHost = getIntent().getBooleanExtra("isHost", false);
        Button ready = (Button)findViewById(R.id.ready_button);
        ready.setEnabled(isHost);

    }


    public static void refreshList(final File file) {
        fileList.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                list_adapter.refreshList(file);
            }
        });
    }

    public static void refreshDeviceList(final List<SalutDevice> devices, final List<ConnectionSpeed> speeds) {
        fileList.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView2.findViewById(R.id.device_list).setVisibility(View.VISIBLE);
                list_adapter2.refreshList(devices, speeds);
            }
        });

    }

    public void onReady(View v) {
        if (MainActivity.network.isRunningAsHost) { //TODO maybe not needed ---> vlepe line 59
            ((HostProcess) MainActivity.mManager).initiateProc();
        } else {

        }
    }

    @Override
    public void onListInteraction(Object b) {
        if (!((File) b).removal)
            want.add((File) b);
        //---> oti kaneis tik prepei na mpei sto want enw oti kaneis untik prepei na vgei apo to want
        Commons.requestFile(MainActivity.network, (File) b);
    }

    public static void refreshList(List<File> fileList) {
        if (list_adapter == null)
            return;
        for (File f : fileList) {
            list_adapter.refreshList(f);
        }
    }
}
