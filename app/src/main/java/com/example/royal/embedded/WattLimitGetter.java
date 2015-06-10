package com.example.royal.embedded;

/**
 * WattLimitGetter.java
 *
 * Created by HJH on 2015-06-08 20:00
 */

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class WattLimitGetter {

    public static void asynchHttpRequest (String a_strDeviceId, final EditText a_editTextW, final EditText a_editTextWh) {

        AsyncHttpClient client = new AsyncHttpClient();

        if (a_strDeviceId!=null) {
            RequestParams req = new RequestParams();
            req.add("device_id", a_strDeviceId);


            //client.get(url, AsyncHttpResponseHandler);
            client.post("http://202.30.29.239:9988", req, new AsyncHttpResponseHandler() {

                TextView editTextWh = a_editTextWh;
                TextView editTextW = a_editTextW;
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

                        try {
                            JSONObject obj = new JSONObject(strRes);
                            editTextW.setText(obj.get("currentwatt").toString());
                            editTextWh.setText(obj.get("watthour").toString());
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
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
