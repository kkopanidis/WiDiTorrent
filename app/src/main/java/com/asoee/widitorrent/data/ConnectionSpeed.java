package com.asoee.widitorrent.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by p3130052 on 7/12/2016.
 */
@JsonObject
public class ConnectionSpeed extends TransferObject {

    @JsonField
    public double speed; //speed of the device connection
    //--->maybe other info too
    @JsonField
    public String name; //unique for each device
}
