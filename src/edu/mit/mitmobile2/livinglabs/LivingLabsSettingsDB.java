package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.mit.mitmobile2.objs.LivingLabSettingItem;

public class LivingLabsSettingsDB {
	
	private static final String TAG = "LivingLabsSettingsDB";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "livinglabssettings.db";
	private static final String LIVINGLABS_SETTINGS_TABLE = "livinglabs_settings";
	
	// settings table field names
	private static final String APP_ID = "app_id";
	private static final String LAB_ID = "lab_id";
	private static final String SERVICE_ID = "service_id";
	
	private static final String ENABLED = "enabled";
	
	private static final String ACTIVITY_PROBE = "activity_probe";
	private static final String SMS_PROBE = "sms_probe";
	private static final String CALL_LOG_PROBE = "call_log_probe";
	private static final String BLUETOOTH_PROBE = "bluetooth_probe";
	private static final String WIFI_PROBE = "wifi_probe";
	private static final String SIMPLE_LOCATION_PROBE = "simple_location_probe";
	private static final String SCREEN_PROBE = "screen_probe";
	private static final String RUNNING_APPLICATIONS_PROBE = "running_applications_probe";
	private static final String HARDWARE_INFO_PROBE = "hardware_info_probe";
	private static final String APP_USAGE_PROBE = "app_usage";
	
	private static final String EXISTS_WHERE_CLAUSE = "app_id = ? AND lab_id = ? AND service_id = ?";
	private static final String APP_ID_WHERE = APP_ID + "=?";
	
	SQLiteOpenHelper mLLSettingsDBHelper;
	
	private static LivingLabsSettingsDB llsettingsDBInstance = null;
	
	/********************************************************************/
	public static LivingLabsSettingsDB getInstance(Context context) {
		if(llsettingsDBInstance == null) {
			llsettingsDBInstance = new LivingLabsSettingsDB(context);
			return llsettingsDBInstance;
		} else {
			return llsettingsDBInstance;
		}
	}
	
	public void close() {
		mLLSettingsDBHelper.close();
	}
	
	private LivingLabsSettingsDB(Context context) {
		mLLSettingsDBHelper = new LLSettingsDatabaseHelper(context); 
	}
	
	private String[] whereArgs(LivingLabSettingItem llsItem) {
		String app_id = (String) llsItem.getAppId();
		String lab_id = (String) llsItem.getLabId();
		String service_id = (String) llsItem.getServiceId();
		String[] args = new String[3];
		args[0] = app_id;
		args[1] = lab_id;
		args[2] = service_id;
		return args;
	}
	/********************************************************************/
	synchronized void clearAll() {
		SQLiteDatabase db = mLLSettingsDBHelper.getWritableDatabase();
		db.delete(LIVINGLABS_SETTINGS_TABLE, null, null);
	}
	
	synchronized void delete(LivingLabSettingItem llsi) {
		SQLiteDatabase db = mLLSettingsDBHelper.getWritableDatabase();
		int result = db.delete(LIVINGLABS_SETTINGS_TABLE, APP_ID_WHERE, whereArgs(llsi));

		db.close();
		mLLSettingsDBHelper.close();
	}
	/********************************************************************/
	void startTransaction() {
		mLLSettingsDBHelper.getWritableDatabase().beginTransaction();
	}
	
