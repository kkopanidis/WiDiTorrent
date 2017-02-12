package com.asoee.widitorrent.utils;

import android.app.Activity;

import com.asoee.widitorrent.Commons;
import com.asoee.widitorrent.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gina4_000 on 12/2/2017.
 */

public class FileManager {


    public static boolean handleParts(String url, int total, Activity main ) {
        String[] filenames = main.getFilesDir().list();
        List<String> files = new ArrayList<>();

        for (String file : filenames) {
            if (file.contains(url)) {
                files.add(file);
            }
        }

        if (files.size() != total) {
            return false;
        }

        Collections.sort(files, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int partno1 = Integer.parseInt(lhs.substring(lhs.lastIndexOf('_')));
                int partno2 = Integer.parseInt(rhs.substring(rhs.lastIndexOf('_')));

                if (partno1 > partno2) {
                    return -1; //so as the order be descending
                } else {
                    return 1;
                }
            }
        });

        List<Byte> bytes = new ArrayList<>();
        int c;

        for (String file : files) {

            try (InputStream inputStream = main
                    .openFileInput(file)) {

                while ((c = inputStream.read()) != -1) {
                    bytes.add((byte) c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                main.getApplicationContext().deleteFile(file);
            }

        }
        byte[] array = new byte[bytes.size()];
        int i = 0;
        for (Byte b : bytes) {
            array[i++] = b;
        }
        Commons.writeFile(array, url);

        return true;
    }
}
