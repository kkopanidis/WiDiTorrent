package com.asoee.widitorrent.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

@JsonObject
public class RequestList extends TransferObject {

    //Should contain the file urls as file1.com, file2.com etc
    @JsonField
    public List<File> fileList;
}