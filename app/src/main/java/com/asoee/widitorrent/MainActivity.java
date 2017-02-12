package com.asoee.widitorrent;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.asoee.widitorrent.net_devices.NetDevices;
import com.asoee.widitorrent.utils.Callback;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.util.Random;


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
        SalutDataReceiver dataReceiver = new SalutDataReceiver(this, new SalutDataRec() {
            @Override
            public void onDataReceived(final Object o) {
                if (mManager != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mManager.receive(o);
                        }
                    }).start();

                } else
                    super.onDataReceived(o);
            }
        });

        //Contains the instance name
        SalutServiceData serviceData = new SalutServiceData("Download", 50489, "WiDi" + new Random()
                .nextInt(256));

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
                group_list.add();
            }
        }, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!network.isDiscovering && !network.isRunningAsHost)
            network.discoverNetworkServices(new SalutDeviceCallback() {
                @Override
                public void call(SalutDevice salutDevice) {
                    Log.d("Connection info:", salutDevice.readableName + "found!");
                    NetDevices.addItem(salutDevice);
                    group_list.add();
                }
            }, true);

    }

    public void refresh(View v) {
        NetDevices.ITEMS.clear();
        NetDevices.ITEM_MAP.clear();
        group_list.add();
        network.stopServiceDiscovery(true);
        network.discoverNetworkServices(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {
                Log.d("Connection info:", salutDevice.readableName + "found!");
                NetDevices.addItem(salutDevice);
                group_list.add();
            }
        }, true);
    }

    @Override
    public void onListInteraction(Object b) {

        chosenGroup = (SalutDevice) b;
        verify_connection();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (network.isDiscovering)
            network.stopServiceDiscovery(true);
        if (network.isRunningAsHost)
            network.stopNetworkService(false);
    }

    // this prompts you to verify that you actually want to connect to the chosen group
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
                                chosenGroup = null;
                                dialog.cancel();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    // connects your device to the chosen group.
    // after connecting, your device becomes a client of the group
    // and informs its owner about its speed
    private void connect_to_group() {
        //Register on the selected network
        network.registerWithHost(chosenGroup, new SalutCallback() {
            @Override
            public void call() {
                mManager = new ClientProcess();
                final android.app.AlertDialog dialog = new ProgressDialog
                        .Builder(MainActivity.this)
                        .setTitle("Testing speed please wait..")
                        .setCancelable(false)
                        .create();
                dialog.show();
                ((ClientProcess) mManager).checkSpeed(new Callback() {
                    @Override
                    public void done(boolean res) {
                        dialog.dismiss();
                        ask_for_file();
                    }
                });
                Log.d("Info", "We're now registered.");


            }
        }, new SalutCallback() {
            @Override
            public void call() {
                Log.d("Info", "We failed to register.");
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Failed!");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }


    // prompts you to enter the url of a file you want to download
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
                        show_group(((EditText) diag.findViewById(R.id.file_select))
                                .getText().toString(), false);
                    }
                });
                diag.findViewById(R.id.button_skip).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diag.hide();
                        show_group(null, false);
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

    // prompts you to enter a name for the group you want to create
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
                        String group_name = ((EditText) diag.findViewById(R.id.groupField)).getText().toString();

                        // this node is now host
                        become_host(group_name);

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

    private void become_host(String group_name) {
        //If the device already IS a host
        if (network.isRunningAsHost) {
            return;
        }

        mManager = new HostProcess();
        network.startNetworkService((HostProcess) mManager);
        network.thisDevice.readableName = group_name;
        show_group(null, true);
    }

    private void show_group(String url, boolean isHost) {
        Intent intent = new Intent(MainActivity.this, FileList.class);
        intent.putExtra("FileUrl", url);
        intent.putExtra("isHost", isHost);
        startActivity(intent);
    }

}
