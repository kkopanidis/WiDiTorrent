package com.asoee.widitorrent;

import com.asoee.widitorrent.data.RequestList;
import com.asoee.widitorrent.data.TransferObject;
import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

public class ClientProcess implements ProcessManager {


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
                //Should be true
            }
        }
    }

    @Override
    public void respond(Object data) {

    }
}