	void endTransaction() {
		mLLSettingsDBHelper.getWritableDatabase().setTransactionSuccessful();
		mLLSettingsDBHelper.getWritableDatabase().endTransaction();
	}
	/********************************************************************/
	synchronized void saveLivingLabSettingItem(LivingLabSettingItem llsi) {
		
		SQLiteDatabase db = mLLSettingsDBHelper.getWritableDatabase();
		
		ContentValues llsValues = new ContentValues();
		
		llsValues.put(APP_ID, (String)llsi.getAppId());
		llsValues.put(LAB_ID, (String)llsi.getLabId());
		llsValues.put(SERVICE_ID, (String)llsi.getServiceId());
		llsValues.put(ENABLED, (Integer) llsi.getEnabled());
		llsValues.put(ACTIVITY_PROBE, (Integer) llsi.getActivityProbe());
		llsValues.put(SMS_PROBE, (Integer) llsi.getSMSProbe());
		llsValues.put(CALL_LOG_PROBE, (Integer) llsi.getCallLogProbe());
		llsValues.put(BLUETOOTH_PROBE, (Integer) llsi.getBluetoothProbe());
		llsValues.put(WIFI_PROBE, (Integer) llsi.getWifiProbe());
		llsValues.put(SIMPLE_LOCATION_PROBE, (Integer) llsi.getSimpleLocationProbe());
		llsValues.put(SCREEN_PROBE, (Integer) llsi.getScreenProbe());
		llsValues.put(RUNNING_APPLICATIONS_PROBE, (Integer) llsi.getRunningApplicationsProbe());
		llsValues.put(HARDWARE_INFO_PROBE, (Integer) llsi.getHardwareInfoProbe());
		llsValues.put(APP_USAGE_PROBE, (Integer) llsi.getAppUsageProbe());
		
		long row_id;
		int rows;
		ArrayList<String> ids = new ArrayList<String>();
		ids.add((String)llsi.getAppId());
		ids.add((String)llsi.getLabId());
		ids.add((String)llsi.getServiceId());
		if(llsiExists(ids)) {
			rows = db.update(LIVINGLABS_SETTINGS_TABLE, llsValues, EXISTS_WHERE_CLAUSE, whereArgs(llsi));
		} else {
			row_id = db.insert(LIVINGLABS_SETTINGS_TABLE, ENABLED, llsValues);
			llsi.sql_id = row_id;
			rows = db.update(LIVINGLABS_SETTINGS_TABLE, llsValues, EXISTS_WHERE_CLAUSE, whereArgs(llsi));
		}
		db.close();
		mLLSettingsDBHelper.close();
		
	}	
	/********************************************************************/
	public Cursor getMapsCursor(ArrayList<String> searchDataInput) {
		return getMapsCursor(searchDataInput, null);
	}
	
	public Cursor getMapsCursor(ArrayList<String> searchDataInput, String limit) {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		String[] fields = new String[] {APP_ID, LAB_ID, SERVICE_ID, ENABLED, ACTIVITY_PROBE, SMS_PROBE, 
				CALL_LOG_PROBE, BLUETOOTH_PROBE, WIFI_PROBE, SIMPLE_LOCATION_PROBE, SCREEN_PROBE, RUNNING_APPLICATIONS_PROBE, HARDWARE_INFO_PROBE, APP_USAGE_PROBE};
		
		String[] searchData = searchDataInput.toArray(new String[searchDataInput.size()]);
		
		Cursor cursor = db.query(LIVINGLABS_SETTINGS_TABLE, fields, EXISTS_WHERE_CLAUSE, searchData, null, null, APP_ID + " DESC", limit);
		return cursor;
	}

	public Cursor getMapsCursor() {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		String[] fields = new String[] {APP_ID, LAB_ID, SERVICE_ID, ENABLED, ACTIVITY_PROBE, SMS_PROBE, 
				CALL_LOG_PROBE, BLUETOOTH_PROBE, WIFI_PROBE, SIMPLE_LOCATION_PROBE, SCREEN_PROBE, RUNNING_APPLICATIONS_PROBE, HARDWARE_INFO_PROBE, APP_USAGE_PROBE};
		
		Cursor cursor = db.query(LIVINGLABS_SETTINGS_TABLE, fields, null, null, null, null, APP_ID + " DESC", null);
		return cursor;
	}
	
	/********************************************************************/
	static LivingLabSettingItem retrieveLivingLabSettingItem(Cursor cursor) {
		LivingLabSettingItem llsi = new LivingLabSettingItem();
		
		llsi.setAppId(cursor.getString(cursor.getColumnIndex(APP_ID)));
		llsi.setLabId(cursor.getString(cursor.getColumnIndex(LAB_ID)));
		llsi.setServiceId(cursor.getString(cursor.getColumnIndex(SERVICE_ID)));
		
		llsi.setEnabled(cursor.getInt(cursor.getColumnIndex(ENABLED)));
		
		llsi.setActivityProbe(cursor.getInt(cursor.getColumnIndex(ACTIVITY_PROBE)));
		llsi.setSMSProbe(cursor.getInt(cursor.getColumnIndex(SMS_PROBE)));
		llsi.setCallLogProbe(cursor.getInt(cursor.getColumnIndex(CALL_LOG_PROBE)));
		llsi.setBluetoothProbe(cursor.getInt(cursor.getColumnIndex(BLUETOOTH_PROBE)));
		llsi.setWifiProbe(cursor.getInt(cursor.getColumnIndex(WIFI_PROBE)));
		llsi.setSimpleLocationProbe(cursor.getInt(cursor.getColumnIndex(SIMPLE_LOCATION_PROBE)));
		llsi.setScreenProbe(cursor.getInt(cursor.getColumnIndex(SCREEN_PROBE)));
		llsi.setRunningApplicationsProbe(cursor.getInt(cursor.getColumnIndex(RUNNING_APPLICATIONS_PROBE)));
		llsi.setHardwareInfoProbe(cursor.getInt(cursor.getColumnIndex(HARDWARE_INFO_PROBE)));
		llsi.setAppUsageProbe(cursor.getInt(cursor.getColumnIndex(APP_USAGE_PROBE)));		

		return llsi;
	}
	/********************************************************************/
	
