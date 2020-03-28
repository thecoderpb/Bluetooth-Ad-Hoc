package com.pratik.bluetoothadhoc.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = Devices.class, exportSchema = false, version = 1)
public abstract class DevicesDB extends RoomDatabase {


    public static final String DB_NAME = "devices_db";
    public static final Object LOCK = new Object();
    public static DevicesDB instance;


    public static synchronized DevicesDB getInstance(Context context){

        if(instance == null){
            synchronized (LOCK){
                instance = Room.databaseBuilder(context.getApplicationContext(),DevicesDB.class,DB_NAME)
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        return instance;
    }

    private static final Migration migration = new Migration(1,2){

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("ALTER TABLE DEVICES ADD COLUMN bogoMIPS LONG DEFAULT NULL");

        }
    };

    public abstract DevicesDAO devicesDAO();
}
