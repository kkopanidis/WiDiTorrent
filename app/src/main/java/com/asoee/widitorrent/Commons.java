package com.asoee.widitorrent;


import android.content.Context;
import android.util.Log;

import com.asoee.widitorrent.data.File;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Salut;

import java.io.FileOutputStream;
import java.io.IOException;

public class Commons {


    public static void requestFile(Salut network, File f) {
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
}
