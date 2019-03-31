package com.ufistudio;

import com.ufistudio.hotelmediabox.repository.data.ConnectDetail;

import java.util.ArrayList;

public class jnitest5 {

    static {
        System.loadLibrary("Ian");
    }

    public native int init(int width,int height,int x,int y);

    public native int setchannel(String c);

    public native int setchannelWithNoSearch(String c);

    public native void play(String c);

    public native void deinit();

    public native String getchannels(String c);

    public native int initDev();

    public synchronized int initDevice(){
        synchronized (this){
            return initDev();
        }
    }

    public synchronized int initPlayer(int width,int height,int x,int y){
        synchronized (this){
            return init(width, height, x, y);
        }
    }

    public synchronized int scanChannel(String freq){
        synchronized (this){
            return setchannelWithNoSearch(freq);
        }
    }

    public synchronized void playChannel(String channel){
        synchronized (this){
            play(channel);
        }
    }

    public synchronized void closePlayer(){
        synchronized (this){
            deinit();
        }
    }

    public synchronized String getChannelList(String c){
        synchronized (this){
            return getchannels(c);
        }
    }
}
