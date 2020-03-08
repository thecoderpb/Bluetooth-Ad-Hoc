package com.pratik.bluetoothadhoc;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static String PACKAGE_NAME = "com.bluetoothadhoc";
    Map<String, String> masterProp;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        try {
            System.load("/system/vendor/lib/libOpenCL.so");
            Log.i(PACKAGE_NAME, "Library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            Log.i(PACKAGE_NAME, "failed to find lib");
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDeviceProp();

        // PACKAGE_NAME = getApplicationContext().getPackageName();

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    private void myDeviceProp() {

        masterProp.put("VERSION.RELEASE", Build.VERSION.RELEASE);
        masterProp.put("VERSION.INCREMENTAL", Build.VERSION.INCREMENTAL);
        masterProp.put("VERSION.SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
        masterProp.put("BOARD", Build.BOARD);
        masterProp.put("DEVICE", Build.DEVICE);


    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
