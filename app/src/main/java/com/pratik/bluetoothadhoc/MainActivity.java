package com.pratik.bluetoothadhoc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_ENABLE_BT = 1;

    static int ConnectedDeviceCount = 0;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        try {
            System.load("/system/vendor/lib/libOpenCL.so");
            Log.i("asdf", "Library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            Log.i("asdf", "failed to find lib");
        }

    }

    Map<String, String> masterProp;

    private BluetoothAdapter btAdapter;
    private BroadcastReceiver receiver;
    private static Button btButton;
    static int btnFlag = 0;
    private DeviceProps deviceProps;

    ArrayList<String> pairedList, deviceReadyList;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothHandler btHandler = new BluetoothHandler();
        btAdapter = btHandler.getBtAdapter();
        if(btAdapter == null){
            Toast.makeText(this, "Bluetooth does not exits. App cannot run", Toast.LENGTH_LONG).show();
            finish();
        }

        masterProp = new HashMap<>();
        pairedList = new ArrayList<>();
        deviceReadyList = new ArrayList<>();

        btButton = findViewById(R.id.bt_btn);

        deviceProps = new DeviceProps();
        myDeviceProp();

        // PACKAGE_NAME = getApplicationContext().getPackageName();

        // Example of a call to a native method
        /*TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());*/
        //readCPUinfo();
        findViewById(R.id.bt_btn).setOnClickListener(this);


        handleListViews();

        receiver = new BluetoothReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(receiver, filter);
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            btnFlag = 1;
            setButtonText(btnFlag);
        }


        manageHandler();

    }

    private void manageHandler() {

        Handler handler = new Handler(Looper.getMainLooper()){

            @Override
            public void handleMessage(@NonNull Message msg) {
                Log.i("asdf",msg.toString());
            }
        };


    }

    private void handleListViews() {


        ListView deviceReadyListView = findViewById(R.id.task_ready_lv);
        ListView pairedListView = findViewById(R.id.pair_lv);

        Set<String> devices = getPairedDevices().keySet();
        Map<String,String> pairedDevices = getPairedDevices();
        pairedList.addAll(devices);

        ArrayAdapter<String> pairedListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pairedList);
        pairedListView.setAdapter(pairedListAdapter);

        pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("asdf",pairedList.get(position));
                BluetoothDevice device = getPairedBtDevices(pairedList.get(position));
                Log.i("asdf","Pairing");
                if (device != null) {
                    BtConnectThread thread = new BtConnectThread(device);
                    thread.start();
                }
            }
        });


        ManageUUID manageUUID = new ManageUUID();
        for(int i=0;i<manageUUID.getDummyUuids().size();i++){
            BtAcceptThread thread = new BtAcceptThread(manageUUID.getDummyUuids().get(i).toString());
            thread.start();
        }
        /*ParcelUuid[] uuids = manageUUID.getUUIDs();
        for (ParcelUuid uuid : uuids) {
            Log.i("asdf", "uuid: " + uuid.toString());
            BtAcceptThread thread = new BtAcceptThread();
            thread.start();
        }*/

        /*final ArrayAdapter<String> deviceReadyAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,deviceReadyList);
        deviceReadyList.addAll(devices);*/


       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                deviceReadyListView.setAdapter(deviceReadyAdapter);
                Log.i("asdf","Getting device prop");

            }
        },3000);*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        },1000);

    }

    static void setButtonText(int btnFlag) {

        switch (btnFlag) {
            case 0:
                btButton.setText("Enable Bluetooth");
                btButton.setVisibility(View.VISIBLE);
                break;
            case 1:
                btButton.setText("Enable Discovery");
                btButton.setVisibility(View.VISIBLE);
                break;
            case 2:
                btButton.setVisibility(View.GONE);
                break;
        }
    }

    private void enableDiscovery() {

        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

    }

    private void myDeviceProp() {

        masterProp.put("VERSION.RELEASE", Build.VERSION.RELEASE);
        masterProp.put("VERSION.INCREMENTAL", Build.VERSION.INCREMENTAL);
        masterProp.put("VERSION.SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
        masterProp.put("BOARD", Build.BOARD);
        masterProp.put("DEVICE", Build.DEVICE);
        masterProp.put("CPU_MAX_FREQ", deviceProps.getMaxFreq());


        Log.i("asdf", "My device number of cores: " + String.valueOf(deviceProps.getNumberOfCores()));
        Log.i("asdf","My device max freq: " + deviceProps.getMaxFreq());


    }



    public native String stringFromJNI();



    public void enableBluetooth() {

        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            //enableDiscovery();
        } else {
            ManageUUID initUUIDs = new ManageUUID();
            ParcelUuid[] UUIDList;
            UUIDList = initUUIDs.getUUIDs();
            for (ParcelUuid uuids : UUIDList) {
                Log.i("asdf", uuids.getUuid().toString());
            }
            // enableDiscovery();
            //getPairedDevices();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            Toast.makeText(this, "bt enabled", Toast.LENGTH_SHORT).show();
            ManageUUID initUUIDs = new ManageUUID();
            ParcelUuid[] UUIDList;
            UUIDList = initUUIDs.getUUIDs();
            for (ParcelUuid uuids : UUIDList) {
                Log.i("asdf", uuids.getUuid().toString());
            }
            btnFlag = 1;
            setButtonText(btnFlag);

        } else {

            Toast.makeText(this, "please enable bt by granting permission", Toast.LENGTH_SHORT).show();
        }
    }

    private Map<String, String> getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        Map<String, String> btPairedList = new HashMap<>();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btPairedList.put(deviceName, deviceHardwareAddress);
                Log.i("asdf", "Device Name: " + deviceName + ", Device MAC Address: " + deviceHardwareAddress);
            }
        }
        return btPairedList;

    }

    private BluetoothDevice getPairedBtDevices(String deviceName){
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if(pairedDevices.size()>0){
            for(BluetoothDevice device : pairedDevices){
                if(device.getName().equals(deviceName))
                    return device;
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.bt_btn:
                if (btnFlag == 0)
                    enableBluetooth();
                else enableDiscovery();
                break;

        }
    }
}
