package com.ufistudio.hotelmediabox;

import com.ufistudio.jnitest5;

public class DVBHelper {

    private static jnitest5 mDVBPlayer;

    public static void initialize(){
        if(mDVBPlayer == null){
            mDVBPlayer = new jnitest5();
        }
    }

    public static jnitest5 getDVBPlayer(){
        if(mDVBPlayer == null){
            initialize();
        }
        return mDVBPlayer;
    }
}