	public ArrayList<LivingLabSettingItem> retrieveLivingLabSettingItem() {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
	
		Cursor cursor = db.query(LIVINGLABS_SETTINGS_TABLE, null, null, null, null, null, null);
		
		if (cursor.getCount()<1) return null;
		
		cursor.moveToFirst();
		
		ArrayList<LivingLabSettingItem> llsiArray = new ArrayList<LivingLabSettingItem>();
		
		for(int i=0; i<cursor.getCount(); i++){
			llsiArray.add(retrieveLivingLabSettingItem(cursor));
			cursor.moveToNext();
		}
		
		cursor.close();
		mLLSettingsDBHelper.close();
		
		return llsiArray;
	}
	
	public LivingLabSettingItem retrieveLivingLabSettingItem(ArrayList<String> searchDataInput) {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		
		String[] searchData = searchDataInput.toArray(new String[searchDataInput.size()]);
		
		Cursor cursor = db.query(LIVINGLABS_SETTINGS_TABLE, null, EXISTS_WHERE_CLAUSE, searchData, null, null, null);
		
		if (cursor.getCount()<1) return null;
		
		cursor.moveToFirst();
		LivingLabSettingItem llsi = retrieveLivingLabSettingItem(cursor);
		cursor.close();
		mLLSettingsDBHelper.close();
		
		return llsi;
	}
	
	/********************************************************************/
	public boolean llsiExists(ArrayList<String> searchDataInput) {
		
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		
		String[] searchData = searchDataInput.toArray(new String[searchDataInput.size()]);
		
		Cursor result = db.query(
			LIVINGLABS_SETTINGS_TABLE, 
			new String[] {APP_ID, LAB_ID, SERVICE_ID}, 
			EXISTS_WHERE_CLAUSE,
			searchData,
			null, null, null);
	
		boolean llsiExists = (result.getCount() > 0);
		result.close();
		return llsiExists;
		
	}
	
	public int isServiceEnabled(ArrayList<String> searchDataInput) {
		
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		
		String[] searchData = searchDataInput.toArray(new String[searchDataInput.size()]);
		
		Cursor result = db.query(
			LIVINGLABS_SETTINGS_TABLE, 
			new String[] {ENABLED}, 
			EXISTS_WHERE_CLAUSE,
			searchData,
			null, null, null);

		result.close();
		return result.getInt(0);
		
	}	

	public int isProbeEnabled(ArrayList<String> searchDataInput) {
		
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		
		String[] searchData = searchDataInput.toArray(new String[searchDataInput.size()]);
		Cursor result = db.query(
			LIVINGLABS_SETTINGS_TABLE, 
			new String[] {searchDataInput.get(searchDataInput.size()-1)}, 
			EXISTS_WHERE_CLAUSE,
			searchData,
			null, null, null);

		result.close();
		return result.getInt(0);
		
	}	
	/********************************************************************/
	private static class LLSettingsDatabaseHelper extends SQLiteOpenHelper {
		
		//make private?
		private LLSettingsDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			
			db.execSQL("CREATE TABLE IF NOT EXISTS" + LIVINGLABS_SETTINGS_TABLE + " ("
					+ APP_ID + " TEXT, "
					+ LAB_ID + " TEXT, "
					+ SERVICE_ID + " TEXT, "
					+ ENABLED + " INTEGER, " 
					+ ACTIVITY_PROBE + " INTEGER, "
					+ SMS_PROBE + " INTEGER, "
					+ CALL_LOG_PROBE + " INTEGER, "
					+ BLUETOOTH_PROBE + " INTEGER, "
					+ WIFI_PROBE + " INTEGER, "
					+ SIMPLE_LOCATION_PROBE + " INTEGER, "
					+ SCREEN_PROBE + " INTEGER, "
					+ RUNNING_APPLICATIONS_PROBE + " INTEGER, "
					+ HARDWARE_INFO_PROBE + " INTEGER, "
					+ APP_USAGE_PROBE + " INTEGER, "					
					
					+ " PRIMARY KEY ("
					+ APP_ID + ", "
					+ LAB_ID + ", "
					+ SERVICE_ID + ")"
				+ ");");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// no old versions exists
		}
	}
	public SQLiteDatabase getWritableDatabase() {
		// TODO Auto-generated method stub
		return null;
	}

}