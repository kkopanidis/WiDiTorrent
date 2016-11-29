package com.asoee.widitorrent.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class RawData extends TransferObject {

    @JsonField
    String base64Data;

    @JsonField
    public String url;

    @JsonField
    String hashData;
}
