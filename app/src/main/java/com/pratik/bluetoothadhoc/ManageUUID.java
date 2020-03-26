package com.pratik.bluetoothadhoc;

import android.bluetooth.BluetoothAdapter;
import android.os.ParcelUuid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

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

    public ArrayList<UUID> getDummyUuids(){

       ArrayList<UUID> mUuidList;
       mUuidList = new ArrayList<>();

       mUuidList.add(UUID.fromString("a60f35f0-b93a-11de-8a39-08002009c666"));
       mUuidList.add(UUID.fromString("54d1cc90-1169-11e2-892e-0800200c9a66"));
       mUuidList.add(UUID.fromString("6acffcb0-1169-11e2-892e-0800200c9a66"));
       mUuidList.add(UUID.fromString("7b977d20-1169-11e2-892e-0800200c9a66"));
       mUuidList.add(UUID.fromString("815473d0-1169-11e2-892e-0800200c9a66"));
       mUuidList.add(UUID.fromString("503c7434-bc23-11de-8a39-0800200c9a66"));
       mUuidList.add(UUID.fromString("503c7435-bc23-11de-8a39-0800200c9a66"));

        return mUuidList;
    }

}
