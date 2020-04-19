package com.pratik.bluetoothadhoc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

class PrefManager {

    private static final String PREF_NAME = "com.pratik.bluetoothadhoc";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String IS_MY_DEVICE_MASTER = "IsMyDeviceMaster";
    private static final String MASTER_MAC_ADDRESS = "MasterMacAddress";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    PrefManager(Context context) {

        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();

    }

    public void setFirstTimeLaunch(boolean isFirstTimeLaunch) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTimeLaunch);
        editor.commit();
    }

    public void setMyDeviceMaster(boolean isMyDeviceMaster){
        editor.putBoolean(IS_MY_DEVICE_MASTER,isMyDeviceMaster);
        editor.commit();
    }

    public void setMasterMacAddress(String masterMacAddress){
        editor.putString(MASTER_MAC_ADDRESS,masterMacAddress);
        editor.commit();
    }

    public  String getMasterMacAddress() { return pref.getString(MASTER_MAC_ADDRESS,""); }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public boolean isMyDeviceMaster(){return pref.getBoolean(IS_MY_DEVICE_MASTER,false); }
}
