package com.asoee.widitorrent;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.asoee.widitorrent.data.ConnectionSpeed;
import com.asoee.widitorrent.data.File;
import com.asoee.widitorrent.data.RawData;
import com.asoee.widitorrent.data.RequestList;
import com.asoee.widitorrent.data.TransferObject;
import com.asoee.widitorrent.utils.InputStreamVolleyRequest;
import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Salut;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientProcess implements ProcessManager {

    Salut network = MainActivity.network;
    RequestQueue queue;
    List<String> have = new ArrayList<>();
    List<File> want = new ArrayList<>();
    static RequestList list;

    @Override
    public void receive(Object data) {
        TransferObject newMessage =
                null;
        try {
            newMessage = LoganSquare.parse((String) data, TransferObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (newMessage instanceof RequestList) {
                //TODO ---> mallon xreiazetai mono gia na ta kaneis display stn o8oni????
                list = (RequestList) newMessage;
                FileList.refreshList(((RequestList) newMessage).fileList);
                want = new ArrayList<>();
                for (File file : list.fileList) {
                    for (String down : file.downloaders) {
                        if (down.equals(network.thisDevice.readableName)) {
                            want.add(file);
                        }
                    }
                }
            } else if (newMessage instanceof File) {
                download((File) newMessage);
            } else if (newMessage instanceof RawData) {

                boolean found = false;
                if (want.contains(((RawData) newMessage).url)
                        && !have.contains(((RawData) newMessage).url))
                    writeFile(Base64.decode(((RawData) newMessage).base64Data, Base64.DEFAULT),
                            ((RawData) newMessage).url);

            }
        }
    }

    @Override
    public void respond(Object data) {

    }

    public void checkSpeed() {
        //TODO --->vres to speed s apo tn server kai dimiourgise to Connection speed
        queue = Volley.newRequestQueue(MainActivity.activity);
        final Long time = System.currentTimeMillis();
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET,
                "http://ipv4.download.thinkbroadband.com/5MB.zip",
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        try {
                            if (response != null) {
                                ConnectionSpeed speed = new ConnectionSpeed();
                                speed.name = network.thisDevice.readableName;
                                speed.speed = 5 * 8 / (System.currentTimeMillis() - time);

                                network.sendToHost(speed, new SalutCallback() {
                                    @Override
                                    public void call() {
                                        Log.d("Info", "We failed to send speed.");
                                    }
                                });
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO handle the error
                error.printStackTrace();
            }
        }, null);
        queue.add(request);


    }

    private void download(final File file) {

        //TODO download the file based on the url specified in file parameter

        //TODO sed the raw data to host
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, file.url,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        if (response != null) {
                            writeFile(response, file.url);
                            sendFile(file.url);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO handle the error
                error.printStackTrace();
            }
        }, null);
        queue.add(request);
    }


    private boolean writeFile(byte[] data, String name) {
        have.add(name);
        FileOutputStream outputStream;
        boolean written = false;
        try {
            outputStream = MainActivity.activity
                    .openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(data);
            outputStream.close();
            written = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return written;


    }

    private void sendFile(String name) {
        FileInputStream inputStream;
        RawData data = new RawData();
        try {
            inputStream = MainActivity.activity
                    .openFileInput(name);
            int c;
            List<Byte> bytes = new ArrayList<>();
            while ((c = inputStream.read()) != -1) {
                bytes.add((byte) c);
            }
            byte[] array = new byte[bytes.size()];
            int i = 0;
            for (Byte b : bytes) {
                array[i++] = b;
            }
            inputStream.close();
            data.base64Data = Base64.encodeToString(array, Base64.DEFAULT);
            MainActivity.network.sendToHost(data, new SalutCallback() {
                @Override
                public void call() {

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }


    }
}
