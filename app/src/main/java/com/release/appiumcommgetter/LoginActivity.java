package com.release.appiumcommgetter;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static com.release.appiumcommgetter.Global.mPassword;
import static com.release.appiumcommgetter.Global.mServerList;
import static com.release.appiumcommgetter.Global.mToken;
import static com.release.appiumcommgetter.Global.mUrlGetServerList;
import static com.release.appiumcommgetter.Global.mUrlPinValidation;
import static com.release.appiumcommgetter.Global.mUserId;
import static com.release.appiumcommgetter.Global.m_BASE_URL;
import static com.release.appiumcommgetter.Global.m_LoginAct;
import static com.release.appiumcommgetter.Global.m_bFirstTime;
import static com.release.appiumcommgetter.Global.m_tempPhone1;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spRegion;
    Button btnLogin;
    EditText etUserId;
    EditText etPassword;
    String _szPIN;
    boolean _isInit = false;
    TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        m_LoginAct = this;
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);

        if (!sharedPref.contains("REGION")) {

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("REGION", "BPAY Production");
            editor.commit();

            Global.REGION_ID = "BPAY Production";
        }
        else
            Global.REGION_ID = sharedPref.getString("REGION", "BPAY Production");

        if (!sharedPref.contains("FirstTime")){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("FirstTime", true);
            editor.commit();
            m_bFirstTime = true;
        }
        else
            m_bFirstTime = sharedPref.getBoolean("FirstTime", true);

        etUserId = (EditText) findViewById(R.id.etUserId);
        etPassword= (EditText) findViewById(R.id.etPassword);
        tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvVersion.setText("Version " + BuildConfig.VERSION_NAME);

        spRegion = (Spinner) findViewById(R.id.spRegion);
        spRegion.setOnItemSelectedListener(this);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login();
            }
        });

        //btnLogin.setEnabled(false);
        spRegion.setEnabled(false);

        OpenURLCloudList();
    }

    private void Login()
    {
        if (etUserId.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "UserId can not be empty",
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (etPassword.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Password can not be empty",
                    Toast.LENGTH_LONG).show();
            return;
        }

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
                    jsonParam.put("username", etUserId.getText().toString());
                    mUserId = etUserId.getText().toString();
                    jsonParam.put("password", etPassword.getText().toString());
                    mPassword = etPassword.getText().toString();
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
                            m_tempPhone1 = jsonData.getString("phonenumber");
                            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("TOKEN", mToken);
                            editor.commit();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (m_bFirstTime) {
                                        //DialogPhone1 dlg = new DialogPhone1(m_LoginAct);
                                        //dlg.setCanceledOnTouchOutside(false);
                                        //dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        //    @Override
                                        //    public void onDismiss(DialogInterface dialogInterface) {
                                                DialogPhone2 dlg2 = new DialogPhone2(m_LoginAct);
                                                dlg2.setCanceledOnTouchOutside(false);
                                                dlg2.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                    @Override
                                                    public void onDismiss(DialogInterface dialogInterface) {
                                                        Global.mSending = false;
                                                        Global.mFailCount = 0;
                                                        Global.mNeedToRestart = 0;
                                                        StartMainActivity();
                                                    }
                                                });
                                                dlg2.show();
                                        //    }
                                        //});
                                        //dlg.show();
                                    }
                                    else {
                                        Global.mSending = false;
                                        Global.mFailCount = 0;
                                        Global.mNeedToRestart = 0;
                                        StartMainActivity();
                                    }
                                }
                            });
                        }
                        else
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),szMessage,
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

    public void StartMainActivity()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "Successfully Login",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        _szPIN = "";
        String item = adapterView.getItemAtPosition(i).toString();
        Log.i(Global.TAG, "Selected Region = " + item);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        alert.setMessage("Please input pin to connect");
        alert.setTitle("PIN REGION " + item);
        alert.setView(edittext);

        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                _szPIN = edittext.getText().toString();

                if (_szPIN.equals(""))
                    return;

                // Send pin and region id to backend for authorization
                (new Thread() {
                    public void run() {
                        try {
                            URL url = new URL(Global.mUrlPinValidation);
                            Log.i(Global.TAG, mUrlPinValidation);

                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setDoInput(true);
                            connection.setDoOutput(true);
                            connection.setUseCaches(false);
                            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            connection.setRequestProperty("Accept", "application/json");
                            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("serverid", mServerList.getJSONObject(i).getString("serverid"));
                            jsonParam.put("pin", _szPIN);
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
                                if (szMessage.equals("MATCH"))
                                {
                                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("REGION", item);
                                    editor.commit();

                                    Global.REGION_ID = item;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),szMessage,
                                                    Toast.LENGTH_LONG).show();
                                            btnLogin.setEnabled(true);
                                        }
                                    });

                                    try {
                                        Global.m_BASE_URL = mServerList.getJSONObject(i).getString("url");
                                        Global.m_URL_POST = m_BASE_URL + "/sms/add_notification.php";
                                        Global.m_URL_HEARTHBEAT_POST = m_BASE_URL + "/appium/comgetter_heartbeat.php";
                                        Global.m_URL_SMS_RESEND = m_BASE_URL + "/sms/resend.php";
                                        Global.m_URL_AUTH = m_BASE_URL + "/appium/auth/comgetter_login.php";
                                        Global.m_MQTT_HOST = mServerList.getJSONObject(i).getString("mqttHost");
                                        Global.m_MQTT_PORT = mServerList.getJSONObject(i).getString("port");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),szMessage,
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
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                _szPIN ="";
            }
        });

        if (!_isInit) {
            //btnLogin.setEnabled(false);
            //alert.show();
        }

        _isInit = false;
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Global.TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("REGION", item);
        editor.commit();
        Global.REGION_ID = item;

        try {
            Global.m_BASE_URL = mServerList.getJSONObject(i).getString("url");
            Global.m_URL_POST = m_BASE_URL + "/sms/add_notification.php";
            Global.m_URL_HEARTHBEAT_POST = m_BASE_URL + "/appium/comgetter_heartbeat.php";
            Global.m_URL_SMS_RESEND = m_BASE_URL + "/sms/resend.php";
            Global.m_URL_AUTH = m_BASE_URL + "/appium/auth/comgetter_login.php";
            Global.m_MQTT_HOST = mServerList.getJSONObject(i).getString("mqttHost");
            Global.m_MQTT_PORT = mServerList.getJSONObject(i).getString("port");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //CheckAndSetBasedOnRegion(false);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void SetSavedRegion(int iSelected)
    {
        spRegion.setSelection(iSelected);
    }

    private void CheckAndSetBasedOnRegion(boolean isInit) {
        _isInit = isInit;

        if (Global.REGION_ID.equals("MY"))
        {
            spRegion.setSelection(0);
            Global.m_URL_HEARTHBEAT_POST = Global.URL_MY_HEARTBEAT_POST;
            Global.m_URL_POST = Global.URL_MY_POST;
            Global.m_URL_SMS_RESEND = Global.URL_MY_SMS_RESEND;
            Global.m_BASE_URL = Global.URL_MY_BASE;
            Global.m_URL_AUTH = Global.URL_MY_AUTH;
        }

        if (Global.REGION_ID.equals("PPAY"))
        {
            spRegion.setSelection(1);
            Global.m_URL_HEARTHBEAT_POST = Global.URL_BP2_HEARTBEAT_POST;
            Global.m_URL_POST = Global.URL_BP2_POST;
            Global.m_URL_SMS_RESEND = Global.URL_BP2_SMS_RESEND;
            Global.m_BASE_URL = Global.URL_BP2_BASE;
            Global.m_URL_AUTH = Global.URL_BP2_AUTH;
        }

        if (Global.REGION_ID.equals("DP")) {
            spRegion.setSelection(2);
            Global.m_URL_HEARTHBEAT_POST = Global.URL_TH_HEARTBEAT_POST;
            Global.m_URL_POST = Global.URL_TH_POST;
            Global.m_URL_SMS_RESEND = Global.URL_TH_SMS_RESEND;
            Global.m_BASE_URL = Global.URL_TH_BASE;
            Global.m_URL_AUTH = Global.URL_TH_AUTH;
        }

        if (Global.REGION_ID.equals("DEV"))
        {
            spRegion.setSelection(3);
            Global.m_URL_HEARTHBEAT_POST = Global.URL_DEV_HEARTBEAT_POST;
            Global.m_URL_POST = Global.URL_DEV_POST;
            Global.m_URL_SMS_RESEND = Global.URL_DEV_SMS_RESEND;
            Global.m_BASE_URL = Global.URL_DEV_BASE;
            Global.m_URL_AUTH = Global.URL_DEV_AUTH;
        }

        if (Global.REGION_ID.equals("SISPAYDEV"))
        {
            spRegion.setSelection(4);
            Global.m_URL_HEARTHBEAT_POST = Global.URL_DEVSISPAY_HEARTBEAT_POST;
            Global.m_URL_POST = Global.URL_DEVSISPAY_POST;
            Global.m_URL_SMS_RESEND = Global.URL_DEVSISPAY_SMS_RESEND;
            Global.m_BASE_URL = Global.URL_DEVSISPAY_BASE;
            Global.m_URL_AUTH = Global.URL_DEVSISPAY_AUTH;
        }

        if (Global.REGION_ID.equals("SISPAYLIVE"))
        {
            spRegion.setSelection(5);
            Global.m_URL_HEARTHBEAT_POST = Global.URL_SISPAY_HEARTBEAT_POST;
            Global.m_URL_POST = Global.URL_SISPAY_POST;
            Global.m_URL_SMS_RESEND = Global.URL_SISPAY_SMS_RESEND;
            Global.m_BASE_URL = Global.URL_SISPAY_BASE;
            Global.m_URL_AUTH = Global.URL_SISPAY_AUTH;
        }
    }

    private void OpenURLCloudList ()
    {
        (new Thread() {
            public void run() {
                try {
                    URL url = new URL(Global.mUrlGetServerList);
                    Log.i(Global.TAG, mUrlGetServerList);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                    JSONObject jsonParam = new JSONObject();
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
                        if (szStatus.equals("ok"))
                        {
                            Global.mServerList = jsonResponse.getJSONArray("records");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    List<String> regions = new ArrayList<String>();
                                    int iSelectedIndex = 0;
                                    for (int i = 0; i < Global.mServerList.length(); i++) {
                                        try {

                                            String szServerName =Global.mServerList.getJSONObject(i).getString("alias");
                                            regions.add(szServerName);
                                            if (szServerName.equals(Global.REGION_ID)) {
                                                iSelectedIndex = i;
                                                _isInit = true;
                                                btnLogin.setEnabled(true);
                                                Global.m_BASE_URL = mServerList.getJSONObject(i).getString("url");
                                                Global.m_URL_POST = m_BASE_URL + "/sms/add_notification.php";
                                                Global.m_URL_HEARTHBEAT_POST = m_BASE_URL + "/appium/comgetter_heartbeat.php";
                                                Global.m_URL_SMS_RESEND = m_BASE_URL + "/sms/resend.php";
                                                Global.m_URL_AUTH = m_BASE_URL + "/appium/auth/comgetter_login.php";
                                                Global.m_MQTT_HOST = mServerList.getJSONObject(i).getString("mqttHost");
                                                Global.m_MQTT_PORT = mServerList.getJSONObject(i).getString("port");
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    ArrayAdapter<String> dataAdapterRegion = new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, regions);
                                    dataAdapterRegion.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                                    spRegion.setAdapter(dataAdapterRegion);
                                    spRegion.setEnabled(true);
                                    SetSavedRegion(iSelectedIndex);
                                    //CheckAndSetBasedOnRegion(false);
                                }
                            });
                        }
                        else
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),szMessage,
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
}
