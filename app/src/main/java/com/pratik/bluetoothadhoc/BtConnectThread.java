package com.pratik.bluetoothadhoc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;


public class BtConnectThread extends Thread {

    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothHandler btHandler;
    private BluetoothAdapter btAdapter;

    public BtConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        btHandler = new BluetoothHandler();
        btAdapter = btHandler.getBtAdapter();
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's getUUIDs string, also used in the server code.
            ManageUUID manageUUID = new ManageUUID();
            for(int i=0;i<manageUUID.getDummyUuids().size();i++){
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(manageUUID.getDummyUuids().get(i).toString()));
            }

        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @Override
    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        btAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            Log.i("asdf","connecting");
            mmSocket.connect();
            Log.i("asdf","connected");
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        manageConnectedSocket(mmSocket);
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {


        BluetoothMessageService service = new BluetoothMessageService();
        service.connectService(mmSocket);

    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }


}
