package com.pratik.bluetoothadhoc;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.pratik.bluetoothadhoc.database.Devices;
import com.pratik.bluetoothadhoc.database.DevicesRepository;

import java.util.List;

public class DevicesViewModel extends AndroidViewModel {

    private DevicesRepository repository;
    private LiveData<List<Devices>> devices,devicesWithNCond,devicesWithPCond;
    private boolean isDeviceInDB;

    public DevicesViewModel(@NonNull Application application) {
        super(application);

        repository = new DevicesRepository(application);
        devices = repository.getAllDevices();
    }

    public LiveData<List<Devices>> getAllDevices(){
        return devices;
    }

    public LiveData<List<Devices>> getDevicesWithNCond(int core,long cpuFreq) {
        devicesWithNCond = repository.getAllDevWithNCond(core,cpuFreq);
        return devicesWithNCond;
    }

    public LiveData<List<Devices>> getDevicesWithPCond(int core,long cpuFreq) {
        devicesWithPCond = repository.getAllDevWithPCond(core,cpuFreq);
        return devicesWithPCond;
    }
    public boolean isDeviceInDB(String deviceName){
        isDeviceInDB = repository.isDeviceInDB(deviceName);
        return isDeviceInDB;
    }
    public void insert(Devices devices){
        repository.insert(devices);
    }

    public void update(Devices devices){
        repository.update(devices);
    }

    public void delete(Devices devices){
        repository.delete(devices);
    }
}
