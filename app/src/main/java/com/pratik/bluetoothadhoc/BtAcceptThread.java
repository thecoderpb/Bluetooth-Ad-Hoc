package com.pratik.bluetoothadhoc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static android.provider.CalendarContract.Calendars.NAME;
import static com.pratik.bluetoothadhoc.MainActivity.remoteAcceptDeviceIdList;


public class BtAcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    public static final String TAG ="asdf";
    private Handler handler;

    public BtAcceptThread(String Uuid) {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.


            tmp = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(NAME, UUID.fromString(Uuid));
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                Log.i("asdf","listening");
                socket = mmServerSocket.accept();
                Log.i("asdf","listened");
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                manageConnectedSocket(socket);
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {

        BluetoothMessageService service = new BluetoothMessageService();
        service.connectService(socket);
        remoteAcceptDeviceIdList.put(socket.getRemoteDevice().getName(),socket);
        Log.i("asdff","connect device name " + BluetoothAdapter.getDefaultAdapter().getName());
        service.sendMessage(BluetoothAdapter.getDefaultAdapter().getName(),socket.getRemoteDevice().getName(),socket.getRemoteDevice().getAddress());


    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}