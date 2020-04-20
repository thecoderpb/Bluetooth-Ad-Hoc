package com.pratik.bluetoothadhoc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import static com.pratik.bluetoothadhoc.MainActivity.handler;
import static com.pratik.bluetoothadhoc.MainActivity.isShowAlert;

public class BluetoothMessageService {
    private static final String TAG = "asdf";
     // handler that gets info from Bluetooth service
    private ConnectedThread thread;
    static String remoteDeviceName,remoteDeviceAddress;

    public static Map<String, Integer> deviceRanking = new HashMap<>();


    public void connectService(BluetoothSocket socket) {

        thread = new ConnectedThread(socket);
        thread.start();
    }


    public void sendMessage(String deviceName,String remoteDeviceName,String remoteDeviceAddress) {
        BluetoothMessageService.remoteDeviceName = remoteDeviceName;
        BluetoothMessageService.remoteDeviceAddress = remoteDeviceAddress;
        DeviceProps props = new DeviceProps();
        String msg = "\f" + props.getMaxFreq() + "\b" + props.getNumberOfCores() + "\b" + deviceName +"\b" + props.getGPUinfo() +"\0";
        byte[] message = msg.getBytes();
        isShowAlert = true;
        thread.write(message, msg);
    }

    public void sendRanking(int rank){
        String msg = "\rRank " + rank +"\0";
        byte[] message = msg.getBytes();
        isShowAlert = false;
        thread.write(message,msg);
    }

    public void sendData(List<String> macAddress){

        String message = "\t";
        for( String str : macAddress){
             message = message + str +"\t";
        }
        message+="\0";
        byte[] msg = message.getBytes();
        thread.write(msg,message);
        Log.i("asdf","next potential ranked device details sent");
    }


    private List<String> getPairedDevices() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        List<String> macAddress = new ArrayList<>();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
               macAddress.add(device.getAddress());
            }
        }
        return macAddress;

    }

    public void sendBroadcast() {

        String message = "\bMessage received from master\0";
        byte[] msg = message.getBytes();
        thread.write(msg,message);

    }

    public interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;


    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[2048];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);

                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes, String msg) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);

                writtenMsg.sendToTarget();
                Log.i("asdf", "message sent");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}