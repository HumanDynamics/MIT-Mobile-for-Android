package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;

import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import edu.mit.mitmobile2.objs.LivingLabContextItem;
import edu.mit.mitmobile2.objs.LivingLabSettingItem;

public class LivingLabsAccessControlDB {
	
	private static final String TAG = "LivingLabsAccessControlDB";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "livinglabsaccesscontrol.db";
	private static final String LIVINGLABS_SETTINGS_TABLE = "livinglabs_settings";
	private static final String LIVINGLABS_CONTEXT_TABLE = "livinglabs_context";
	
	// settings table field names
	private static final String APP_ID = "app_id";
	private static final String LAB_ID = "lab_id";
	//private static final String SERVICE_ID = "service_id";

	
	//private static final String ENABLED = "enabled";
	
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
	
	private static final String SETTINGS_CONTEXT_LABEL = "settings_context_label";
	
	private static final String EXISTS_WHERE_CLAUSE = "app_id = ? AND lab_id = ?";
	private static final String APP_ID_WHERE = APP_ID + "=?";
	
	private static final String CONTEXT_LABEL = "context_label";
	private static final String CONTEXT_DURATION_START = "context_duration_start";
	private static final String CONTEXT_DURATION_END = "context_duration_end";
	private static final String CONTEXT_DURATION_DAYS = "context_duration_days";
	private static final String CONTEXT_PLACES = "context_places";
	
	private static final String CONTEXT_LABEL_WHERE = CONTEXT_LABEL + " = ?";

	SQLiteOpenHelper mLLSettingsDBHelper;
	
	private static LivingLabsAccessControlDB llsettingsDBInstance = null;
	
	/********************************************************************/
	public static LivingLabsAccessControlDB getInstance(Context context) {
		if(llsettingsDBInstance == null) {
			llsettingsDBInstance = new LivingLabsAccessControlDB(context);
			return llsettingsDBInstance;
		} else {
			return llsettingsDBInstance;
		}
	}
	
	public void close() {
		mLLSettingsDBHelper.close();
	}
	
	private LivingLabsAccessControlDB(Context context) {
		mLLSettingsDBHelper = new LLSettingsDatabaseHelper(context); 
	}
	
	private String[] whereArgsSettings(LivingLabSettingItem llsItem) {
		String app_id = (String) llsItem.getAppId();
		String lab_id = (String) llsItem.getLabId();
		String[] args = new String[2];
		args[0] = app_id;
		args[1] = lab_id;
		return args;
	}
	
	private String[] whereArgsContext(LivingLabContextItem llcItem) {
		String context_label = (String) llcItem.getContextLabel();
		String[] args = new String[1];
		args[0] = context_label;
		return args;
	}

	/********************************************************************/
	synchronized void clearAll() {
		SQLiteDatabase db = mLLSettingsDBHelper.getWritableDatabase();
		db.delete(LIVINGLABS_SETTINGS_TABLE, null, null);
		db.delete(LIVINGLABS_CONTEXT_TABLE, null, null);
	}
	
	synchronized void delete(LivingLabSettingItem llsi, LivingLabContextItem llci) {
		SQLiteDatabase db = mLLSettingsDBHelper.getWritableDatabase();
		int result_settings = db.delete(LIVINGLABS_SETTINGS_TABLE, EXISTS_WHERE_CLAUSE, whereArgsSettings(llsi));
		int result_context = db.delete(LIVINGLABS_CONTEXT_TABLE, null, null);

		db.close();
		mLLSettingsDBHelper.close();
	}
	
	synchronized void deleteContextItem(LivingLabContextItem llci) {
		SQLiteDatabase db = mLLSettingsDBHelper.getWritableDatabase();
		int result_context = db.delete(LIVINGLABS_CONTEXT_TABLE, CONTEXT_LABEL_WHERE, whereArgsContext(llci));

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
		llsValues.put(SETTINGS_CONTEXT_LABEL, llsi.getSettingsContextLabel());
		
		long row_id;
		int rows;
		ArrayList<String> ids = new ArrayList<String>();
		ids.add((String)llsi.getAppId());
		ids.add((String)llsi.getLabId());
		if(llsiExists(ids)) {
			rows = db.update(LIVINGLABS_SETTINGS_TABLE, llsValues, EXISTS_WHERE_CLAUSE, whereArgsSettings(llsi));
		} else {
			row_id = db.insert(LIVINGLABS_SETTINGS_TABLE, null, llsValues);
			llsi.sql_id = row_id;
			rows = db.update(LIVINGLABS_SETTINGS_TABLE, llsValues, EXISTS_WHERE_CLAUSE, whereArgsSettings(llsi));
		}
		db.close();
		mLLSettingsDBHelper.close();
		
	}	
	
