package com.pratik.bluetoothadhoc.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devices")
public class Devices {


    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo
    private String deviceName;
    private long cpuFreq;
    private int cpuCore;


    public Devices(String deviceName, long cpuFreq, int cpuCore) {
        this.deviceName = deviceName;
        this.cpuFreq = cpuFreq;
        this.cpuCore = cpuCore;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public long getCpuFreq() {
        return cpuFreq;
    }

    public void setCpuFreq(long cpuFreq) {
        this.cpuFreq = cpuFreq;
    }

    public int getCpuCore() {
        return cpuCore;
    }

    public void setCpuCore(int cpuCore) {
        this.cpuCore = cpuCore;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
