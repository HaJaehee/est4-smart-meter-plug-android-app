package com.example.royal.embedded;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Royal on 2015-06-05.
 */
public class global {

    private static  GoogleCloudMessaging gcm;
    private static AtomicInteger msgId = new AtomicInteger();
    private static String regid = new String();
    private static String SENDER_ID = new String();
    public static void setSENDER_ID(String s){
       SENDER_ID= s;
    }
    public static void setGcm(GoogleCloudMessaging g){
            gcm = g;
    }
    public static void setMsgId(AtomicInteger m){
            msgId = m;
    }
    public static void setRegid(String r){
            regid = r;
    }
    public static String getSENDER_ID(){
        return SENDER_ID;
    }
    public static GoogleCloudMessaging getGcm(){
        return gcm;
    }
    public static AtomicInteger getMsgId(){
        return msgId;
    }
    public static String getRegid(){
        return regid;
    }
}
