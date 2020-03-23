package com.pratik.bluetoothadhoc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.pratik.bluetoothadhoc.MainActivity.btnFlag;
import static com.pratik.bluetoothadhoc.MainActivity.setButtonText;

public class BluetoothReceiver extends BroadcastReceiver {

    BluetoothHandler btHandler;

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

            btHandler = new BluetoothHandler();
            if (btHandler.getBtAdapter().getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
               btnFlag = 2;
               setButtonText(btnFlag);
            } else if(btHandler.getBtAdapter().getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
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

            } else if (state == BluetoothAdapter.STATE_OFF) {
                btnFlag = 0;
                setButtonText(btnFlag);
                Log.i("asdf", "bt off");
            }

        }

    }
}
