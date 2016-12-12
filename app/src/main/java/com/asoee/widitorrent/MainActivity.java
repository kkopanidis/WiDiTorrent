package com.asoee.widitorrent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
    MyItemRecyclerViewAdapter group_list;
    SalutDevice chosenGroup;
    static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.fab).setOnClickListener(this);


        // Set the group_list
        Context context = findViewById(R.id.content_main).getContext();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.content_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        group_list = new MyItemRecyclerViewAdapter(NetDevices.ITEMS, this);
        recyclerView.setAdapter(group_list);
        MainActivity.activity = this;

        //Salut Config here
        config_salut();

    }

    private void config_salut() {

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
                group_list.add(salutDevice);
            }
        }, true);
    }

    @Override
    public void onListInteraction(Object b) { //---> otan klikareis tn lista prospa8eis na sinde8eis se kapoia omada

        chosenGroup = (SalutDevice) b;
        verify_connection();

    }

    private void verify_connection() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.connection_prompt);
                builder.setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                connect_to_group();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show(); //---> den eimai sigouri an xreiazetai
            }
        });
    }

    private void connect_to_group() {
        //Register on the selected network
        network.registerWithHost(chosenGroup, new SalutCallback() {
            @Override
            public void call() {
                mManager = new ClientProcess();
                ((ClientProcess) mManager).checkSpeed();
                Log.d("Info", "We're now registered.");
                ask_for_file();
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                Log.d("Info", "We failed to register.");
            }
        });
    }


    private void ask_for_file() {
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

    //When the fab is clicked, it immediatly starts a new service
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {

            //If the device is looking for networks to join
            if (network.isDiscovering) {
                network.stopServiceDiscovery(true);
            }

            ask_group_name();

        }
    }

    private void ask_group_name() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog diag = new Dialog(MainActivity.this);
                diag.setContentView(R.layout.new_group);
                diag.findViewById(R.id.okBttn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get the input and set it as group name
                        String group_name = ((TextView) findViewById(R.id.groupField)).getText().toString();

                        // create the group / update groups' list


                        // this node is now host
                        become_host();

                        //TODO redirect to some other view maybe or not
                    }
                });
                diag.findViewById(R.id.cancelBttn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diag.hide();
                    }
                });
                diag.show();
            }
        });
    }

    private void become_host() {
        //If the device already IS a host
        if (network.isRunningAsHost) {
            return;
        }

        mManager = new HostProcess();
        network.startNetworkService((HostProcess) mManager);
    }


}
