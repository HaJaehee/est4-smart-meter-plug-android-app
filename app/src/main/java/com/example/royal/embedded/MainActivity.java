package com.example.royal.embedded;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    //String SENDER_ID = "591144519813";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Demo";

    TextView mDisplay;
    global globalset = new global();

    //GoogleCloudMessaging gcm;
    //AtomicInteger msgId = new AtomicInteger();
    //String regid;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        globalset.setSENDER_ID("591144519813");
        context = getApplicationContext();

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            /*actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));*/
            ActionBar.Tab T =  actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this);
            /*if(i==0){
                T.setIcon(R.drawable.re);
            }else if(i==1){
                T.setIcon(R.drawable.re);
            }else{
                T.setIcon(R.drawable.re);
            }*/
            actionBar.addTab(T);
        }

//        mDisplay = (TextView)findViewById(R.id.text3);
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            globalset.setGcm(GoogleCloudMessaging.getInstance(this));
            globalset.setRegid(getRegistrationId(context));

          if (globalset.getRegid().isEmpty()) {
                registerInBackground();
            }
             /* else {
                mDisplay.setText(globalset.getRegid());
            }*/
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (globalset.getGcm() == null) {
                        globalset.setGcm(GoogleCloudMessaging.getInstance(context));
                    }
                    globalset.setRegid(globalset.getGcm().register(globalset.getSENDER_ID()));
                    //Device registered, registration ID=
                    msg = globalset.getRegid();

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, globalset.getRegid());
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
                AndroidDeviceRegister.asynchHttpRequest(msg);//send device registration id to jh-server.js and save that id in database
            }
        }.execute(null, null, null);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0 : return PlaceholderFragment1.newInstance(position + 1);
                case 1 : return PlaceholderFragment2.newInstance(position + 1);
                case 2 : return PlaceholderFragment3.newInstance(position + 1);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            String a = new String();
            int f = 0;
            switch (position) {
                case 0:
                    a = "전력량 조회";
                    f = 1;
                    break;
                case 1:
                    a = "스위치 켜기/끄기";
                    f = 1;
                    break;
                case 2:
                    a = "예상 전력량 설정";
                    f = 1;
                    break;

            }
            if (f == 1) {
                return a.toUpperCase(l);
            } else {
                return null;
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment1 extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment1 newInstance(int sectionNumber) {
            PlaceholderFragment1 fragment = new PlaceholderFragment1();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment1() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment1, container, false);
            TextView text = (TextView)inflater.inflate(R.layout.text1, null, false);
            Button but = (Button)inflater.inflate(R.layout.but1, null, false);

            rootView.addView(text);
            rootView.addView(but);

            myListener m = new myListener(text);

            but.setOnClickListener(m);
            return rootView;
        }
    }


    public static class PlaceholderFragment2 extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment2 newInstance(int sectionNumber) {
            PlaceholderFragment2 fragment = new PlaceholderFragment2();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment2() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment2, container, false);
            LinearLayout childView = (LinearLayout)inflater.inflate(R.layout.llayout, null, false);
            TextView text = (TextView)inflater.inflate(R.layout.text2, null, false);
            Button but1 = (Button)inflater.inflate(R.layout.but2, null, false);
            Button but2 = (Button)inflater.inflate(R.layout.but3, null, false);

            rootView.addView(text);
            childView.addView(but1);
            childView.addView(but2);
            rootView.addView(childView);

            myListener m = new myListener(text);

            but1.setOnClickListener(m);
            but2.setOnClickListener(m);

            return rootView;
        }
    }

    public static class PlaceholderFragment3 extends Fragment {

        private EditText editTextWh;
        private EditText editTextW;

        private Button buttonSet;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        global globalset;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment3 newInstance(int sectionNumber) {
            PlaceholderFragment3 fragment = new PlaceholderFragment3();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment3() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.fragment3, container, false);

            editTextWh = (EditText) rootView.findViewById(R.id.editTextWh);
            editTextW = (EditText) rootView.findViewById(R.id.editTextW);
            WattLimitGetter.asynchHttpRequest("10001", editTextW,editTextWh);

            buttonSet = (Button) rootView.findViewById(R.id.buttonSet);
            buttonSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(editTextWh.getText().length() != 0 && editTextW.getText().length() != 0) {
                        String currWatt = editTextW.getText().toString();
                        String wattHour = editTextWh.getText().toString();
                        WattLimitRegister.asynchHttpRequest(currWatt,wattHour);
                    }

                }
            });
            return rootView;
        }
    }

    /*public static class PlaceholderFragment3 extends Fragment {
        *//**
         * The fragment argument representing the section number for this
         * fragment.
         *//*
        private static final String ARG_SECTION_NUMBER = "section_number";
        global globalset;
        *//**
         * Returns a new instance of this fragment for the given section
         * number.
         *//*
        public static PlaceholderFragment3 newInstance(int sectionNumber) {
            PlaceholderFragment3 fragment = new PlaceholderFragment3();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment3() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            LinearLayout rootView = (LinearLayout)inflater.inflate(R.layout.fragment3, container, false);
            Button but1 = (Button)rootView.findViewById(R.id.but4);
            Button but2 = (Button)rootView.findViewById(R.id.but5);
            TextView text = (TextView)rootView.findViewById(R.id.text3);

            text.setText(globalset.getRegid());



            myListener m = new myListener(text);
            but1.setOnClickListener(m);
            but2.setOnClickListener(m);
            return rootView;
        }
    }*/
}
class myListener implements View.OnClickListener {
    //리스너 클래스를 따로 정의하여 공통된 리스너를 쓸 수 있도록한다.
    myListener(TextView T) {
        TV = T;
    }
    AsyncHttpClient client = new AsyncHttpClient();
    global globalset;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but1:
                //client.get(url, AsyncHttpResponseHandler);
                client.get("http://202.30.29.239:80/check.php", new AsyncHttpResponseHandler() {

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
                            double watt = 0;
                            double watthour = 0;
                            int power = 0;
                            String strRes = new String(response,"UTF-8");
                            Log.d("response", strRes);
                            try {
                                JSONObject  jObject = new JSONObject(strRes);
                                 watt = Double.parseDouble(jObject.get("watt").toString());
                                 watthour = Double.parseDouble(jObject.get("watthour").toString());
                                 watt += watthour;
                                 power = Integer.parseInt(jObject.get("power").toString());
                            }catch (JSONException e){
                                Log.d("state", "Error parsing data");
                            }
                            if(power == 1) {
                                TV.setText("Wh : " +watt + "\n전원 : 켜짐");
                            }else{
                                TV.setText("Wh : " +watt + "\n전원 : 꺼짐");
                            }
                        }
                        catch (UnsupportedEncodingException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.d("state", "onFailure");
                        TV.setText(statusCode+"");
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                        Log.d("state", "onRetry");
                    }
                });
                break;
            case R.id.but2:
                TV.setText("켜짐!");
                DevicePowerRegister.asynchHttpRequest("1");
                break;
            case R.id.but3:
                TV.setText("꺼짐!");
                DevicePowerRegister.asynchHttpRequest("0");
                break;
            /*case R.id.but4: new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg = "";
                    try {
                        Bundle data = new Bundle();
                        data.putString("my_message", "Hello World");
                        data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
                        String id = Integer.toString(globalset.getMsgId().incrementAndGet());
                        globalset.getGcm().send(globalset.getSENDER_ID() + "@gcm.googleapis.com", id, data);
                        msg = "Sent message";
                    } catch (IOException ex) {
                        msg = "Error :" + ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    TV.append(msg + "\n");
                }
            }.execute(null, null, null);
                break;
            case R.id.but5:TV.setText("");
                break;*/
        }
    }
    private TextView TV;
};