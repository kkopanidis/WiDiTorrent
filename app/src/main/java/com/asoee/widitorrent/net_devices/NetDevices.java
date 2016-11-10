package com.asoee.widitorrent.net_devices;

import com.peak.salut.SalutDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NetDevices {

    public static final List<SalutDevice> ITEMS = new ArrayList<>();

    public static final Map<String, SalutDevice> ITEM_MAP = new HashMap<>();

    public static void addItem(SalutDevice item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.deviceName, item);
    }

    public static void clear() {
        ITEM_MAP.clear();
        ITEMS.clear();
    }
}
