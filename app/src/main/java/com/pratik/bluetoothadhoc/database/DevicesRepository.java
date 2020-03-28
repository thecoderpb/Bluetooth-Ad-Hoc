package com.pratik.bluetoothadhoc.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class DevicesRepository {

    private DevicesDAO devicesDAO;
    private LiveData<List<Devices>> allDevices, allDevWithNCond, allDevWithPCond;
    private Application application;

    public DevicesRepository(Application application) {

        this.application = application;
        DevicesDB db = DevicesDB.getInstance(application);

        devicesDAO = db.devicesDAO();

        allDevices = devicesDAO.getAllDeviceProps();

    }

    public void insert(final Devices devices) {

        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                devicesDAO.insertDevice(devices);
            }
        });

        Log.i("asdf", "device inserted into db");
    }

    public void update(final Devices devices) {

        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                devicesDAO.updateDevice(devices);

                Log.i("adf", "task updated");
            }
        });
    }

    public void delete(final Devices devices) {

        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                devicesDAO.deleteDevice(devices);
            }
        });
    }

    public void deleteTable() {
        AppExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                devicesDAO.nukeTable();
            }
        });

    }

    public LiveData<List<Devices>> getAllDevices() {
        return allDevices;
    }

    public LiveData<List<Devices>> getAllDevWithNCond(final int core, final long cpuFreq) {
        DevicesDB db = DevicesDB.getInstance(application);

        DevicesDAO devicesDAO = db.devicesDAO();
        return devicesDAO.getDevicesWithNCond(core, cpuFreq);
    }

    public LiveData<List<Devices>> getAllDevWithPCond(final int core, final long cpuFreq) {
        DevicesDB db = DevicesDB.getInstance(application);

        DevicesDAO devicesDAO = db.devicesDAO();
        return devicesDAO.getDeviceWithPCond(core, cpuFreq);
    }

    public boolean isDeviceInDB(String deviceName){
        DevicesDB db = DevicesDB.getInstance(application);

        DevicesDAO devicesDAO = db.devicesDAO();
        return devicesDAO.getDeviceIfExist(deviceName)!=null;
    }




}
