package com.release.appiumcommgetter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseSMS  extends SQLiteOpenHelper {
    public static final String db_name = "DB_SMS";
    public static final int db_version = 5;

    public DatabaseSMS(Context context) {
        super(context, db_name, null, db_version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String szQuery = "CREATE TABLE SMS (" +
                "id TEXT PRIMARY KEY, " +
                "sn TEXT, " +
                "fromNumber TEXT, " +
                "body TEXT, " +
                "phoneNumber TEXT, " +
                "sentFlag INTEGER, " +
                "serviceCenter TEXT, " +
                "dateTime TEXT)";
        sqLiteDatabase.execSQL(szQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS SMS");
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(String tableName, Object objData) throws JSONException {
        JSONObject joData = (JSONObject) objData;
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contantValues = new ContentValues();
        contantValues.put("id",joData.getString("ID"));
        contantValues.put("sn", joData.getString("SN"));
        contantValues.put("fromNumber", joData.getString("From"));
        contantValues.put("body", joData.getString("Body"));
        contantValues.put("phoneNumber", joData.getString("phoneNumber"));
        contantValues.put("sentFlag", joData.getInt("sentFlag"));
        contantValues.put("serviceCenter", joData.getString("serviceCenter"));
        contantValues.put("dateTime", joData.getString("dateTime"));
        db.insert(tableName, null, contantValues);
        db.close();
        return true;
    }

    public boolean updateData(String tableName, Object objData) throws JSONException {
        JSONObject joData = (JSONObject) objData;
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contantValues = new ContentValues();
        contantValues.put("sn", joData.getString("SN"));
        contantValues.put("fromNumber", joData.getString("From"));
        contantValues.put("body", joData.getString("Body"));
        contantValues.put("phoneNumber", joData.getString("phoneNumber"));
        contantValues.put("sentFlag", joData.getInt("sentFlag"));
        contantValues.put("serviceCenter", joData.getString("serviceCenter"));
        db.update(tableName, contantValues, "id = ?", new String[]{joData.getString("ID")});
        db.close();
        return true;
    }

    public boolean updateDataSentFlag(String tableName, String id, int iSentFlag) throws JSONException {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contantValues = new ContentValues();
        contantValues.put("sentFlag", iSentFlag);
        db.update(tableName, contantValues, "id = ?", new String[]{id});
        db.close();
        return true;
    }

    public Object getFailedSMS(String tableName) throws JSONException {
        JSONArray jaResult = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, sn, fromNumber, body, phoneNumber, sentFlag, serviceCenter, dateTime FROM "+ tableName + " WHERE sentFlag = ?", new String[] { Integer.toString(0) });
        if (c.moveToFirst()){
            do {
                // Passing values
                JSONObject joResult = new JSONObject();
                joResult.put("ID", c.getString(0));
                joResult.put("SN", c.getString(1));
                joResult.put("From", c.getString(2));
                joResult.put("Body", c.getString(3));
                joResult.put("phoneNumber", c.getString(4));
                joResult.put("sentFlag", c.getInt(5));
                joResult.put("serviceCenter", c.getString(6));
                joResult.put("dateTime", c.getString(7));
                // Do something Here with values
                jaResult.put(joResult);
            } while(c.moveToNext());
        }
        c.close();
        db.close();
        return jaResult;
    }

    public Object getLastSMS(String tableName, int iTop) throws JSONException {
        JSONArray jaResult = new JSONArray();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, sn, fromNumber, body, phoneNumber, sentFlag, serviceCenter, dateTime FROM "+ tableName + " ORDER BY CAST(id AS INTEGER) DESC LIMIT " + iTop, null );
        if (c.moveToFirst()){
            do {
                // Passing values
                JSONObject joResult = new JSONObject();
                joResult.put("ID", c.getString(0));
                joResult.put("SN", c.getString(1));
                joResult.put("From", c.getString(2));
                joResult.put("Body", c.getString(3));
                joResult.put("phoneNumber", c.getString(4));
                joResult.put("sentFlag", c.getInt(5));
                joResult.put("serviceCenter", c.getString(6));
                joResult.put("dateTime", c.getString(7));
                // Do something Here with values
                jaResult.put(joResult);
            } while(c.moveToNext());
        }
        c.close();
        db.close();
        return jaResult;
    }

    public Object getSMSbyId(String tableName, String szId) throws JSONException {
        JSONObject joResult = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery("SELECT *FROM " + tableName + " WHERE ID = '" + szId + "'" , null);
        if (c.moveToFirst())
        {
            joResult = new JSONObject();
            joResult.put("ID", c.getString(0));
            joResult.put("SN", c.getString(1));
            joResult.put("From", c.getString(2));
            joResult.put("Body", c.getString(3));
            joResult.put("phoneNumber", c.getString(4));
            joResult.put("sentFlag", c.getInt(5));
            joResult.put("serviceCenter", c.getString(6));
        }
        c.close();
        db.close();
        return joResult;
    }
}
