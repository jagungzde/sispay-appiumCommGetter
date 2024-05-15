package com.release.appiumcommgetter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Created by yulie on 03/08/2018.
 */

public class BroadcastMsgReceiver extends BroadcastReceiver {

    public static String MAIN_SERVICE = "com.release.smsreader.MainService";
    public static String MAIN_ACT = "com.release.smsreader.MainActivity";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("restartService"))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, MainService.class).setAction("restartService"));
            } else {
                context.startService(new Intent(context, MainService.class).setAction("restartService"));
            }
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        /*else
        if (intent.getAction().equals(MAIN_ACT)) {
            Log.i(Global.TAG, "Message from activity");

            String szAction = intent.getStringExtra("Action");

            if (szAction.equals("Start")) {
                Intent i = new Intent(context, MainService.class);
                i.putExtra("Action", "Start");
                context.startService(i);
            }
        }
        else
        if (intent.getAction().equals(MAIN_SERVICE)) {
            Log.i(Global.TAG, "Message from service");

            if (Global.m_MainAct == null)
                return;

            String szMode = intent.getStringExtra("Action");

            if (szMode.equals("Init"))
                Global.m_MainAct.StartInit();

            if (szMode.equals("SendSMS"))
            {
                Bundle b = intent.getBundleExtra("SendSMS");
                String szSMSData = b.getString("SMSData");
                Global.m_MainAct.SendSMS(szSMSData);
            }
        }
        */
        else {
            /*Bundle b = intent.getExtras();
            if (b != null) {
                Log.i(Global.TAG, "smsReceiver : Reading Bundle");

                Object[] pdus = (Object[]) b.get("pdus");
                for (Object pdu : pdus) {
                    Log.d(Global.TAG, "legacy SMS implementation (before KitKat)");
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    if (sms == null) {
                        Log.e(Global.TAG, "SMS message is null -- ABORT");
                        break;
                    }
                    String originatingaddr = sms.getDisplayOriginatingAddress();
                    Log.i(Global.TAG, originatingaddr);
                    String message = sms.getMessageBody();
                    if ( Global.m_MainService != null)
                        Global.m_MainService.PutThreadHandle(1, new Object[] { message, originatingaddr });
                }
                */

                //SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[0]);
                //String message = sms.getMessageBody();
            //}
        }
    }
}