	synchronized void saveLivingLabContextItem(LivingLabContextItem llci) {
		
		SQLiteDatabase db = mLLSettingsDBHelper.getWritableDatabase();
		
		ContentValues llciValues = new ContentValues();
		
		llciValues.put(CONTEXT_LABEL, llci.getContextLabel());
		llciValues.put(CONTEXT_DURATION_START, llci.getContextDurationStart());
		llciValues.put(CONTEXT_DURATION_END, llci.getContextDurationEnd());
		llciValues.put(CONTEXT_DURATION_DAYS, llci.getContextDurationDays());
		llciValues.put(CONTEXT_PLACES, llci.getContextPlaces());
		
		long row_id;
		int rows;
		ArrayList<String> ids = new ArrayList<String>();
		String context_label = llci.getContextLabel();
		ids.add(context_label);
		if(llciExists(ids)) {
			rows = db.update(LIVINGLABS_CONTEXT_TABLE, llciValues, CONTEXT_LABEL_WHERE, whereArgsContext(llci));
		} else {
			row_id = db.insert(LIVINGLABS_CONTEXT_TABLE, null, llciValues);
			llci.sql_id = row_id;
			rows = db.update(LIVINGLABS_CONTEXT_TABLE, llciValues, CONTEXT_LABEL_WHERE, whereArgsContext(llci));
		}
		db.close();
		mLLSettingsDBHelper.close();
		
	}	
	/********************************************************************/
	public Cursor getMapsCursorSettings(ArrayList<String> searchDataInput) {
		return getMapsCursorSettings(searchDataInput, null);
	}
	
	public Cursor getMapsCursorContet(ArrayList<String> searchDataInput) {
		return getMapsCursorContext(searchDataInput, null);
	}
	
	public Cursor getMapsCursorSettings(ArrayList<String> searchDataInput, String limit) {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		String[] fields = new String[] {APP_ID, LAB_ID, ACTIVITY_PROBE, SMS_PROBE, 
				CALL_LOG_PROBE, BLUETOOTH_PROBE, WIFI_PROBE, SIMPLE_LOCATION_PROBE, SCREEN_PROBE, RUNNING_APPLICATIONS_PROBE, HARDWARE_INFO_PROBE, APP_USAGE_PROBE,
				SETTINGS_CONTEXT_LABEL};
		
		String[] searchData = searchDataInput.toArray(new String[searchDataInput.size()]);
		
		Cursor cursor = db.query(LIVINGLABS_SETTINGS_TABLE, fields, EXISTS_WHERE_CLAUSE, searchData, null, null, APP_ID + " DESC", limit);
		return cursor;
	}
	
	public Cursor getMapsCursorContext(ArrayList<String> searchDataInput, String limit) {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		String[] fields = new String[] {CONTEXT_LABEL, CONTEXT_DURATION_START, CONTEXT_DURATION_END, CONTEXT_DURATION_DAYS, CONTEXT_PLACES};
		
		String[] searchData = searchDataInput.toArray(new String[searchDataInput.size()]);
		
		Cursor cursor = db.query(LIVINGLABS_CONTEXT_TABLE, fields, CONTEXT_LABEL_WHERE, searchData, null, null, null, limit);
		return cursor;
	}

	public Cursor getMapsCursorSettings() {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		String[] fields = new String[] {APP_ID, LAB_ID, ACTIVITY_PROBE, SMS_PROBE, 
				CALL_LOG_PROBE, BLUETOOTH_PROBE, WIFI_PROBE, SIMPLE_LOCATION_PROBE, SCREEN_PROBE, RUNNING_APPLICATIONS_PROBE, HARDWARE_INFO_PROBE, APP_USAGE_PROBE,
				SETTINGS_CONTEXT_LABEL};
		
		Cursor cursor = db.query(LIVINGLABS_SETTINGS_TABLE, fields, null, null, null, null, APP_ID + " DESC", null);
		return cursor;
	}
	
	public Cursor getMapsCursorContext() {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		String[] fields = new String[] {CONTEXT_LABEL, CONTEXT_DURATION_START, CONTEXT_DURATION_END, CONTEXT_DURATION_DAYS, CONTEXT_PLACES};
		
		Cursor cursor = db.query(LIVINGLABS_CONTEXT_TABLE, fields, null, null, null, null, null, null);
		return cursor;
	}
	
	static LivingLabSettingItem retrieveLivingLabSettingItem(Cursor cursor) throws JSONException {
		LivingLabSettingItem llsi = new LivingLabSettingItem();
		
		llsi.setAppId(cursor.getString(cursor.getColumnIndex(APP_ID)));
		llsi.setLabId(cursor.getString(cursor.getColumnIndex(LAB_ID)));
		
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
		
		llsi.setSettingsContextLabel(cursor.getString(cursor.getColumnIndex(SETTINGS_CONTEXT_LABEL)));

		return llsi;
	}
	
