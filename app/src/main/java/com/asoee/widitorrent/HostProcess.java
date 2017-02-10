package com.asoee.widitorrent;

import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.asoee.widitorrent.data.ConnectionSpeed;
import com.asoee.widitorrent.data.File;
import com.asoee.widitorrent.data.RawData;
import com.asoee.widitorrent.data.RequestList;
import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDevice;

import java.io.IOException;
import java.io.InputStream;
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
    List<String> have = new ArrayList<>();
    private Salut network;

    {
        network = MainActivity.network;
    }


    //TODO ---> i receive object prepei na trexei kapws sinexeia..sto background..auto ginetai tr??
    @Override
    synchronized public void receive(Object data) {

        try {
            Object newMessage = null;
            if (((String) data).contains("speed"))
                newMessage =
                        LoganSquare.parse((String) data, ConnectionSpeed.class);
            else if (((String) data).contains("base64Data"))
                newMessage =
                        LoganSquare.parse((String) data, RawData.class);
            else if (((String) data).contains("fileList"))
                newMessage =
                        LoganSquare.parse((String) data, RequestList.class);
            else if (((String) data).contains("url"))
                newMessage =
                        LoganSquare.parse((String) data, File.class);


            if (newMessage instanceof File) {
                FileList.refreshList((File) newMessage);
                Commons.requestFile(MainActivity.network, null);
            } else if (newMessage instanceof RawData) {
                Looper.prepare();
                Toast.makeText(FileList.fileList, "File data received", Toast.LENGTH_SHORT).show();
                network.sendToAllDevices(newMessage, new SalutCallback() {
                    @Override
                    public void call() {
                        //TODO later
                    }
                });
                if (FileList.want.contains(((RawData) newMessage).url)
                        && (!have.contains(((RawData) newMessage).url + "_" + ((RawData) newMessage).part))
                        || !have.contains(((RawData) newMessage).url))
                    Toast.makeText(FileList.fileList, "File saved", Toast.LENGTH_SHORT).show();
                have.add(((RawData) newMessage).url + "_" + ((RawData) newMessage).part);
                Commons.writeFile(Base64.decode(((RawData) newMessage).base64Data, Base64.DEFAULT),
                        ((RawData) newMessage).url + "_" + ((RawData) newMessage).part);
                handleParts(((RawData) newMessage).url, deviceList.size());
            } else if (newMessage instanceof ConnectionSpeed) {
                speeds.add((ConnectionSpeed) newMessage);
                FileList.refreshDeviceList(deviceList, speeds);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleParts(String url, int total) {
        String[] filenames = MainActivity.activity.getFilesDir().list();
        List<String> files = new ArrayList<>();

        for (String file : filenames) {
            if (file.contains(url)) {
                files.add(file);
            }
        }

        if (files.size() != total) {
            return;
        }

        Collections.sort(files, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int partno1 = Integer.parseInt(lhs.substring(lhs.lastIndexOf('_')));
                int partno2 = Integer.parseInt(rhs.substring(rhs.lastIndexOf('_')));

                if (partno1 > partno2) {
                    return -1; //so as the order be descending
                } else {
                    return 1;
                }
            }
        });

        List<Byte> bytes = new ArrayList<>();
        int c;

        for (String file : files) {

            try (InputStream inputStream = MainActivity.activity
                    .openFileInput(file)) {

                while ((c = inputStream.read()) != -1) {
                    bytes.add((byte) c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                MainActivity.activity.getApplicationContext().deleteFile(file);
            }

        }
        byte[] array = new byte[bytes.size()];
        int i = 0;
        for (Byte b : bytes) {
            array[i++] = b;
        }
        Commons.writeFile(array, url);
        have.add(url);
    }

    @Override
    public void respond(Object data) {
        System.out.println("Hey");

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

        network.sendToDevice(device, FileList.list_adapter.getList(), new SalutCallback() {
            @Override
            public void call() {
                Log.d("Error:", "Failed to send data!");
            }
        });
    }

    // it will be called when the host has locked the group and only after all devices ready or timeout happens
    public void startDownload() {
        //sorts with descending order file list based on the size of each file
        Collections.sort(FileList.list_adapter.getList().fileList, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                if (lhs.size == rhs.size) {
                    return 0;
                } else if (lhs.size > rhs.size) {
                    return -1; //so as the order be descending
                } else {
                    return 1;
                }
            }
        });

        //sorts devices in descending order based on their speed
//        Collections.sort(speeds, new Comparator<ConnectionSpeed>() {
//            @Override
//            public int compare(ConnectionSpeed lhs, ConnectionSpeed rhs) {
//                if (lhs.speed == rhs.speed) {
//                    return 0;
//                } else if (lhs.speed > rhs.speed) {
//                    return -1; //so as the order be descending
//                } else {
//                    return 1;
//                }
//            }
//        });

        //cycles on devices and asks them to download files.
        // //---->Not very efficient we should try something else
        int dev = 0;
        for (File f : FileList.list_adapter.getList().fileList) {
            f.part = 0;
            for (SalutDevice device : deviceMap.values()) {
                network.sendToDevice(device, f, new SalutCallback() {
                    @Override
                    public void call() {
                        Log.e("Info", "Oh no! The data failed to send.");
                    }
                });
                f.part++;
            }
            dev++;
            if (dev >= speeds.size()) {
                dev = 0;
            }
        }
    }

    public void initiateProc() {
        network.sendToAllDevices("GO", new SalutCallback() {
            @Override
            public void call() {
                //TODO later
            }
        });
    }
}
