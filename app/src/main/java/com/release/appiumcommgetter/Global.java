package com.release.appiumcommgetter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yulie on 03/08/2018.
 */

public class Global {

    public static String TAG = "SMSReader";

    public static String mUrlGetServerList = "https://test123domain.com/api/getServerListAppium.php";
    public static String mUrlPinValidation = "https://test123domain.com/api/pinValidationAppium.php";
    public static JSONArray mServerList = null;

    public static String m_URL_POST = "";
    public static String m_URL_HEARTHBEAT_POST = "";
    public static String m_URL_FORGETPASSWORD_POST = "";
    public static String m_URL_SMS_RESEND = "";
    public static String m_BASE_URL = "";
    public static String m_URL_AUTH = "";
    public static String m_MQTT_HOST = "";
    public static String m_MQTT_PORT = "";

    public static String URL_DEV_BASE = "https://test123domain.com";
    public static String URL_DEV_POST = URL_DEV_BASE + "/sms/add_notification.php";
    public static String URL_DEV_HEARTBEAT_POST = URL_DEV_BASE + "/sms/heartbeat.php";
    public static String URL_DEV_SMS_RESEND = URL_DEV_BASE + "/sms/resend.php";
    public static String URL_DEV_AUTH = URL_DEV_BASE + "/sispay/webservices/auth/login.php";

    public static String URL_MY_BASE = "https://www.bpmatchserver.com"; //"https://bpay.cash";
    public static String URL_MY_POST = URL_MY_BASE + "/sms/add_notification.php";
    public static String URL_MY_HEARTBEAT_POST = URL_MY_BASE + "/sms/heartbeat.php";
    public static String URL_MY_SMS_RESEND = URL_MY_BASE + "/sms/resend.php";
    public static String URL_MY_AUTH = URL_MY_BASE + "/sms/auth/login.php";

    public static String URL_BP2_BASE = "http://3.108.7.11";
    public static String URL_BP2_POST = URL_BP2_BASE + "/sms/add_notification.php";
    public static String URL_BP2_HEARTBEAT_POST = URL_BP2_BASE + "/sms/heartbeat.php";
    public static String URL_BP2_SMS_RESEND = URL_BP2_BASE + "/sms/resend.php";
    public static String URL_BP2_AUTH = URL_BP2_BASE + "/sms/auth/login.php";

    public static String URL_TH_BASE = "https://dpmatchserver.com";
    public static String URL_TH_POST = URL_TH_BASE + "/sms/add_notification.php";
    public static String URL_TH_HEARTBEAT_POST = URL_TH_BASE + "/sms/heartbeat.php";
    public static String URL_TH_SMS_RESEND = URL_TH_BASE + "/sms/resend.php";
    public static String URL_TH_AUTH = URL_TH_BASE + "/sms/auth/login.php";

    public static String URL_DEVSISPAY_BASE = "https://test123domain.com";
    public static String URL_DEVSISPAY_POST = URL_DEV_BASE + "/sms/add_notification.php";
    public static String URL_DEVSISPAY_HEARTBEAT_POST = URL_DEV_BASE + "/sms/heartbeat.php";
    public static String URL_DEVSISPAY_SMS_RESEND = URL_DEV_BASE + "/sms/resend.php";
    public static String URL_DEVSISPAY_AUTH = URL_DEVSISPAY_BASE + "/sms/auth/login.php";

    public static String URL_SISPAY_BASE = "http://sispay.tech";
    public static String URL_SISPAY_POST = "http://sispay.tech/sms/add_notification.php";
    public static String URL_SISPAY_HEARTBEAT_POST = "http://sispay.tech/sms/add_notification.php";
    public static String URL_SISPAY_SMS_RESEND = "http://sispay.tech/sms/resend.php";
    public static String URL_SISPAY_AUTH = URL_SISPAY_BASE + "/sms/auth/login.php";

    public static String REGION_ID = "MY";

    public static MainActivity m_MainAct = null;
    public static LoginActivity m_LoginAct = null;
    public static MainService m_MainService = null;

    public static String m_PhoneNumber = "";
    public static String m_EmulatorPhoneNumber = "";
    public static String m_PhoneNumber2 = "";
    public static String m_tempPhone1 = "";
    public static String m_tempPhone2 = "";
    public static boolean m_bFirstTime = false;

    public static String m_PhoneId = "";
    public static int m_iSubId = -1;
    public static int m_MaxSMS = 30;
    public static List<JSONObject> m_dualPhoneNumber = new ArrayList<>();
    public static String m_queryInboxSimId = "sim_id";
    public static int m_iDualSimSender = 0;

    public static LinkedList<JSONObject> m_smsList = new LinkedList<>();

    public static String mToken = "";
    public static String mUserId = "";
    public static String mPassword = "";
    public static boolean mSending = false;
    public static int mFailCount = 0;
    public static int mNeedToRestart = 0;
    public static Calendar mLastCheckTimeStamp = null;
}
