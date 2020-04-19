package com.pratik.bluetoothadhoc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.pratik.bluetoothadhoc.MainActivity.btnFlag;
import static com.pratik.bluetoothadhoc.MainActivity.faultTolerantAddresss;
import static com.pratik.bluetoothadhoc.MainActivity.realRank;
import static com.pratik.bluetoothadhoc.MainActivity.setButtonText;

public class BluetoothReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Discovery has found a device. Get the BluetoothDevice
            // object and its info from the Intent.
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            assert device != null;
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress(); // MAC address

        } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {


            if (BluetoothAdapter.getDefaultAdapter().getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                btnFlag = 2;
                setButtonText(btnFlag);
            } else if (BluetoothAdapter.getDefaultAdapter().getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                btnFlag = 1;
                setButtonText(btnFlag);
            }

        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);


            if (state == BluetoothAdapter.STATE_ON) {
                btnFlag = 1;
                setButtonText(btnFlag);
                Log.i("asdf", "bt on");
                ManageUUID manageUUID = new ManageUUID();
                for (int i = 0; i < manageUUID.getDummyUuids().size(); i++) {
                    BtAcceptThread thread = new BtAcceptThread(manageUUID.getDummyUuids().get(i).toString());
                    thread.start();
                }

            } else if (state == BluetoothAdapter.STATE_OFF) {
                btnFlag = 0;
                setButtonText(btnFlag);
                Log.i("asdf", "bt off");
            }

        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

            PrefManager prefs = new PrefManager(context);
            if(!prefs.isMyDeviceMaster())
                Toast.makeText(context, "Device disconnected. Reconnecting", Toast.LENGTH_SHORT).show();
            Log.i("asdf", "Device disconnected. Reconnecting");
            if (realRank == 1) {

                List<String> deviceAddr = cleanFTA(faultTolerantAddresss);
                for (String addr : deviceAddr) {

                    if(!addr.equals("")){
                        Log.i("asdf","Device disconnected | addr to connect next " + addr );

                        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);
                        Log.i("asdf", "new master connecting");
                        BtConnectThread thread = new BtConnectThread(device);
                        thread.start();
                    }

                }
            }
        }

    }

    private List<String> cleanFTA(List<String> faultTolerantAddresss) {
        Set<String> set = new HashSet<>(faultTolerantAddresss);
        return new ArrayList<>(set);
    }
}
