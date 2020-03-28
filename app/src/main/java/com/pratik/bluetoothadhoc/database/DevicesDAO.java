package com.pratik.bluetoothadhoc.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DevicesDAO {

    @Query("SELECT * FROM DEVICES")
    LiveData<List<Devices>> getAllDeviceProps();

    @Insert
    void insertDevice(Devices devices);
    @Update
    void updateDevice(Devices devices);
    @Delete
    void deleteDevice(Devices devices);

    @Query("SELECT * FROM DEVICES WHERE id=:id")
    LiveData<List<Devices>> getDeviceWithId(int id);

    @Query("SELECT * FROM DEVICES WHERE cpuCore >=:cpuCore AND cpuFreq >=:cpuFreq")
    LiveData<List<Devices>> getDeviceWithPCond(int cpuCore,long cpuFreq);

    @Query("SELECT * FROM DEVICES WHERE cpuCore <=:cpuCore AND cpuFreq <=:cpuFreq")
    LiveData<List<Devices>> getDevicesWithNCond(int cpuCore,long cpuFreq);

    @Query("SELECT deviceName FROM DEVICES WHERE deviceName =:deviceName")
    String getDeviceIfExist(String deviceName);

    @Query("DELETE FROM DEVICES")
    void nukeTable();


}
