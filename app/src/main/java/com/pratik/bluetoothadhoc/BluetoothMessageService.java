package com.pratik.bluetoothadhoc;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class BluetoothMessageService {
    private static final String TAG = "asdf";
    private Handler handler; // handler that gets info from Bluetooth service
    private ConnectedThread thread;

    public static Map<String, Integer> deviceRanking = new HashMap<>();


    public void connectService(BluetoothSocket socket) {

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == MessageConstants.MESSAGE_READ) {
                    Object obj = msg.obj;
                    byte[] message = (byte[]) obj;
                    String str = new String(message);
                    //Log.i("asdf",str);

                    String[] strings = new String[3];
                    String[] split = str.split("\0");
                    strings[0] = split[0].substring(0, 7);
                    strings[1] = split[0].substring(8, 9);
                    strings[2] = split[0].substring(10);
                    Log.i("asdf", "device freq:" + strings[0]);
                    Log.i("asdf", "device max cores:" + strings[1]);
                    Log.i("asdf", "device name:" + strings[2]);
                    rankDevice(strings[0], strings[1], strings[2]);
                }

            }

        };

        thread = new ConnectedThread(socket);
        thread.start();
    }

    private void rankDevice(String cpuFreq, String cores, String deviceName) {

        int size = deviceRanking.size();




    }

    public void sendMessage(String deviceName) {
        DeviceProps props = new DeviceProps();
        String msg = props.getMaxFreq() + " " + props.getNumberOfCores() + " " + deviceName + "\0";
        byte[] message = msg.getBytes();
        thread.write(message, msg);
    }

    private interface MessageConstants {
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
            mmBuffer = new byte[1024];
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