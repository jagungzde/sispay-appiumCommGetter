package com.release.appiumcommgetter;

import static com.release.appiumcommgetter.Global.TAG;
import static com.release.appiumcommgetter.Global.mFailCount;
import static com.release.appiumcommgetter.Global.mNeedToRestart;
import static com.release.appiumcommgetter.Global.mPassword;
import static com.release.appiumcommgetter.Global.mSending;
import static com.release.appiumcommgetter.Global.mToken;
import static com.release.appiumcommgetter.Global.mUserId;
import static com.release.appiumcommgetter.Global.m_EmulatorPhoneNumber;
import static com.release.appiumcommgetter.Global.m_PhoneNumber;
import static com.release.appiumcommgetter.Global.m_PhoneNumber2;
import static com.release.appiumcommgetter.Global.m_dualPhoneNumber;
import static com.release.appiumcommgetter.Global.m_queryInboxSimId;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//import android.support.annotation.Nullable;

/**
 * Created by yulie on 03/08/2018.
 */

public class MainService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        Global.m_MainService = this;

        Log.i(Global.TAG, "Service Started !");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            StartServiceForeGround();
        else
            startForeground(1, new Notification());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //String szMode = intent.getStringExtra("Action");

        //if (szMode == null) {
        //    return START_STICKY;
        //}
        if (intent == null)
            return START_STICKY;

        if (intent.getAction().equals("startService")) {
            publishResults("Action", "Init", null);
        }

        if (intent.getAction().equals("restartService"))
            reLogin(false);

        HeartBeatSMS();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        //this.stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartService");
        broadcastIntent.setClass(this, BroadcastMsgReceiver.class);
        this.sendBroadcast(broadcastIntent);
        //stopForeground(true);
        //stopSelf();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void StartServiceForeGround() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("SMSReader")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private void publishResults(String szMode, String szAct, Bundle b) {
        Intent intent = new Intent(BroadcastMsgReceiver.MAIN_SERVICE);
        intent.putExtra(szMode, szAct);
        intent.putExtra(szAct, b);
        sendBroadcast(intent);
    }

    Handler hThreadHandle = new Handler() {
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            int iType = b.getInt("Type");

            switch (iType) {
                case 1: // Received SMS and Do Something here !
                    /*Object[] objParam = (Object[]) msg.obj;
                    Log.i(Global.TAG, "SMSReceived = " + objParam[0].toString());
                    Log.i(Global.TAG, "From = " + objParam[1].toString());
                    String szBuildNumber = Build.SERIAL.toUpperCase();
                    String szBody = objParam[0].toString();

                    JSONObject jsoMsg = null;

                    try {
                        jsoMsg = new JSONObject();
                        jsoMsg.put("ID", ((Long) System.currentTimeMillis()).toString());
                        jsoMsg.put("SN", szBuildNumber);
                        jsoMsg.put("From", objParam[1].toString());
                        jsoMsg.put("Body", objParam[0].toString());
                        jsoMsg.put("phoneNumber", Global.m_PhoneNumber);
                        jsoMsg.put("sentFlag", 0);
                        JSONObject joDB = new JSONObject();
                        Global.m_MainAct.dbSMS.insertData("SMS", jsoMsg);
                    } catch (Exception ex) {
                    }
                    Global.m_MainAct.OnSetSMSText();
                    final String json = jsoMsg.toString();
                    final JSONObject jsoResend = jsoMsg;
                    Log.i(Global.TAG, json);
                    Global.m_smsList.addFirst(jsoResend);
                    if (Global.m_smsList.size() > 200)
                        Global.m_smsList.removeLast();

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
                                    Global.m_MainAct.dbSMS.updateDataSentFlag("SMS", jsoResend.getString("ID"), 1);
                                    Global.m_MainAct.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Global.m_MainAct.OnSetSMSText();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    */
                    //GetAllSMSInbox();
                    //SendUnsentSMS();
                    break;
            }
        }
    };

    public void PutThreadHandle(int iType, Object objData) {
        Message msg = hThreadHandle.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("Type", iType);
        msg.obj = objData;
        msg.setData(b);
        hThreadHandle.sendMessage(msg);
    }

    private void HeartBeatSMS() {
        JSONObject jso = null;
        try {
            jso = new JSONObject();
            jso.put("SN", Build.SERIAL.toUpperCase());
            jso.put("DeviceID", Build.SERIAL.toUpperCase());
            jso.put("phoneNumber", Global.m_PhoneNumber);
            jso.put("phoneId", Global.m_PhoneId);
        } catch (Exception ex) {
            Log.e(Global.TAG, ex.getMessage());
        }

        final String json = jso.toString();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int iCount = 0;
                StartMqttClient();

                while (true) {
                    if (iCount >= 30) {
                        iCount = 0;
                        SendHeartBeat(json);
                        //if (mFailCount > 0 || !mSending)
                        //if (!mSending)
                        //    continue;
                        //GetAllSMSInboxOld();
                        //SendUnsentSMS();
                        //SendForgotPassword(json);
                        Global.mLastCheckTimeStamp = Calendar.getInstance();
                    }

                    //if (iCount % 5 == 0) {
                        //GetLastSMSInbox(5);
                        //GetCommandAndroid();
                    //}

                    try {
                        iCount++;
                        //	iRecvHeartBeat++;
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.e(Global.TAG, e.getMessage());
                    }
                }
            }
        });

        t.start();
    }

    private void StartMqttClient()
    {
        String serverUri = "tcp://"+Global.m_MQTT_HOST+":"+Global.m_MQTT_PORT;
        String clientId = "comm-getter";
        String username = "papinaga";
        String password = "Cideng87(";
        String topic = String.format("comm-getter-get-otp/%s", m_PhoneNumber.replace("+", ""));

        try {
            MqttClient mqttClient = new MqttClient(serverUri, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            mqttClient.connect(options);
            Log.i(TAG, "MQTT Client connected and ready");
            mqttClient.subscribe(topic);
            Log.i(TAG, "Subcribe Topic = " + topic);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "MQTT LOST CONNECTION");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    Log.i(TAG, "MQTT Data 1 = " + payload);
                    String szBody = payload;
                    //JSONObject jsonResponse = new JSONObject(payload);
                    //String szBody = jsonResponse.getString("body");
                    SmsManager sms = SmsManager.getSmsManagerForSubscriptionId(m_dualPhoneNumber.get(0).getInt("subscriptionId"));
                    if (szBody.length() > 0)
                        sms.sendTextMessage(m_EmulatorPhoneNumber,null, szBody, null, null);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }catch (Exception ex)
        {
            Log.e(Global.TAG, ex.getMessage());
        }
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
            osw.write("{}");
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
                if (jsonResponse.getString("status").equals("FAIL"))
                {
                    if (jsonResponse.getString("messages").equals("Invalid Token"))
                        reLogin(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(Global.TAG, "CommGetter Sending HeartBeat");
    }

    public void SendForgotPassword(String json) {
        try {
            URL url = new URL(Global.m_URL_FORGETPASSWORD_POST + "/forgotpassword/pending");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            osw.write(json);
            osw.flush();
            osw.close();
            Log.i(Global.TAG, Global.m_URL_FORGETPASSWORD_POST + "/forgotpassword/pending - Send Parameter : " + json);

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
                Bundle b = new Bundle();
                b.putString("SMSData", sb.toString());
                publishResults("Action", "SendSMS", b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void GetAllSMSInbox() {
        if (Global.mFailCount > 0)
        {
            Global.mNeedToRestart += 1;
            if (mNeedToRestart >= 5)
                Global.m_MainAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Global.m_MainAct.OnInternetNoConnection();
                    }
                });
            else
                return;
        }
        else
            mNeedToRestart = 0;

        //StringBuilder smsBuilder = new StringBuilder();
        final String SMS_URI_INBOX = "content://sms/inbox";
        //final String SMS_URI_ALL = "content://sms/";
        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            //String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};

            //String selection = "type = 1 AND sim_id = ?";
            //String selection = "type = 1 AND sub_id = ?";
            //String[] selectionArgs = new String[]{String.valueOf(m_iSubId)};

            //Cursor cur = getContentResolver().query(uri, null, selection, selectionArgs, "_id DESC LIMIT " + String.valueOf(Global.m_MaxSMS));
            //if (m_iDualSimSender >= 2)
            //    cur = getContentResolver().query(uri, null, null, null, "_id DESC LIMIT " + String.valueOf(Global.m_MaxSMS));
            //else{
            //    String selection = "type = 1 AND " + m_queryInboxSimId + " = ?";
            //    String[] selectionArgs = new String[]{String.valueOf(m_iSubId)};
            //    cur = getContentResolver().query(uri, null, selection, selectionArgs, "_id DESC LIMIT " + String.valueOf(Global.m_MaxSMS));
            //}
            for (int i = 0; i < m_dualPhoneNumber.size(); i++)
            {
                JSONObject joSimcard = m_dualPhoneNumber.get(i);
                String szPhoneNumber = "";
                if (i == 0)
                    szPhoneNumber = m_PhoneNumber;
                if (i == 1)
                    szPhoneNumber = m_PhoneNumber2;

                if (joSimcard.getInt("isValid") == 1)
                {
                    Cursor cur = null;
                    String selection = "type = 1 AND " + m_queryInboxSimId + " = ?";
                    String[] selectionArgs = new String[]{String.valueOf(joSimcard.getInt("sms_id"))};
                    cur = getContentResolver().query(uri, null, selection, selectionArgs, "_id DESC LIMIT " + Global.m_MaxSMS);
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
                            /*smsBuilder.append("[ ");
                            smsBuilder.append(strAddress + ", ");
                            smsBuilder.append(intPerson + ", ");
                            smsBuilder.append(strbody + ", ");
                            smsBuilder.append(longDate + ", ");
                            smsBuilder.append(int_Type);
                            smsBuilder.append(" ]\n\n");
                             */

                            JSONObject joSMS = (JSONObject) Global.m_MainAct.dbSMS.getSMSbyId("SMS", szId);
                            if (joSMS == null) {
                                InsertIntoDatabase(szId, strAddress, strbody, strServiceCenter, longDate, szPhoneNumber);
                                mFailCount += 1;
                            }
                            else
                            {
                                int iSentFlag = joSMS.getInt("sentFlag");
                                if (iSentFlag == 0)
                                    mFailCount += 1;
                            }

                        } while (cur.moveToNext());

                        if (!cur.isClosed()) {
                            cur.close();
                            cur = null;
                            if (mFailCount == 0)
                                Global.m_MainAct.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Global.m_MainAct.OnSetSMSText("Finish", "All SMS already sent");
                                    }
                                });
                            else
                                Global.m_MainAct.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Global.m_MainAct.OnSetSMSText("Finish", mFailCount + " sms(s) not sent");
                                    }
                                });

                            Global.m_MainAct.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Global.m_MainAct.OnSetSMSText("LastChecked", "");
                                }
                            });
                        }
                        //Log.i(Global.TAG, smsBuilder.toString());
                    }
                }
            }
        } catch (SQLiteException | JSONException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
    }

    private void GetAllSMSInboxOld() {
        if (Global.mFailCount > 0)
        {
            Global.mNeedToRestart += 1;
            if (mNeedToRestart >= 5)
                Global.m_MainAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Global.m_MainAct.OnInternetNoConnection();
                    }
                });
            else
                return;
        }
        else
            mNeedToRestart = 0;

        final String SMS_URI_INBOX = "content://sms/inbox";

        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            Cursor cur = getContentResolver().query(uri, null, null, null, "_id DESC LIMIT " + String.valueOf(Global.m_MaxSMS));
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

                    JSONObject joSMS = (JSONObject) Global.m_MainAct.dbSMS.getSMSbyId("SMS", szId);
                    if (joSMS == null) {
                        InsertIntoDatabase(szId, strAddress, strbody, strServiceCenter, longDate, m_PhoneNumber);
                        mFailCount += 1;
                    }
                    else
                    {
                        int iSentFlag = joSMS.getInt("sentFlag");
                        if (iSentFlag == 0)
                            mFailCount += 1;
                    }

                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                    if (mFailCount == 0)
                        Global.m_MainAct.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Global.m_MainAct.OnSetSMSText("Finish", "All SMS already sent");
                            }
                        });
                    else
                        Global.m_MainAct.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Global.m_MainAct.OnSetSMSText("Finish", mFailCount + " sms(s) not sent");
                            }
                        });

                    Global.m_MainAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Global.m_MainAct.OnSetSMSText("LastChecked", "");
                        }
                    });
                }
            }
        } catch (SQLiteException | JSONException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
    }

    private void GetLastSMSInbox(int iTop) {
        final String SMS_URI_INBOX = "content://sms/inbox";
        try {
            JSONArray jaData = new JSONArray();
            Uri uri = Uri.parse(SMS_URI_INBOX);
            Cursor cur = getContentResolver().query(uri, null, null, null, "_id DESC LIMIT " + iTop);
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

                    JSONObject joData = new JSONObject();
                    joData.put("id", szId);
                    joData.put("body", strbody);
                    jaData.put(joData);
                } while (cur.moveToNext());

                if (!cur.isClosed()) {
                    cur.close();
                    cur = null;
                }

                JSONObject joSend = new JSONObject();
                joSend.put("phoneNumber", m_PhoneNumber);
                joSend.put("data", jaData);

                (new Thread() {
                    public void run()
                    {
                        try
                        {
                            URL url = new URL(Global.m_BASE_URL + "/sms/otp1.php");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setDoInput(true);
                            connection.setDoOutput(true);
                            connection.setUseCaches(false);
                            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            connection.setRequestProperty("Accept", "application/json");
                            connection.setRequestProperty("Authorization", "Bearer " + mToken);
                            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                            Log.i(Global.TAG, "Sending OTP1 Data = " + joSend.toString());
                            osw.write(joSend.toString());
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
                                String response = sb.toString();
                                if (response.equals("success\n"))
                                {
                                    Log.i(Global.TAG, "Sending last " + iTop + " SMS Success");
                                }
                            }
                        } catch(IOException e){
                            Log.e(Global.TAG, "Internet is not connected");
                            Global.m_MainAct.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mSending)
                                        Global.m_MainAct.OnInternetNoConnection();
                                }
                            });
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (SQLiteException | JSONException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
    }

    public boolean InsertIntoDatabase(String szId, String szFrom, String szBody, String szServiceCenter, Long lDateTime, String szPhoneNumber)
    {
        String szBuildNumber = Build.SERIAL.toUpperCase();
        JSONObject jsoMsg = null;
        try {
            jsoMsg = new JSONObject();
            jsoMsg.put("ID", szId);
            jsoMsg.put("SN", szBuildNumber);
            jsoMsg.put("From", szFrom);
            jsoMsg.put("Body", szBody);
            jsoMsg.put("phoneNumber", szPhoneNumber);
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

    private void SendUnsentSMS()
    {
        try {
            JSONArray jaFailedSMS = (JSONArray) Global.m_MainAct.dbSMS.getFailedSMS("SMS");
            if (jaFailedSMS != null)
            {
                if (mToken.equals("")) {
                    reLogin(true);
                    return;
                }

                for (int i = 0; i < jaFailedSMS.length(); i++)
                {
                    JSONObject joData = jaFailedSMS.getJSONObject(i);
                    joData.put("phoneId", Global.m_PhoneId);

                    String json = joData.toString();
                    (new Thread() {
                        public void run()
                        {
                            try
                            {
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
                                    JSONObject jsonResponse = new JSONObject(sb.toString());
                                    String szStatus = jsonResponse.getString("Status");
                                    String szFlag = jsonResponse.getString("Flag");
                                    String szId = jsonResponse.getString("id");
                                    String szMessages = jsonResponse.getString("Messages");
                                    Global.mFailCount -= 1;

                                    if (szStatus.equals("Success"))
                                    {
                                        Global.m_MainAct.dbSMS.updateDataSentFlag("SMS", szId, 1);
                                        Global.m_MainAct.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Global.m_MainAct.OnSetSMSText(szFlag, szMessages);
                                            }
                                        });
                                    }
                                    else {
                                        if (szMessages.equals("Invalid Token"))
                                            reLogin(true);
                                        else {
                                            Global.m_MainAct.dbSMS.updateDataSentFlag("SMS", szId, 1);
                                            Global.m_MainAct.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Global.m_MainAct.OnSetSMSText(szFlag, szMessages);
                                                }
                                            });
                                        }
                                    }
                                }
                            } catch(IOException e){
                              Log.e(Global.TAG, "Internet is not connected");
                              Global.m_MainAct.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mSending)
                                            Global.m_MainAct.OnInternetNoConnection();
                                    }
                              });
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void reLogin(boolean bInvalidToken)
    {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
        if (sharedPref.contains("TOKEN")) {
            mToken = sharedPref.getString("TOKEN", "");
        }

        if (mToken.equals("") || bInvalidToken == true)
        {
            (new Thread() {
                public void run() {
                    try {
                        URL url = new URL(Global.m_URL_AUTH);
                        Log.i(Global.TAG, "Login URL = "+ Global.m_URL_AUTH);

                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoInput(true);
                        connection.setDoOutput(true);
                        connection.setUseCaches(false);
                        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        connection.setRequestProperty("Accept", "application/json");
                        OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("username", mUserId);
                        jsonParam.put("password", mPassword);
                        jsonParam.put("isLoginApp", true);
                        String json = jsonParam.toString();
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
                            String szStatus = jsonResponse.getString("status");
                            final String szMessage = jsonResponse.getString("message");
                            if (szStatus.equals("success"))
                            {
                                JSONObject jsonData = jsonResponse.getJSONObject("data");
                                mToken = jsonData.getString("token");
                                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("TOKEN", mToken);
                                editor.commit();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void GetCommandAndroid()
    {
        (new Thread() {
            public void run() {
                try {
                    URL url = new URL(Global.m_BASE_URL + "/appium/get_command_android.php");
                    Log.i(Global.TAG, "Get Command Android = "+ Global.m_BASE_URL + "/appium/get_command_android.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer " + mToken);
                    OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("phoneNumber", m_PhoneNumber);
                    String json = jsonParam.toString();
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
                        String szStatus = jsonResponse.getString("status");
                        if (szStatus.equals("success"))
                        {
                            JSONObject jsonData = jsonResponse.getJSONObject("data");
                            int iAction = jsonData.getInt("action");
                            if (iAction == 1)
                            {
                                url = new URL(Global.m_BASE_URL + "/appium/get_otp.php");
                                Log.i(Global.TAG, "Get OTP = "+ Global.m_BASE_URL + "/appium/get_otp.php");
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setDoInput(true);
                                connection.setDoOutput(true);
                                connection.setUseCaches(false);
                                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                                connection.setRequestProperty("Accept", "application/json");
                                connection.setRequestProperty("Authorization", "Bearer " + mToken);
                                osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                                jsonParam = new JSONObject();
                                jsonParam.put("phoneNumber", m_PhoneNumber); //""+01978202491");
                                json = jsonParam.toString();
                                osw.write(json);
                                osw.flush();
                                osw.close();

                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    sb = new StringBuilder();
                                    br = new BufferedReader(new InputStreamReader(
                                            connection.getInputStream(), "utf-8"));
                                    line = null;
                                    while ((line = br.readLine()) != null) {
                                        sb.append(line + "\n");
                                    }
                                    br.close();

                                    Log.i(Global.TAG, "" + sb.toString());
                                    jsonResponse = new JSONObject(sb.toString());
                                    szStatus = jsonResponse.getString("status");
                                    if (szStatus.equals("success")) {
                                        String szBody = jsonResponse.getJSONObject("data").getString("body");
                                        SmsManager sms = SmsManager.getSmsManagerForSubscriptionId(m_dualPhoneNumber.get(0).getInt("subscriptionId"));
                                        if (szBody.length() > 0)
                                            sms.sendTextMessage(m_EmulatorPhoneNumber,null, szBody, null, null);
                                    }
                                }

                                url = new URL(Global.m_BASE_URL + "/appium/set_command_android.php");
                                Log.i(Global.TAG, "Get OTP = "+ Global.m_BASE_URL + "/appium/set_command_android.php");
                                connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("POST");
                                connection.setDoInput(true);
                                connection.setDoOutput(true);
                                connection.setUseCaches(false);
                                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                                connection.setRequestProperty("Accept", "application/json");
                                connection.setRequestProperty("Authorization", "Bearer " + mToken);
                                osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                                jsonParam = new JSONObject();
                                jsonParam.put("phoneNumber", m_PhoneNumber);
                                jsonParam.put("action", 0);
                                json = jsonParam.toString();
                                osw.write(json);
                                osw.flush();
                                osw.close();

                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    sb = new StringBuilder();
                                    br = new BufferedReader(new InputStreamReader(
                                            connection.getInputStream(), "utf-8"));
                                    line = null;
                                    while ((line = br.readLine()) != null) {
                                        sb.append(line + "\n");
                                    }
                                    br.close();
                                    Log.i(Global.TAG, "" + sb.toString());
                                    jsonResponse = new JSONObject(sb.toString());
                                    szStatus = jsonResponse.getString("status");
                                    if (szStatus.equals("success")) {
                                        Log.i(TAG, "set action back to zero success");
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