	static LivingLabContextItem retrieveLivingLabContextItem(Cursor cursor) {
		LivingLabContextItem llci = new LivingLabContextItem();
		
		llci.setContextLabel(cursor.getString(cursor.getColumnIndex(CONTEXT_LABEL)));
		llci.setContextDurationStart(cursor.getString(cursor.getColumnIndex(CONTEXT_DURATION_START)));
		llci.setContextDurationEnd(cursor.getString(cursor.getColumnIndex(CONTEXT_DURATION_END)));
		llci.setContextDurationDays(cursor.getString(cursor.getColumnIndex(CONTEXT_DURATION_DAYS)));
		llci.setContextPlaces(cursor.getString(cursor.getColumnIndex(CONTEXT_PLACES)));	

		return llci;
	}
	
	public ArrayList<LivingLabSettingItem> retrieveLivingLabSettingItem() throws JSONException {
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
	
	public ArrayList<LivingLabContextItem> retrieveLivingLabContextItem() {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
	
		Cursor cursor = db.query(LIVINGLABS_CONTEXT_TABLE, null, null, null, null, null, null);
		
		if (cursor.getCount()<1) return null;
		
		cursor.moveToFirst();
		
		ArrayList<LivingLabContextItem> llciArray = new ArrayList<LivingLabContextItem>();
		
		for(int i=0; i<cursor.getCount(); i++){
			llciArray.add(retrieveLivingLabContextItem(cursor));
			cursor.moveToNext();
		}
		
		cursor.close();
		mLLSettingsDBHelper.close();
		
		return llciArray;
	}
	
	public LivingLabSettingItem retrieveLivingLabSettingItem(ArrayList<String> searchDataInput) throws JSONException {
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
	
	public LivingLabContextItem retrieveLivingLabContextItem(ArrayList<String> searchDataInput) throws JSONException {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		
		String[] searchData = searchDataInput.toArray(new String[searchDataInput.size()]);
		
		String[] fields = new String[] {CONTEXT_LABEL, CONTEXT_DURATION_START, CONTEXT_DURATION_END, CONTEXT_DURATION_DAYS, CONTEXT_PLACES};
		
		
		Cursor cursor = db.query(LIVINGLABS_CONTEXT_TABLE, fields, CONTEXT_LABEL_WHERE, searchData, null, null, null);
		
		if (cursor.getCount()<1) return null;
		
		cursor.moveToFirst();
		LivingLabContextItem llci = retrieveLivingLabContextItem(cursor);
		cursor.close();
		mLLSettingsDBHelper.close();
		
		return llci;
	}
	
	/********************************************************************/
	public boolean llsiExists(ArrayList<String> searchDataInput) {
		
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		
		String[] searchData = searchDataInput.toArray(new String[searchDataInput.size()]);
		
		Cursor result = db.query(
			LIVINGLABS_SETTINGS_TABLE, 
			new String[] {APP_ID, LAB_ID}, 
			EXISTS_WHERE_CLAUSE,
			searchData,
			null, null, null);
	
		boolean llsiExists = (result.getCount() > 0);
		result.close();
		return llsiExists;
		
	}
	
	public boolean llciExists(ArrayList<String> searchDataInput) {
		
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		
		String[] searchData = searchDataInput.toArray(new String[searchDataInput.size()]);
		
		Cursor result = db.query(
			LIVINGLABS_CONTEXT_TABLE, 
			new String[] {CONTEXT_LABEL}, 
			CONTEXT_LABEL_WHERE,
			searchData,
			null, null, null);
	
		boolean llciExists = (result.getCount() > 0);
		result.close();
		return llciExists;
		
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
			
			
			db.execSQL("DROP TABLE IF EXISTS " + LIVINGLABS_SETTINGS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + LIVINGLABS_CONTEXT_TABLE);
			
			
			db.execSQL("CREATE TABLE IF NOT EXISTS " + LIVINGLABS_SETTINGS_TABLE + " ("
					+ APP_ID + " TEXT, "
					+ LAB_ID + " TEXT, "
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
					+ SETTINGS_CONTEXT_LABEL + " TEXT, "
					
					+ " PRIMARY KEY ("
					+ APP_ID + ", "
					+ LAB_ID + ")"
				+ ");");
			
			db.execSQL("CREATE TABLE IF NOT EXISTS " + LIVINGLABS_CONTEXT_TABLE + " ("
					+ CONTEXT_LABEL + " TEXT, "
					+ CONTEXT_DURATION_START + " TEXT, " 
					+ CONTEXT_DURATION_END + " TEXT, " 
					+ CONTEXT_DURATION_DAYS + " TEXT, " 
					+ CONTEXT_PLACES + " TEXT, " 
					
					+ " PRIMARY KEY ("
					+ CONTEXT_LABEL + ")"					
				+ ");");
			
			db.execSQL("INSERT INTO " + LIVINGLABS_CONTEXT_TABLE + " ("
					+ CONTEXT_LABEL + ", "
					+ CONTEXT_DURATION_START + ", "
					+ CONTEXT_DURATION_END + ", "
					+ CONTEXT_DURATION_DAYS + ", "
					+ CONTEXT_PLACES
					+ ") VALUES ("
					+ "'MIT', '10 : 00', '18 : 00','[0,1,1,1,1,1,0]',''"
					+ ")");
					
			
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