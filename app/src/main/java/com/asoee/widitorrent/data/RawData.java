package com.asoee.widitorrent.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class RawData extends TransferObject {

    @JsonField
    public String base64Data;

    @JsonField
    public String url;

    @JsonField
    public int part;

    @JsonField
    String hashData;
}
