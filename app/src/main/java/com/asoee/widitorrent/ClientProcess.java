package com.asoee.widitorrent;

import android.util.Log;

import com.asoee.widitorrent.data.ConnectionSpeed;
import com.asoee.widitorrent.data.File;
import com.asoee.widitorrent.data.RequestList;
import com.asoee.widitorrent.data.TransferObject;
import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Salut;

import java.io.IOException;

public class ClientProcess implements ProcessManager {

     Salut newtwork = MainActivity.network;

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
            }else if (newMessage instanceof File){
                download((File) newMessage);
            }
        }
    }

    @Override
    public void respond(Object data) {

    }

    public void checkSpeed() {
        //TODO --->vres to speed s apo tn server kai dimiourgise to Connection speed
        ConnectionSpeed speed = new ConnectionSpeed();
        speed.name = newtwork.thisDevice.readableName;
        //---> vale to speed p ema8es sto speed
        newtwork.sendToHost(speed, new SalutCallback() {
            @Override
            public void call() {
                Log.d("Info", "We failed to send speed.");
            }
        });
    }

    private void download(File file){

        //TODO download the file based on the url specified in file parameter

        //TODO sed the raw data to host

    }
}
