package com.asoee.widitorrent;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.asoee.widitorrent.data.ConnectionSpeed;
import com.asoee.widitorrent.data.File;
import com.asoee.widitorrent.data.RawData;
import com.asoee.widitorrent.data.RequestList;
import com.asoee.widitorrent.utils.Callback;
import com.asoee.widitorrent.utils.FileManager;
import com.asoee.widitorrent.utils.InputStreamVolleyRequest;
import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Salut;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientProcess implements ProcessManager {

    Salut network = MainActivity.network;
    RequestQueue queue;
    List<String> have = new ArrayList<>();
    static RequestList list;
    //File mine;
    Map<String, String> map = new HashMap<>();

    @Override
    public void receive(Object data) {
        Object newMessage =
                null;
        try {
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
            else if (((String) data).contains("DONE"))
                newMessage = "DONE";
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (newMessage instanceof RequestList) {
                list = (RequestList) newMessage;
                FileList.refreshList(((RequestList) newMessage).fileList);
            } else if (newMessage instanceof File) {
                download((File) newMessage);
            } else if (newMessage instanceof RawData) {
                boolean found = false;
                //TODO check it, it enters here while the file does exist
                if (FileList.want.contains(((RawData) newMessage).url)
                        && (!have.contains(((RawData) newMessage).url + "_" + ((RawData) newMessage).part))
                        || !have.contains(((RawData) newMessage).url)) {

                    have.add(((RawData) newMessage).url + "_" + ((RawData) newMessage).part);
                    Commons.writeFile(Base64.decode(((RawData) newMessage).base64Data, Base64.DEFAULT),
                            ((RawData) newMessage).url + "_" + ((RawData) newMessage).part);
                    if (FileManager.handleParts(((RawData) newMessage).url, map.size(), MainActivity.activity)) {
                        have.add(((RawData) newMessage).url);
                    }
                }

            } else if (newMessage instanceof Map) {

                map.putAll((Map<? extends String, ? extends String>) newMessage);
            } else if (newMessage instanceof String) {
                FileList.fileList.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FileList.fileList.onBackPressed();
                    }
                });

            }
        }
    }


    @Override
    public void respond(Object data) {

    }

    public void checkSpeed(final Callback cb) {
        queue = Volley.newRequestQueue(MainActivity.activity);
        final Long time = System.currentTimeMillis();
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET,
                "http://ipv4.download.thinkbroadband.com/1MB.zip",
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        try {
                            if (response != null) {
                                ConnectionSpeed speed = new ConnectionSpeed();
                                speed.name = network.thisDevice.readableName;
                                speed.speed = 8 / ((System.currentTimeMillis() - time) / 1000);
                                network.sendToHost(speed, new SalutCallback() {
                                    @Override
                                    public void call() {
                                        Log.d("Info", "We failed to send speed.");
                                    }
                                });
                                cb.done(true);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                            e.printStackTrace();
                            cb.done(false);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO handle the error
                error.printStackTrace();
                cb.done(false);
            }
        }, null);
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 0f));
        queue.add(request);


    }

    private void download(final File file) {
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET,
                "http://widitorrent.azurewebsites.net",
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        if (response != null) {
                            Toast.makeText(FileList.fileList, "File downloaded", Toast.LENGTH_SHORT).show();
                            have.add(file.url);
                            Commons.writeFile(response, file.url + "_" + file.part);
                            forwardFile(file.url + "_" + file.part);

                            if (!FileList.want.contains(file.url)) {
                                MainActivity.activity.getApplicationContext()
                                        .deleteFile(Commons.normalizeName(file.url) + "_" + file.part);
                            } else {
                                if (FileManager.handleParts(file.url, map.size(), MainActivity.activity)) {
                                    have.add(file.url);
                                }
                            }

                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO handle the error
                        error.printStackTrace();
                        Toast.makeText(FileList.fileList, "Error downloading", Toast.LENGTH_SHORT).show();
                    }
                },
                null) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.putAll(super.getHeaders());
                map.put("part", String.valueOf(file.part));
                map.put("url", file.url);
                map.put("total", String.valueOf(file.downloaders.size()));

                return map;
            }
        };

        queue.add(request);
    }


    private void forwardFile(String name) {
        FileInputStream inputStream;
        RawData data = new RawData();
        try {
            String original = name;
            name = Commons.normalizeName(name);
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
            data.url = original;
            data.base64Data = Base64.encodeToString(array, Base64.DEFAULT);
            MainActivity.network.sendToHost(data, new SalutCallback() {
                @Override
                public void call() {

                }
            });
            Toast.makeText(FileList.fileList, "File sent to host!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }


    }
}
