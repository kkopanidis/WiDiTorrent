package com.asoee.widitorrent;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.asoee.widitorrent.data.File;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Salut;

import java.io.FileOutputStream;

public class Commons {


    public static void requestFile(Salut network, File f) {
        if (network.isRunningAsHost) {
            network.sendToAllDevices(((MyFileViewAdapter) FileList.recyclerView.getAdapter())
                    .getList(), new SalutCallback() {
                @Override
                public void call() {
                    Log.d("Error:", "Failed to send data!");
                }
            });
        } else
            network.sendToHost(f, new SalutCallback() {
                @Override
                public void call() {
                    Log.d("Info", "We failed to send speed.");
                }
            });
    }

    public static boolean writeFile(byte[] data, String name) {

        FileOutputStream outputStream;
        boolean written = false;
        try {
            name = normalizeName(name);
            outputStream = MainActivity.activity
                    .openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(data);
            outputStream.close();
            written = true;
            
            Toast.makeText(FileList.fileList, "File saved to device", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return written;


    }

    public static String normalizeName(String name) {
        return name.replaceAll("\\W", "_");
    }
}
