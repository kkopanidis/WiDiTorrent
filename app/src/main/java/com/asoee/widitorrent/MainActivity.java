package com.asoee.widitorrent;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.asoee.widitorrent.net_devices.NetDevices;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;


public class MainActivity extends AppCompatActivity implements OnListInteractionListener, View.OnClickListener {

    public static Salut network;
    public static ProcessManager mManager;
    MyItemRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.fab).setOnClickListener(this);


        // Set the adapter
        Context context = findViewById(R.id.content_main).getContext();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.content_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new MyItemRecyclerViewAdapter(NetDevices.ITEMS, this);
        recyclerView.setAdapter(adapter);


        //Salut Config here
        //Will trigger the data received callback
        SalutDataReceiver dataReceiver = new SalutDataReceiver(this, new SalutDataRec());

        //Contains the instance name
        SalutServiceData serviceData = new SalutServiceData("Download", 50489, "Instance1");

        //Instantiate the service
        network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                Log.e("Error:", "Sorry, but this device does not support WiFi Direct.");
            }
        });


        network.discoverNetworkServices(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {
                Log.d("Connection info:", salutDevice.readableName + "found!");
                NetDevices.addItem(salutDevice);
                adapter.notifyDataSetChanged();
            }
        }, true);

    }

    @Override
    public void onListInteraction(Object b) { //---> otan klikareis tn lista prospa8eis na sinde8eis se kapoia omada
        //Register on the selected network
        network.registerWithHost((SalutDevice) b, new SalutCallback() {
            @Override
            public void call() {
                mManager = new ClientProcess();
                ((ClientProcess)mManager).checkSpeed();
                Log.d("Info", "We're now registered.");
                //After register display the dialog asking abou the file
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Dialog diag = new Dialog(MainActivity.this);
                        diag.setContentView(R.layout.dialog_layout);
                        diag.findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                diag.hide();
                            }
                        });
                        diag.findViewById(R.id.button_skip).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                diag.hide();
                            }
                        });
                        diag.show();
                    }
                });
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                Log.d("Info", "We failed to register.");
            }
        });

    }

    //When the fab is clicked, it immediatly starts a new service
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {

            //If the device is looking for networks to join
            if (network.isDiscovering) {
                network.stopServiceDiscovery(true);
            }

            //TODO add toast here
            //If the device already IS a host
            if (network.isRunningAsHost) {
                return;
            }

            mManager = new HostProcess();
            network.startNetworkService((HostProcess) mManager);

            //---> den eimai sigouri an ananewnetai i lista

        }
    }
}
