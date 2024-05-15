package com.release.appiumcommgetter;

import static com.release.appiumcommgetter.Global.TAG;
import static com.release.appiumcommgetter.Global.m_MainAct;
import static com.release.appiumcommgetter.Global.m_PhoneNumber;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DialogCheckingSMS extends Dialog {

    JSONObject _joData;

    public DialogCheckingSMS(@NonNull Context context, Object objData) {
        super(context);
        _joData = (JSONObject) objData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_checking_sms);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CheckSMSInbox();
            }
        }, 5000);

    }

    private void CheckSMSInbox()
    {
        try {
            JSONArray jaData = m_MainAct.GetSMSInboxData(_joData.getInt("sms_id"));
            for (int i = 0; i < jaData.length(); i++)
            {
                try {
                    JSONObject joData = jaData.getJSONObject(i);
                    int iPhoneSlot = _joData.getInt("phoneSlot");
                    if (iPhoneSlot == 1)
                        if (joData.getString("Body").equals(m_PhoneNumber)) {
                            Log.i(TAG, "Phone Number Input Correctly");
                            SharedPreferences sharedPref = m_MainAct.getSharedPreferences(Global.TAG, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("PHONE_NUMBER", m_PhoneNumber);
                            editor.commit();
                            break;
                        }
                        else
                        {
                            Toast.makeText(m_MainAct, "No Phone Number can be used, application will be closed.",
                                    Toast.LENGTH_LONG).show();
                            Thread.sleep(3000);
                            m_MainAct.finishAffinity();
                        }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            dismiss();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
