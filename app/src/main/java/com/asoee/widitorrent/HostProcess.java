package com.asoee.widitorrent;

import android.util.Log;

import com.asoee.widitorrent.data.File;
import com.asoee.widitorrent.data.RawData;
import com.asoee.widitorrent.data.RequestList;
import com.asoee.widitorrent.data.TransferObject;
import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HostProcess implements ProcessManager, SalutDeviceCallback {

    List<SalutDevice> deviceList = new ArrayList<>();
    Map<String, SalutDevice> deviceMap = new HashMap<>();
    RequestList list;
    private Salut network;

    {
        list = new RequestList();
        list.fileList = new ArrayList<>();
        network = MainActivity.network;
    }


    //TODO
    @Override
    public void receive(Object data) {

        try {
            TransferObject newMessage =
                    LoganSquare.parse((String) data, TransferObject.class);

            if (newMessage instanceof File) {
                int i = list.fileList.indexOf(newMessage);
                // NEW FILE REQUEST
                if (i > -1) {
                    if (list.fileList.get(i).downloaders.contains(
                            ((File) newMessage).downloaders.get(0)))
                        return;
                    list.fileList.get(i).downloaders.addAll(((File) newMessage).downloaders);
                } else {
                    list.fileList.add((File) newMessage);

                }
            } else if (newMessage instanceof RawData) {
                network.sendToAllDevices(newMessage, new SalutCallback() {
                    @Override
                    public void call() {
                            //TODO later
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void respond(Object data) {

    }

    @Override
    public void call(SalutDevice device) {
        Log.d("Connection info:", device.readableName + " has connected!");
        if (!deviceMap.containsKey(device.readableName)) {
            deviceMap.put(device.readableName, device);
            deviceList.add(device);
            refreshRouting();
        }

        sendInfo(device);
    }

    private void refreshRouting() {

        network.sendToAllDevices(deviceMap, new SalutCallback() {
            @Override
            public void call() {
                Log.d("Error:", "Failed to send data!");
            }
        });
    }


    private void sendInfo(SalutDevice device) {

        network.sendToDevice(device, list, new SalutCallback() {
            @Override
            public void call() {
                Log.d("Error:", "Failed to send data!");
            }
        });
    }


}
