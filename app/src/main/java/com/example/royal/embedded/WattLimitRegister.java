package com.example.royal.embedded;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;


public class WattLimitRegister {

    public static void asynchHttpRequest (String a_strCurrWatt, String a_strWattHour) {

        AsyncHttpClient client = new AsyncHttpClient();

        if (a_strCurrWatt!=null&& a_strCurrWatt!=null) {
            RequestParams req = new RequestParams();
            req.add("set_curr_watt", a_strCurrWatt);
            req.add("set_watt_hour", a_strWattHour);

            //client.get(url, AsyncHttpResponseHandler);
            client.post("http://202.30.29.239:9999", req, new AsyncHttpResponseHandler() {

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
