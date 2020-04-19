package com.pratik.bluetoothadhoc;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.pratik.bluetoothadhoc.database.Devices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.pratik.bluetoothadhoc.BluetoothMessageService.remoteDeviceAddress;
import static com.pratik.bluetoothadhoc.BluetoothMessageService.remoteDeviceName;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_ENABLE_BT = 1;

    static int ConnectedDeviceCount = 0;
    public static Handler handler;
    public static boolean isShowAlert = true;

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

    static Map<String, BluetoothSocket> remoteDeviceIdList = new HashMap<>();
    static List<BluetoothDevice> remoteBtDeviceList = new ArrayList<>();
    static List<String> faultTolerantAddresss;
    static int realRank;

    private BluetoothAdapter btAdapter;
    private BroadcastReceiver receiver;
    private static Button btButton;
    ArrayAdapter<String> deviceReadyListAdapter;
    static int btnFlag = 0;
    private DeviceProps deviceProps;
    private DevicesViewModel viewModel;
    private TextView rankText;
    ArrayList<String> pairedList, deviceReadyList;
    ListView deviceReadyListView;
    PrefManager prefs;


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
        viewModel.nukeTable();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(this, "Bluetooth does not exits. App cannot run", Toast.LENGTH_LONG).show();
            finish();
        }




        masterProp = new HashMap<>();
        pairedList = new ArrayList<>();
        deviceReadyList = new ArrayList<>();
        prefs = new PrefManager(this);

        deviceReadyListView = findViewById(R.id.task_ready_lv);

        btButton = findViewById(R.id.bt_btn);

        deviceProps = new DeviceProps();
        myDeviceProp();
        String mGlInfo = gatherGlInfo();


        // PACKAGE_NAME = getApplicationContext().getPackageName();

        // Example of a call to a native method

        //readCPUinfo();
        viewModel = ViewModelProviders.of(MainActivity.this).get(DevicesViewModel.class);
        viewModel.nukeTable();
        findViewById(R.id.bt_btn).setOnClickListener(this);
        deviceReadyListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceReadyList);


        handleListViews();

        receiver = new BluetoothReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            btnFlag = 1;
            setButtonText(btnFlag);
        }


        try {
            manageHandler();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        rankText = findViewById(R.id.task_ready_tv);

    }

    private String gatherGlInfo() {

        return deviceProps.getGPUinfo();

    }

    private void manageHandler() throws IOException, ClassNotFoundException {

        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(@NonNull Message msg) {


                if (msg.what == BluetoothMessageService.MessageConstants.MESSAGE_READ) {

                    Object obj = msg.obj;
                    byte[] message = (byte[]) obj;
                    String strs = new String(message);

                    //Log.i("asdf",str);

                    final String[] strings = new String[4];
                    String[] mainMsg = strs.split("\0"); //entire msg
                    Log.i("asdf", "Entire message received from device -->" + mainMsg[0]);
                    //TODO: Fix the substring issue
                    if (mainMsg[0].startsWith("\r")) {
                        String rank = mainMsg[0].substring(6);
                        displayRank(rank);

                    } else if (mainMsg[0].startsWith("\f")) {
                        strings[0] = mainMsg[0].substring(1, 8); // freq
                        strings[1] = mainMsg[0].substring(9, 10); // core
                        strings[2] = mainMsg[0].substring(11); //gpu and device name
                        if (strings[2].startsWith("Adreno", 12)) {
                            strings[3] = strings[2].substring(11, 21);
                            strings[2] = strings[2].substring(21);

                            Log.i("asdf", "device freq:" + strings[0]);
                            Log.i("asdf", "device max cores:" + strings[1]);
                            Log.i("asdf", "device name:" + strings[2]);

                        }

                        try {
                            addDeviceToDB(Long.parseLong(strings[0]), Integer.parseInt(strings[1]), strings[2]);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    rankDevice(strings[0], strings[1], strings[3], strings[2]);
                                }
                            }, 500);

                        } catch (Exception e) {
                            e.printStackTrace();

                        }


                    } else if (mainMsg[0].startsWith("\t")) {

                        Log.i("asdf", mainMsg[0]);
                        String[] strss = mainMsg[0].split("\t");
                        faultTolerantAddresss = new ArrayList<>(Arrays.asList(strss));
                        for (String addr : strss) {
                            if (!addr.equals("")) {
                                faultTolerantAddresss.add(addr);
                                Log.i("asdf","fta : "+addr);
                            }
                        }

                        if (!prefs.getMasterMacAddress().equals(""))
                            faultTolerantAddresss.add(prefs.getMasterMacAddress());
                        Log.i("asdff", faultTolerantAddresss.toString());


                    }


                } else if (msg.what == BluetoothMessageService.MessageConstants.MESSAGE_WRITE) {
                    if (isShowAlert)
                        displayAlert();
                }

                deviceReadyView();

            }

        };


    }




    @SuppressLint("SetTextI18n")
    private void displayRank(String rank) {


        realRank = Integer.parseInt(rank) + 1;
        Log.i("asdf", "device rank is " + realRank);
        Toast.makeText(this, "Device Rank is " + realRank, Toast.LENGTH_SHORT).show();
        rankText.setText("This device is now slave to " + remoteDeviceName + " | Rank is " + realRank);


    }

    private void addDeviceToDB(final long cpuFreq, final int cpuCores, final String deviceName) {


        Devices addDevice = new Devices(deviceName, cpuFreq, cpuCores);
        //if (!viewModel.isDeviceInDB(deviceName))
        viewModel.insert(addDevice);


    }

    //Slave Node
    private void displayAlert() {

        String str = "Slave (this device " + Build.MODEL + ") connected to master " + remoteDeviceName;

        prefs.setMasterMacAddress(remoteDeviceAddress);
        new AlertDialog.Builder(this)
                .setTitle("Connected to master")
                .setMessage(str)
                .setPositiveButton("OK", null)
                .show();

        deviceReadyListView.setVisibility(View.INVISIBLE);


    }

    //Master Node
    private void rankDevice(String cpuFreq, String cpuCore, String gpu, final String deviceName) {

        String text = "Device Name : " + deviceName + "\nDevice CPU Freq : " + cpuFreq + " Hz\nDevice max cores:" + cpuCore;
        new AlertDialog.Builder(this)
                .setTitle("Device Props obtained")
                .setMessage(text)
                .setPositiveButton("Ok", null)
                .show();

        viewModel.getRankedDevices().observe(this, new Observer<List<String>>() {
            @SuppressLint("HardwareIds")
            @Override
            public void onChanged(final List<String> deviceName) {
                //send updated ranks to devices
                Log.i("asdf", "sending updated ranks to device(s)");
                if (remoteDeviceIdList.size() != 0) {
                    for (int i = 0; i < deviceName.size(); i++) {
                        if (remoteDeviceIdList.containsKey(deviceName.get(i))) {
                            BluetoothSocket socket = remoteDeviceIdList.get(deviceName.get(i));
                            assert socket != null;
                            if (socket.isConnected()) {
                                BluetoothMessageService service = new BluetoothMessageService();
                                service.connectService(socket);
                                service.sendRanking(i);

                                Log.i("asdf", "Sending fault tolerance data");
                                //Send Fault tolerance data to next potential master
                                if (i == 0) {

                                    Log.i("asdf", "Sending fault tolerance data");
                                    BluetoothMessageService service2 = new BluetoothMessageService();
                                    service2.connectService(socket);

                                    List<BluetoothSocket> list = new ArrayList<>(remoteDeviceIdList.values());
                                    List<String> addr = new ArrayList<>();
                                    for (BluetoothSocket soc : list) {
                                        if (!socket.getRemoteDevice().getAddress().equals(soc.getRemoteDevice().getAddress()))
                                            addr.add(soc.getRemoteDevice().getAddress());
                                    }
                                    service2.sendData(addr);

                                }


                            }
                        }
                    }
                }
            }
        });

    }

    private void handleListViews() {


        ListView pairedListView = findViewById(R.id.pair_lv);

        Set<String> devices = getPairedDevices().keySet();
        Map<String, String> pairedDevices = getPairedDevices();
        pairedList.addAll(devices);

        ArrayAdapter<String> pairedListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pairedList);
        pairedListView.setAdapter(pairedListAdapter);

        pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.i("asdf", pairedList.get(position));
                BluetoothDevice device = getPairedBtDevices(pairedList.get(position));
                Log.i("asdf", "Pairing");
                if (device != null && BluetoothAdapter.getDefaultAdapter().getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    Toast.makeText(MainActivity.this, "Connecting", Toast.LENGTH_SHORT).show();
                    BtConnectThread thread = new BtConnectThread(device);
                    thread.start();
                }
            }
        });

        if (BluetoothAdapter.getDefaultAdapter().getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
            ManageUUID manageUUID = new ManageUUID();
            for (int i = 0; i < manageUUID.getDummyUuids().size(); i++) {
                BtAcceptThread thread = new BtAcceptThread(manageUUID.getDummyUuids().get(i).toString());
                thread.start();
            }
        }

    }

    public void deviceReadyView() {

        deviceReadyListView.setVisibility(View.VISIBLE);
        deviceReadyList.clear();
        deviceReadyList.addAll(remoteDeviceIdList.keySet());
        deviceReadyListView.setAdapter(deviceReadyListAdapter);

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


        Log.i("asdf", "My device number of cores: " + deviceProps.getNumberOfCores());
        Log.i("asdf", "My device max freq: " + deviceProps.getMaxFreq());
        Log.i("asdf", "My device gpu: " + deviceProps.getGPUinfo());
        Log.i("asdf", "My device name: " + Build.MODEL);


    }


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

    private BluetoothDevice getPairedBtDevices(String deviceName) {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(deviceName))
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (remoteDeviceIdList.size() == 0) {

            Toast.makeText(this, "No devices ready to execute task", Toast.LENGTH_SHORT).show();

        } else
            switch (item.getItemId()) {
                case R.id.menu_task1:
                    break;
                case R.id.menu_task2:
                    break;
                case R.id.menu_task3:
                    break;
            }

        return super.onOptionsItemSelected(item);
    }


    public native void taskJNI(Map remoteDeviceId);

}
