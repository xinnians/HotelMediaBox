package com.ufistudio;

import android.util.Log;

public class jnitest5 {

    private static final String TAG = jnitest5.class.getSimpleName();
    private static final boolean TAG_INIT_JNI = true;

    static {
        if (TAG_INIT_JNI)
            System.loadLibrary("Ian");
    }

    private native int init(int width, int height, int x, int y);

    private native int setchannel(String c);

    private native int setchannelWithNoSearch(String c);

    private native int play(String c);

    private native void deinit();

    private native String getchannels(String c);

    private native int initDev();

    //------------------------------------------------------------------

    private native int initVO(int width, int height, int x, int y);

    private native int initAVPlayer();

    private native int deInitAVPlayer();

    private native int deInitDev();

    public synchronized int setVO(int width, int height, int x, int y) {
        Log.d(TAG, "[setVO] call");
        if (!TAG_INIT_JNI)
            return 0;
        synchronized (this) {
            return initVO(width, height, x, y);
        }
    }

    public synchronized int setAVPlayer() {
        Log.d(TAG, "[setAVPlayer] call");
        if (!TAG_INIT_JNI)
            return 0;
        synchronized (this) {
            return initAVPlayer();
        }
    }

    public synchronized int releaseAVPlayer() {
        Log.d(TAG, "[releaseAVPlayer] call");
        if (!TAG_INIT_JNI)
            return 0;
        synchronized (this) {
            return deInitAVPlayer();
        }
    }

    public synchronized int releaseDev() {
        Log.d(TAG, "[releaseDev] call");
        if (!TAG_INIT_JNI)
            return 0;
        synchronized (this) {
            return deInitDev();
        }
    }

    public synchronized int initDevice() {
        Log.d(TAG, "[initDevice] call");
        if (!TAG_INIT_JNI)
            return 0;
        synchronized (this) {
            return initDev();
        }
    }

    public synchronized int initPlayer(int width, int height, int x, int y) {
        Log.d(TAG, "[initPlayer] call");
        if (!TAG_INIT_JNI)
            return 0;
        synchronized (this) {
            return init(width, height, x, y);
        }
    }

    public synchronized int scanChannel(String freq) {
        Log.d(TAG, "[scanChannel] call");
        if (!TAG_INIT_JNI)
            return 0;
        synchronized (this) {
            return setchannelWithNoSearch(freq);
        }
    }

    public synchronized int playChannel(String channel) {
        Log.d(TAG, "[playChannel] call");
        if (!TAG_INIT_JNI)
            return 0;
        synchronized (this) {
            return play(channel);
        }
    }

    public synchronized void closePlayer() {
        Log.d(TAG, "[closePlayer] call");
        if (!TAG_INIT_JNI)
            return;
        synchronized (this) {
            deinit();
        }
    }

    public synchronized String getChannelList(String c) {
        Log.d(TAG, "[getChannelList] call");
        if (!TAG_INIT_JNI)
            return "";
        synchronized (this) {
            return getchannels(c);
        }
    }
}
