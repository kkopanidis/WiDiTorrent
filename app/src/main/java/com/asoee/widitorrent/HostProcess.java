package com.asoee.widitorrent;

import android.util.Log;

import com.asoee.widitorrent.data.ConnectionSpeed;
import com.asoee.widitorrent.data.File;
import com.asoee.widitorrent.data.RawData;
import com.asoee.widitorrent.data.RequestList;
import com.asoee.widitorrent.data.TransferObject;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.typeconverters.DoubleBasedTypeConverter;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HostProcess implements ProcessManager, SalutDeviceCallback {

    List<SalutDevice> deviceList = new ArrayList<>(); //registeredDevices
    List<ConnectionSpeed> speeds = new ArrayList<>();
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
            }else if (newMessage instanceof ConnectionSpeed){
                speeds.add((ConnectionSpeed) newMessage);
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

    // it will be called when the host has locked the group and only after all devices ready or timeout happens
    public void startDownload(){
        //sorts with descending order file list based on the size of each file
        Collections.sort(list.fileList, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if( lhs.size == rhs.size){
                    return  0;
                }else if (lhs.size > rhs.size){
                    return -1; //so as the order be descending
                }else{
                    return 1;
                }
            }
        });

        //sorts devices in descending order based on their speed
        Collections.sort(speeds, new Comparator<ConnectionSpeed>() {
            @Override
            public int compare(ConnectionSpeed lhs, ConnectionSpeed rhs) {
                if( lhs.speed == rhs.speed){
                    return  0;
                }else if (lhs.speed > rhs.speed){
                    return -1; //so as the order be descending
                }else{
                    return 1;
                }
            }
        });

        //cycles on devices and asks them to download files.
        // //---->Not very efficient we should try something else
        int dev = 0;
        for(File f : list.fileList){
            network.sendToDevice(deviceMap.get(speeds.get(dev).name), f, new SalutCallback() {
                @Override
                public void call() {
                    Log.e("Info", "Oh no! The data failed to send.");
                }
            });
            dev++;
            if(dev >= speeds.size()){
                dev = 0;
            }
        }
    }


}
