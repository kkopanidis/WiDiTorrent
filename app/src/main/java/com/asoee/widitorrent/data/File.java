package com.asoee.widitorrent.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.peak.salut.SalutDevice;

import java.util.List;

@JsonObject
public class File extends TransferObject {

    @JsonField
    String url;
    @JsonField
    List<String> downloaders;

    {
        this.objectType = this.getClass().toString();
    }


}
