package com.release.appiumcommgetter;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

import static com.release.appiumcommgetter.Global.mFailCount;
import static com.release.appiumcommgetter.Global.mNeedToRestart;
import static com.release.appiumcommgetter.Global.mSending;
import static com.release.appiumcommgetter.Global.mToken;
import static com.release.appiumcommgetter.Global.m_EmulatorPhoneNumber;
import static com.release.appiumcommgetter.Global.m_MainAct;
import static com.release.appiumcommgetter.Global.m_PhoneNumber;
import static com.release.appiumcommgetter.Global.m_PhoneNumber2;
import static com.release.appiumcommgetter.Global.m_bFirstTime;
import static com.release.appiumcommgetter.Global.m_dualPhoneNumber;
import static com.release.appiumcommgetter.Global.m_iDualSimSender;
import static com.release.appiumcommgetter.Global.m_iSubId;
import static com.release.appiumcommgetter.Global.m_queryInboxSimId;
import static com.release.appiumcommgetter.Global.m_tempPhone1;
import static com.release.appiumcommgetter.Global.m_tempPhone2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private boolean bAlreadyInit = false;
    Spinner spRegion;
    Button btnSaveMaxSMS;
    EditText etMaxSMS;
    Button btnSavePhone;
    Button btnSavePhone2;
    Button btnGetLine1;
    Button btnGetLine2;
    EditText etPhone;
    EditText etPhone2;
    Button btnResend;
    DatabaseSMS dbSMS;
    TextView tvSMSCount;
    TextView tvSMSData;
    TextView tvLastChecked;
    TextView tvLastSend;
    Button btnCheckInternet;
    Button btnRestart;
    Intent _intent;
    String _szLastSend = "";
    Button btnSMSId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Global.m_MainAct = this;

        dbSMS = new DatabaseSMS(this);

        Log.i(Global.TAG, "Activity Started !");

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);

        etMaxSMS = (EditText) findViewById(R.id.etMaxSMS);
        if (!sharedPref.contains("MAX_SMS")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("MAX_SMS", 100);
            editor.commit();

            etMaxSMS.setText(String.valueOf(100));
            Global.m_MaxSMS = 100;
        } else {
            Global.m_MaxSMS = sharedPref.getInt("MAX_SMS", 100);
            etMaxSMS.setText(String.valueOf(sharedPref.getInt("MAX_SMS", 100)));
        }

        etPhone = (EditText) findViewById(R.id.etPhoneNumber);
        /*if (!sharedPref.contains("PHONE_NUMBER")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("PHONE_NUMBER", m_PhoneNumber);
            editor.commit();
            etPhone.setText(m_PhoneNumber);
        } else {
            Global.m_PhoneNumber = sharedPref.getString("PHONE_NUMBER", m_PhoneNumber);
            etPhone.setText(sharedPref.getString("PHONE_NUMBER", m_PhoneNumber));
        }*/
        m_PhoneNumber = m_tempPhone1;
        etPhone.setText(m_PhoneNumber);

        etPhone2 = (EditText) findViewById(R.id.etPhoneNumber2);
        if (!sharedPref.contains("PHONE_NUMBER2")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("PHONE_NUMBER2", m_PhoneNumber2);
            editor.commit();
            etPhone2.setText(m_PhoneNumber2);
        } else {
            m_EmulatorPhoneNumber = sharedPref.getString("PHONE_NUMBER2", m_PhoneNumber2);
            etPhone2.setText(sharedPref.getString("PHONE_NUMBER2", m_EmulatorPhoneNumber));
        }

        if (!sharedPref.contains("SUBSCRIPTION_ID")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("SUBSCRIPTION_ID", m_iSubId);
            editor.commit();
        } else {
            m_iSubId = sharedPref.getInt("SUBSCRIPTION_ID", m_iSubId);
        }

        /*etPhoneId = (EditText) findViewById(R.id.etPhoneId);
        if (!sharedPref.contains("PHONE_ID")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            //TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            editor.putString("PHONE_ID", Build.SERIAL.toUpperCase() + "-" + ((Long) System.currentTimeMillis()).toString());
            //editor.putString("PHONE_ID", tm.getDeviceId());
            editor.commit();

            etPhoneId.setText(Build.SERIAL.toUpperCase() + "-" + ((Long) System.currentTimeMillis()).toString());
            Global.m_PhoneId = Build.SERIAL.toUpperCase() + "-" + ((Long) System.currentTimeMillis()).toString();
            //etPhoneId.setText(tm.getDeviceId());
            //Global.m_PhoneId = tm.getDeviceId();
        } else {
            //TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            etPhoneId.setText(sharedPref.getString("PHONE_ID", Build.SERIAL.toUpperCase() + "-" + ((Long) System.currentTimeMillis()).toString()));
            Global.m_PhoneId = sharedPref.getString("PHONE_ID", Build.SERIAL.toUpperCase() + "-" + ((Long) System.currentTimeMillis()).toString());
            //Global.m_PhoneId = sharedPref.getString("PHONE_ID", tm.getDeviceId());
            //etPhoneId.setText(sharedPref.getString("PHONE_ID", tm.getDeviceId()));
        }*/

        if (m_bFirstTime)
        {
            /*etPhone.setText(m_tempPhone1);
            m_PhoneNumber = m_tempPhone1;
            sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("PHONE_NUMBER", etPhone.getText().toString());
            editor.commit();
            */

            etPhone2.setText(m_tempPhone2);
            m_EmulatorPhoneNumber = m_tempPhone2;
            if (m_tempPhone2.equals("NOT USED"))
            {
                etPhone2.setText("");
                etPhone2.setHint("NOT USED");
                etPhone2.setEnabled(false);
                sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor = sharedPref.edit();
                editor.putString("PHONE_NUMBER2", m_tempPhone2);
                editor.commit();
            }
            else
            {
                etPhone2.setEnabled(false);
                sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor = sharedPref.edit();
                editor.putString("PHONE_NUMBER2", m_EmulatorPhoneNumber);
                editor.commit();
            }
        }

        String[] PERMISSIONS = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_PHONE_NUMBERS,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        };

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ValidateSMSId();
            }
        }, 3500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Intent intent = new Intent(BroadcastMsgReceiver.MAIN_ACT);
                //intent.putExtra("Action", "Start");
                //sendBroadcast(intent);
                _intent = new Intent(Global.m_MainAct, MainService.class);
                _intent.setAction("startService");
                //i.putExtra("Action", "Start");
                Global.m_MainAct.startService(_intent);
            }
        }, 6500);


        btnSaveMaxSMS = (Button) findViewById(R.id.btSaveMaxSMS);
        btnSaveMaxSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                int iMax = Integer.valueOf(etMaxSMS.getText().toString()) > 100 ? 100 : Integer.valueOf(etMaxSMS.getText().toString());
                editor.putInt("MAX_SMS", iMax);
                editor.commit();
                Global.m_MaxSMS = iMax;
                etMaxSMS.setText(String.valueOf(iMax));
                Toast.makeText(getApplicationContext(), "Maximum Resend SMS Successfully saved",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnSavePhone = (Button) findViewById(R.id.btnSavePhone);
        btnSavePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("PHONE_NUMBER", etPhone.getText().toString());
                editor.commit();
                Global.m_PhoneNumber = etPhone.getText().toString();
                Toast.makeText(getApplicationContext(), "Phone Number Successfully saved",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnSavePhone2 = (Button) findViewById(R.id.btnSavePhone2);
        btnSavePhone2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("PHONE_NUMBER2", etPhone2.getText().toString());
                editor.commit();
                Global.m_PhoneNumber2 = etPhone2.getText().toString();
                Toast.makeText(getApplicationContext(), "Phone Number Successfully saved",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnGetLine1 = (Button) findViewById(R.id.btnGet);
        btnGetLine1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }

                        boolean bPhoneFilled = false;
                        /*String mPhoneNumber = tMgr.getLine1Number();
                        Log.i(Global.TAG, "This phone Number is = " + mPhoneNumber);
                        if (mPhoneNumber.length() > 0) {
                            etPhone.setText(mPhoneNumber);
                            m_PhoneNumber = mPhoneNumber;
                            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("PHONE_NUMBER", etPhone.getText().toString());
                            editor.commit();
                            Toast.makeText(getApplicationContext(), "1: Phone Number Successfully Get and Saved",
                                    Toast.LENGTH_LONG).show();
                            bPhoneFilled = false;
                        }*/

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!bPhoneFilled) {
                                m_dualPhoneNumber.clear();
                                List<SubscriptionInfo> subscription = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
                                for (int i = 0; i < subscription.size(); i++) {
                                    SubscriptionInfo info = subscription.get(i);
                                    Log.d(Global.TAG, "number " + info.getNumber());
                                    Log.d(Global.TAG, "network name : " + info.getCarrierName());
                                    Log.d(Global.TAG, "country iso " + info.getCountryIso());
                                    JSONObject joSimCard = new JSONObject();
                                    try {
                                        joSimCard.put("phoneNumber", info.getNumber());
                                        joSimCard.put("networkName", info.getCarrierName());
                                        m_dualPhoneNumber.add(joSimCard);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (m_dualPhoneNumber.size() > 0)
                                {
                                    try {
                                        etPhone.setText(m_dualPhoneNumber.get(0).getString("phoneNumber"));
                                        m_PhoneNumber = m_dualPhoneNumber.get(0).getString("phoneNumber");
                                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString("PHONE_NUMBER", etPhone.getText().toString());
                                        editor.commit();
                                        Toast.makeText(getApplicationContext(), "2: Phone Number Successfully Get and Saved",
                                                Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }, 1000);
            }
        });

        btnGetLine2 = (Button) findViewById(R.id.btnGet2);
        btnGetLine2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ){
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            m_dualPhoneNumber.clear();
                            List<SubscriptionInfo> subscription = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
                            for (int i = 0; i < subscription.size(); i++) {
                                SubscriptionInfo info = subscription.get(i);
                                Log.d(Global.TAG, "number " + info.getNumber());
                                Log.d(Global.TAG, "network name : " + info.getCarrierName());
                                Log.d(Global.TAG, "country iso " + info.getCountryIso());
                                JSONObject joSimCard = new JSONObject();
                                try {
                                    joSimCard.put("phoneNumber", info.getNumber());
                                    joSimCard.put("networkName", info.getCarrierName());
                                    m_dualPhoneNumber.add(joSimCard);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                if (m_dualPhoneNumber.size() > 1) {
                                    try {
                                        etPhone2.setText(m_dualPhoneNumber.get(1).getString("phoneNumber"));
                                        m_PhoneNumber2 = m_dualPhoneNumber.get(1).getString("phoneNumber");
                                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString("PHONE_NUMBER2", etPhone2.getText().toString());
                                        editor.commit();
                                        Toast.makeText(getApplicationContext(), "2: Phone Number Successfully Get and Saved",
                                                Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }, 1000);
            }
        });

        /*btnSavePhoneId = (Button) findViewById(R.id.btSavePhoneId);
        btnSavePhoneId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("PHONE_ID", etPhoneId.getText().toString());
                editor.commit();
                Global.m_PhoneId = etPhoneId.getText().toString();
                Toast.makeText(getApplicationContext(), "Phone ID Successfully saved",
                        Toast.LENGTH_LONG).show();
            }
        });
        */


        btnResend = (Button) findViewById(R.id.btnResend);
        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnResend.getText().equals("START")) {
                    mSending = true;
                    Toast.makeText(getApplicationContext(), "Send SMS service Started",
                            Toast.LENGTH_LONG).show();
                    btnResend.setText("STOP");
                    tvSMSCount.setText("Service Started!");
                    tvSMSCount.setTextColor(Color.BLACK);
                } else {
                    mFailCount = 0;
                    mNeedToRestart = 0;
                    mSending = false;
                    Toast.makeText(getApplicationContext(), "Send SMS service Stoped",
                            Toast.LENGTH_LONG).show();
                    btnResend.setText("START");
                    tvSMSCount.setText("Service Stopped!");
                    tvSMSCount.setTextColor(Color.BLACK);
                }
            }
        });

        btnCheckInternet = (Button) findViewById(R.id.btnCheckInternet);
        btnCheckInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                if (!IsServiceStillActive()) {
                    Log.e(Global.TAG, "Service has been closed");
                    RestartApplication("Service has been stoped, please RESTART SMSREADER!");
                } else {
                    String szMessage = "";
                    if (mSending) {
                        Toast.makeText(getApplicationContext(), "Service OK",
                                Toast.LENGTH_LONG).show();
                        szMessage = "Service is OK, do you want to restart SMSREADER ?";
                        RestartApplication(szMessage);
                    } else
                        Toast.makeText(getApplicationContext(), "Service not yet started",
                                Toast.LENGTH_LONG).show();
                }*/

                SmsManager sms = null;
                try {
                    sms = SmsManager.getSmsManagerForSubscriptionId(m_dualPhoneNumber.get(0).getInt("subscriptionId"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sms.sendTextMessage(m_EmulatorPhoneNumber,null, "This is a test message, to make sure the phone can send to the given number", null, null);
            }
        });

        btnRestart = (Button) findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnSMSId = (Button) findViewById(R.id.btnSMSId);
        btnSMSId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogSMSId dialog = new DialogSMSId(m_MainAct);
                dialog.show();
            }
        });

        tvSMSCount = (TextView) findViewById(R.id.tvSMSCount);
        tvSMSData = (TextView) findViewById(R.id.tvSMSData);
        tvLastSend = (TextView) findViewById(R.id.tvLastSend);
        tvLastChecked = (TextView) findViewById(R.id.tvLastChecked);
        tvSMSData.setMovementMethod(new ScrollingMovementMethod());

        tvSMSCount.setText("  - no data send yet - \r\nplease start the service");
        //OnSetSMSText();

        etMaxSMS.setKeyListener(null);
        btnSaveMaxSMS.setVisibility(View.GONE);
        btnRestart.setVisibility(View.GONE);
        btnGetLine1.setVisibility(View.GONE);
        btnGetLine2.setVisibility(View.GONE);
        btnSavePhone.setVisibility(View.GONE);
        btnSavePhone2.setVisibility(View.GONE);
        btnResend.setVisibility(View.GONE);
        //btnCheckInternet.setVisibility(View.GONE);

        etPhone.setEnabled(false);
        etPhone2.setEnabled(false);

        /*String permission = Manifest.permission.RECEIVE_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if ( grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }

        permission = Manifest.permission.READ_SMS;
        grant = ContextCompat.checkSelfPermission(this, permission);
        if ( grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
        */
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    mSending = false;
                    finishAffinity();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    DialogInterface.OnClickListener restartAppClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    mSending = false;
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private String _phoneNumber1 = "";
    DialogInterface.OnClickListener dialogPhoneNumber1Confirmation = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    etPhone.setText(_phoneNumber1);
                    m_PhoneNumber = _phoneNumber1;
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("PHONE_NUMBER", _phoneNumber1);
                    editor.commit();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                    editor = sharedPref.edit();
                    editor.putString("PHONE_NUMBER", m_tempPhone1);
                    editor.commit();
                    break;
            }
        }
    };

    private String _phoneNumber2 = "";
    DialogInterface.OnClickListener dialogPhoneNumber2Confirmation = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    etPhone2.setText(_phoneNumber2);
                    m_PhoneNumber2 = _phoneNumber2;
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("PHONE_NUMBER2", _phoneNumber2);
                    editor.commit();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                    editor = sharedPref.edit();
                    editor.putString("PHONE_NUMBER2", m_tempPhone2);
                    editor.commit();
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("Do you want to close this application?");

        builder.setPositiveButton("YES", dialogClickListener)
                .setNegativeButton("NO", dialogClickListener);

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        //Intent broadcastIntent = new Intent();
        //broadcastIntent.setAction("restartService");
        //broadcastIntent.setClass(this, BroadcastMsgReceiver.class);
        //this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    public void StartInit() {
        if (!bAlreadyInit) {
            bAlreadyInit = true;

            Calendar cal = Calendar.getInstance();
            Intent i = new Intent(this, MainService.class);
            PendingIntent pintent = PendingIntent.getService(this, 0, i, 0);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + (30 * 10000), 30 * 1000, pintent);
        }
    }

    public void SendSMS(String szSMSData) {
        try {
            JSONObject jso = new JSONObject(szSMSData);
            JSONObject jsoResult = jso.getJSONObject("result");
            String szStatus = jso.getString("status");

            if (!szStatus.equals("Success"))
                return;

            //int iTotalCount = jsoResult.getInt("totalCount");
            //if (iTotalCount <= 0)
            //    return;

            JSONObject jsoData = jsoResult.getJSONObject("data");
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(jsoData.getString("Phone"), null, jsoData.getString("Message"), null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Log.e(Global.TAG, ex.getMessage());
            //Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
            //        Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    public void OnSetSMSText(String szFlag, String szMessages) {
        /*
        try {
            JSONArray jaFailed = (JSONArray) dbSMS.getFailedSMS("SMS");
            if (jaFailed.length() > 0)
            {
                tvSMSCount.setText("SMS Failed to send (" + jaFailed.length() + ")");
                tvSMSCount.setTextColor(Color.RED);
            }
            else {
                tvSMSCount.setText("Clear!");
                tvSMSCount.setTextColor(Color.GREEN);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonArray jaSMSData = (JsonArray) dbSMS.getLastSMS("SMS", 100);
            tvSMSData.setText(gson.toJson(jaSMSData));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
        if (szFlag.equals("Valid")) {
            _szLastSend = "Last send sms = " + getCurrentTimeStamp();
            tvLastSend.setText(_szLastSend);
            tvLastSend.setTextColor(Color.GREEN);
        } else if (szFlag.equals("Invalid")) {
            _szLastSend = "Last send sms = " + getCurrentTimeStamp() + "\r\nINVALID\r\n" + szMessages;
            tvLastSend.setText(_szLastSend);
            tvLastSend.setTextColor(Color.RED);
        } else if (szFlag.equals("Finish")) {
            //if (!_szLastSend.equals(""))
            //    tvSMSCount.setText(_szLastSend + "\r\n" + szMessages);
            //else
            tvSMSCount.setText(szMessages);
            tvSMSCount.setTextColor(Color.BLACK);
        } else if (szFlag.equals("LastChecked")) {
            tvLastChecked.setText("Last checked sms inbox = " + getCurrentTimeStamp());
        } else if (szFlag.equals("Internet")) {
            tvSMSCount.setText(szMessages);
            if (szMessages.equals("Connected"))
                tvSMSCount.setTextColor(Color.GREEN);
            else
                tvSMSCount.setTextColor(Color.RED);
        }
    }

    public void OnInternetNoConnection() {
        Global.mSending = false;
        Global.mFailCount = 0;
        Global.mNeedToRestart = 0;
        mToken = "";
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("TOKEN", mToken);
        editor.commit();

        //StartLoginActivity();
        RestartApplication("Internet connection has been loss, please RESTART SMSREADER!");
    }

    private String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date());
            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void ResendFailedSMS() {
        try {
            JSONArray jaFailedSMS = (JSONArray) dbSMS.getFailedSMS("SMS");
            if (jaFailedSMS != null) {
                for (int i = 0; i < jaFailedSMS.length(); i++) {
                    JSONObject joData = jaFailedSMS.getJSONObject(i);
                    String json = joData.toString();
                    (new Thread() {
                        public void run() {
                            try {
                                URL url = new URL(Global.m_URL_POST);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setDoInput(true);
                                connection.setDoOutput(true);
                                connection.setUseCaches(false);
                                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                                connection.setRequestProperty("Accept", "application/json");
                                connection.setRequestProperty("Authorization", "Bearer " + mToken);
                                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                                osw.write(json);
                                osw.flush();
                                osw.close();

                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    StringBuilder sb = new StringBuilder();
                                    BufferedReader br = new BufferedReader(new InputStreamReader(
                                            connection.getInputStream(), "utf-8"));
                                    String line = null;
                                    while ((line = br.readLine()) != null) {
                                        sb.append(line + "\n");
                                    }
                                    br.close();
                                    Log.i(Global.TAG, "" + sb.toString());
                                    dbSMS.updateDataSentFlag("SMS", joData.getString("ID"), 1);
                                /*runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        OnSetSMSText();
                                    }
                                });*/
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void ResendLastSMS() {
        JSONObject jsoResend = new JSONObject();
        JSONArray jsaResend = new JSONArray();
        for (int i = 0; i < Global.m_smsList.size(); i++)
            jsaResend.put(Global.m_smsList.get(i));

        try {
            jsoResend.put("data", jsaResend);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String json = jsoResend.toString();
        Log.i(Global.TAG, json);

        (new Thread() {
            public void run() {
                try {
                    URL url = new URL(Global.m_URL_SMS_RESEND);
                    Log.i(Global.TAG, "RESEND URL = " + Global.m_URL_SMS_RESEND);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer " + mToken);
                    OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                    osw.write(json);
                    osw.flush();
                    osw.close();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        StringBuilder sb = new StringBuilder();
                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                connection.getInputStream(), "utf-8"));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();

                        Log.i(Global.TAG, "" + sb.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Last " + Global.m_smsList.size() + "(s) sms successfully sent",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void StartLoginActivity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Internet Connection");
        builder.setMessage("No internet connection, please relogin.");

        builder.setPositiveButton("YES", dialogClickListener);

        AlertDialog alert = builder.create();
        alert.show();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "No Internet Connection",
                Toast.LENGTH_LONG).show();
    }

    public void RestartApplication(String szMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Service");
        builder.setMessage(szMessage);

        builder.setPositiveButton("YES", restartAppClickListener);
        builder.setNegativeButton("NO", restartAppClickListener);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void SendHeartBeat(String json) {
        try {
            URL url = new URL(Global.m_URL_HEARTHBEAT_POST);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + mToken);
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            osw.write(json);
            osw.flush();
            osw.close();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                Log.i(Global.TAG, "" + sb.toString());
                JSONObject jsonResponse = new JSONObject(sb.toString());
                String szStatus = jsonResponse.getString("Status");
                if (szStatus.equals("OK"))
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OnSetSMSText("Internet", "Connected");
                        }
                    });

            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    OnSetSMSText("Internet", "Disconnect from server, please RESTART SMSREADER NOW !");
                }
            });
        }

        Log.i(Global.TAG, "Sending HeartBeat SMS Http POST");
    }

    private boolean IsServiceStillActive() {
        Calendar now = Calendar.getInstance();
        if (Global.mLastCheckTimeStamp == null)
            return true;

        long timeSpan = Global.mLastCheckTimeStamp.getTimeInMillis() - now.getTimeInMillis();
        long lMinutes = TimeUnit.MILLISECONDS.toMinutes(timeSpan);
        if (lMinutes >= 2)
            return false;
        return true;
    }

    public JSONArray GetSMSInboxData(int iSubId) {
        final String SMS_URI_INBOX = "content://sms/inbox";
        JSONArray jaSMSInbox = new JSONArray();

        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String selection = "type = 1 AND " + m_queryInboxSimId + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(iSubId)};

            Cursor cur = getContentResolver().query(uri, null, selection, selectionArgs, "_id DESC LIMIT " + String.valueOf(Global.m_MaxSMS));
            if (cur.moveToFirst()) {
                int index_id = cur.getColumnIndex("_id");
                int index_Address = cur.getColumnIndex("address");
                int index_Person = cur.getColumnIndex("person");
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                int index_Type = cur.getColumnIndex("type");
                int index_ServiceCenter = cur.getColumnIndex("service_center");
                do {
                    String szId = cur.getString(index_id);
                    String strAddress = cur.getString(index_Address);
                    int intPerson = cur.getInt(index_Person);
                    String strbody = cur.getString(index_Body);
                    long longDate = cur.getLong(index_Date);
                    int int_Type = cur.getInt(index_Type);
                    String strServiceCenter = cur.getString(index_ServiceCenter);
                    JSONObject joSMSInbox = new JSONObject();
                    joSMSInbox.put("Body", strbody);
                    jaSMSInbox.put(joSMSInbox);
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            }
        } catch (Exception ex) {
            Log.d("ERROR = ", ex.getMessage());
                return jaSMSInbox;
        }
            return jaSMSInbox;
    }

    private int GetSMSInbox(int iSubId) {
        final String SMS_URI_INBOX = "content://sms/inbox";
        int iTotalSMS = 0;
        try {

            Uri uri = Uri.parse(SMS_URI_INBOX);

            String selection = "type = 1 AND " + m_queryInboxSimId + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(iSubId)};
            JSONArray jaSMSInbox = new JSONArray();
            Cursor cur = getContentResolver().query(uri, null, selection, selectionArgs, "_id DESC"); //LIMIT " + String.valueOf(Global.m_MaxSMS));
            if (cur.moveToFirst()) {
                int index_id = cur.getColumnIndex("_id");
                int index_Address = cur.getColumnIndex("address");
                int index_Person = cur.getColumnIndex("person");
                int index_Body = cur.getColumnIndex("body");
                int index_Date = cur.getColumnIndex("date");
                int index_Type = cur.getColumnIndex("type");
                int index_ServiceCenter = cur.getColumnIndex("service_center");
                do {
                    String szId = cur.getString(index_id);
                    String strAddress = cur.getString(index_Address);
                    int intPerson = cur.getInt(index_Person);
                    String strbody = cur.getString(index_Body);
                    long longDate = cur.getLong(index_Date);
                    int int_Type = cur.getInt(index_Type);
                    String strServiceCenter = cur.getString(index_ServiceCenter);
                    JSONObject joSMSInbox = new JSONObject();
                    joSMSInbox.put("Body", strbody);
                    jaSMSInbox.put(joSMSInbox);
                    iTotalSMS += 1;
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }
            }
        } catch (Exception ex) {
            Log.d("ERROR = ", ex.getMessage());
            if (ex.getMessage().indexOf("no such column: sim_id") >= 0) {
                m_queryInboxSimId = "sub_id";
                return -9;
            }
            return iTotalSMS;
        }

        return iTotalSMS;
    }

    public boolean InsertIntoDatabase(String szId, String szFrom, String szBody, String szServiceCenter, Long lDateTime)
    {
        String szBuildNumber = Build.SERIAL.toUpperCase();
        JSONObject jsoMsg = null;
        try {
            jsoMsg = new JSONObject();
            jsoMsg.put("ID", szId);
            jsoMsg.put("SN", szBuildNumber);
            jsoMsg.put("From", szFrom);
            jsoMsg.put("Body", szBody);
            jsoMsg.put("phoneNumber", Global.m_PhoneNumber);
            jsoMsg.put("sentFlag", 0);
            if (szServiceCenter != null)
                jsoMsg.put("serviceCenter", szServiceCenter);
            else
                jsoMsg.put("serviceCenter", "");
            Date dateTime = new Date(lDateTime);
            String formatedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(dateTime);
            jsoMsg.put("dateTime", formatedDate);

            Global.m_MainAct.dbSMS.insertData("SMS", jsoMsg);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void ValidateSMSId()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TelephonyManager tMgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                boolean bPhoneFilled = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!bPhoneFilled) {
                        m_dualPhoneNumber.clear();
                        List<SubscriptionInfo> subscription = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
                        for (int i = 0; i < subscription.size(); i++) {
                            SubscriptionInfo info = subscription.get(i);
                            Log.d(Global.TAG, "number " + info.getNumber());
                            Log.d(Global.TAG, "network name : " + info.getCarrierName());
                            Log.d(Global.TAG, "country iso " + info.getCountryIso());
                            JSONObject joSimCard = new JSONObject();
                            try {
                                joSimCard.put("phoneNumber", info.getNumber());
                                joSimCard.put("networkName", info.getCarrierName());
                                joSimCard.put("subscriptionId", info.getSubscriptionId());
                                m_dualPhoneNumber.add(joSimCard);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        int iSimCardCount = 0;
                        m_iDualSimSender = 0;
                        for (int i = 0; i < 3; i++) {
                            int iTotalSMS = GetSMSInbox(i);
                            if (iTotalSMS == -9)
                                iTotalSMS = GetSMSInbox(i);
                            if (iTotalSMS > 0) {
                                try {
                                    JSONObject joSimcard = m_dualPhoneNumber.get(iSimCardCount);
                                    joSimcard.put("sms_id", i);
                                    joSimcard.put("totalSMS", iTotalSMS);
                                    joSimcard.put("isValid", 1);
                                    iSimCardCount += 1;
                                    if (iSimCardCount == m_dualPhoneNumber.size())
                                        break;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    Log.i(Global.TAG, m_dualPhoneNumber.toString());
                    Toast.makeText(getApplicationContext(), m_dualPhoneNumber.toString(),
                            Toast.LENGTH_LONG).show();

                    m_bFirstTime = false;
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("FirstTime", false);
                    editor.commit();
                }
            }
        }, 5000);
    }
}
