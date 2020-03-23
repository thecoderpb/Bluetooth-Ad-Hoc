package com.pratik.bluetoothadhoc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

class PrefManager {

    private static final String PREF_NAME = "com.pratik.bluetoothadhoc";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

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

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }
}
