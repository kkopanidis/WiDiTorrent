package com.asoee.widitorrent.data;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.peak.salut.SalutDevice;

import java.util.List;

@JsonObject
public class RequestList extends TransferObject {

    //Should contain the file urls as file1.com, file2.com etc
    @JsonField
    public List<File> fileList;

    public void addFile(File f){
        int i = fileList.indexOf(f);
        if (i > -1) {
            for(String downloader : f.downloaders) {
                if(!fileList.get(i).downloaders.contains(downloader))
                    fileList.get(i).downloaders.add(downloader);
            }
        } else {
            fileList.add(f);
        }
    }

}