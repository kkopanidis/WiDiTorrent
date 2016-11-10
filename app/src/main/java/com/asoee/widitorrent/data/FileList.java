package com.asoee.widitorrent.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class FileList {

    //Should contain the file urls as file1.com, file2.com etc
    @JsonField
    public String fileList;

}
