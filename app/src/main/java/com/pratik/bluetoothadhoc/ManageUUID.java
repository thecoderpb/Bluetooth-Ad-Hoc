package com.pratik.bluetoothadhoc;

import android.bluetooth.BluetoothAdapter;
import android.os.ParcelUuid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ManageUUID {

    static final String MY_UUID = "00001000-0000-1000-8000-00805f9b34fb";

    ParcelUuid[] getUUIDs() {

        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            return (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

}
