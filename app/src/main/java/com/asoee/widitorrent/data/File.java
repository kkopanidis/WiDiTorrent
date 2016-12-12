package com.asoee.widitorrent.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.peak.salut.SalutDevice;

import java.util.List;

@JsonObject
public class File extends TransferObject {

    @JsonField
    public String url;

    @JsonField
    public List<String> downloaders;

    public int size;

    @Override
    public boolean equals(Object o) {
        if (o instanceof String) {
            return url.equals(o);
        } else
            return (o instanceof File) && this.url.equals(((File) o).url);
    }


}
