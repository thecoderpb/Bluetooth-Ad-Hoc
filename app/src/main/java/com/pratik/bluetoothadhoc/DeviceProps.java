package com.pratik.bluetoothadhoc;

import android.annotation.SuppressLint;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;

import com.pratik.bluetoothadhoc.gles.EglCore;
import com.pratik.bluetoothadhoc.gles.OffscreenSurface;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;


public class DeviceProps {

    private GLSurfaceView mGlSurfaceView;

    @SuppressLint("ObsoleteSdkInt")
    public int getNumberOfCores() {
        if (Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors();
        } else {
            return getNumCoresOldPhones();
        }
    }


    private int getNumCoresOldPhones() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    public String getMaxFreq() {

        String cpuMaxFreq = "0";
        RandomAccessFile reader;
        try {
            reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
            cpuMaxFreq = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuMaxFreq;
    }

    private void readCPUinfo() {
        ProcessBuilder cmd;
        String result = "";

        try {
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[1024];
            while (in.read(re) != -1) {
                System.out.println(new String(re));
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Log.i("asdf", result);
        //return result;
    }

    public String getGPUinfo() {
        // We need a GL context to examine, which means we need an EGL surface.  Create a 1x1
        // pbuffer.
        EglCore eglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);
        OffscreenSurface surface = new OffscreenSurface(eglCore, 1, 1);
        surface.makeCurrent();


        return GLES20.glGetString(GLES20.GL_RENDERER);
    }


}
