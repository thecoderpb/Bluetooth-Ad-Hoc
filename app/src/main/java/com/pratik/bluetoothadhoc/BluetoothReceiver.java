package com.pratik.bluetoothadhoc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.pratik.bluetoothadhoc.MainActivity.btnFlag;
import static com.pratik.bluetoothadhoc.MainActivity.createThreads;
import static com.pratik.bluetoothadhoc.MainActivity.deviceReadyList;
import static com.pratik.bluetoothadhoc.MainActivity.deviceReadyListAdapter;
import static com.pratik.bluetoothadhoc.MainActivity.dialog;
import static com.pratik.bluetoothadhoc.MainActivity.faultTolerantAddresss;
import static com.pratik.bluetoothadhoc.MainActivity.getPairedDevices;
import static com.pratik.bluetoothadhoc.MainActivity.pairedListAdapter;
import static com.pratik.bluetoothadhoc.MainActivity.rankText;
import static com.pratik.bluetoothadhoc.MainActivity.realRank;
import static com.pratik.bluetoothadhoc.MainActivity.releaseThreads;
import static com.pratik.bluetoothadhoc.MainActivity.setButtonText;
import static com.pratik.bluetoothadhoc.MainActivity.viewModel;

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
                pairedListAdapter.clear();
                pairedListAdapter.addAll(getPairedDevices().keySet());
                pairedListAdapter.addAll();
                pairedListAdapter.notifyDataSetChanged();
                createThreads();

            } else if (state == BluetoothAdapter.STATE_OFF) {
                btnFlag = 0;
                setButtonText(btnFlag);
                Log.i("asdf", "bt off");
                dialog.show();
            }

        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

            releaseThreads();

            rankText.setText("Device Ready");

            viewModel.nukeTable();

            createThreads();

            PrefManager prefs = new PrefManager(context);
            if (!prefs.isMyDeviceMaster() && prefs.isMyDeviceSlave() && realRank != 1) { //proper slave
                Toast.makeText(context, "Device disconnected. Reconnecting", Toast.LENGTH_SHORT).show();
                connectService(context, prefs.getMasterMacAddress());
            }

            Log.i("asdf", "Device disconnected. Reconnecting");
            if (realRank == 1) { //next new master in charge

                List<String> deviceAddr = cleanFTA(faultTolerantAddresss);
                for (String addr : deviceAddr) {

                    if (!addr.equals("")) {
                        Log.i("asdf", "Device disconnected | addr to connect next " + addr);
                        connectService(context,addr);
                    }

                }
            }
        }

    }

    private void connectService(Context context, String addr) {

        try {
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr);
            Log.i("asdf", "new master connecting");
            BtConnectThread thread = new BtConnectThread(device);
            thread.start();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Failed to connect to devices. Restart clusters", Toast.LENGTH_SHORT).show();
            releaseThreads();
            deviceReadyList.clear();
            deviceReadyListAdapter.notifyDataSetChanged();
            createThreads();
        }




    }

    private void freeThreads(Map<String, BluetoothSocket> DeviceIdList) throws IOException {
        List<BluetoothSocket> sockets = new ArrayList<>(DeviceIdList.values());
        for (BluetoothSocket soc : sockets) {
            soc.close();
        }

    }


    private List<String> cleanFTA(List<String> faultTolerantAddresss) {
        Set<String> set = new HashSet<>(faultTolerantAddresss);
        return new ArrayList<>(set);
    }
}
