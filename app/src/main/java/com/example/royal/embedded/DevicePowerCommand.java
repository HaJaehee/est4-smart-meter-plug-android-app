package com.example.royal.embedded;

/**
 * DevicePowerCommnand.java
 *
 * Created by HJH on 2015-06-08 20:00
 */
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;


public class DevicePowerCommand {

    public static void asynchHttpRequest (String a_strPower, String a_strDeviceIp) {

        AsyncHttpClient client = new AsyncHttpClient();

        if (a_strPower!=null) {
            RequestParams req = new RequestParams();
            req.add("power", a_strPower);

            //client.get(url, AsyncHttpResponseHandler);
            client.post("http://"+a_strDeviceIp+":5000", req, new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    // called before request is started
                    Log.d("state", "onStart");
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    // called when response HTTP status is "200 OK"
                    Log.d("state", "onSuccess");
                    try {
                        String strRes = new String(response, "UTF-8");
                        Log.d("response", strRes);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    Log.d("state", "onFailure");
                }

                @Override
                public void onRetry(int retryNo) {
                    // called when request is retried
                    Log.d("state", "onRetry");
                }
            });
        }
    }
}
