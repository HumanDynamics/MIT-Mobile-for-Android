package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

import edu.mit.media.funf.probe.builtin.ActivityProbe;
import edu.mit.media.funf.probe.builtin.BluetoothProbe;
import edu.mit.media.funf.probe.builtin.CallLogProbe;
import edu.mit.media.funf.probe.builtin.HardwareInfoProbe;
import edu.mit.media.funf.probe.builtin.RunningApplicationsProbe;
import edu.mit.media.funf.probe.builtin.ScreenProbe;
import edu.mit.media.funf.probe.builtin.SimpleLocationProbe;
import edu.mit.media.funf.probe.builtin.SmsProbe;
import edu.mit.media.funf.probe.builtin.WifiProbe;
import edu.mit.media.openpds.client.PreferencesWrapper;
import edu.mit.mitmobile2.objs.LivingLabContextItem;
import edu.mit.mitmobile2.objs.LivingLabSettingItem;

public class LivingLabsAccessControlDB {
	
	/*
	 * vs 0: settings, context and syncing with the server
	 * vs 1: probes table
	 * 
	 */
	
	public static Map<String, Class> PROBE_MAPPING;
	
	static {
		PROBE_MAPPING = new HashMap<String, Class>();
		PROBE_MAPPING.put("activity_probe", ActivityProbe.class);
		PROBE_MAPPING.put("sms_probe", SmsProbe.class);
		PROBE_MAPPING.put("call_log_probe", CallLogProbe.class);
		PROBE_MAPPING.put("bluetooth_probe", BluetoothProbe.class);
		PROBE_MAPPING.put("wifi_probe", WifiProbe.class);
		PROBE_MAPPING.put("simple_location_probe", SimpleLocationProbe.class);
		PROBE_MAPPING.put("screen_probe", ScreenProbe.class);
		PROBE_MAPPING.put("running_applications_probe", RunningApplicationsProbe.class);
		PROBE_MAPPING.put("hardware_info_probe", HardwareInfoProbe.class);
	}
	
	private static final String TAG = "LivingLabsAccessControlDB";
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "livinglabsaccesscontrol.db";
	private static final String LIVINGLABS_SETTINGS_TABLE = "livinglabs_settings";
	private static final String LIVINGLABS_CONTEXT_TABLE = "livinglabs_context";
	private static final String LIVINGLABS_PROBES_TABLE = "livinglabs_probes";
	
	// settings table field names
	private static final String APP_ID = "app_id";
	private static final String LAB_ID = "lab_id";

	private static final String ACTIVITY_PROBE = "activity_probe";
	private static final String SMS_PROBE = "sms_probe";
	private static final String CALL_LOG_PROBE = "call_log_probe";
	private static final String BLUETOOTH_PROBE = "bluetooth_probe";
	private static final String WIFI_PROBE = "wifi_probe";
	private static final String SIMPLE_LOCATION_PROBE = "simple_location_probe";
	private static final String SCREEN_PROBE = "screen_probe";
	private static final String RUNNING_APPLICATIONS_PROBE = "running_applications_probe";
	private static final String HARDWARE_INFO_PROBE = "hardware_info_probe";
	private static final String APP_USAGE_PROBE = "app_usage_probe";
	
	private static final String SETTINGS_CONTEXT_LABEL = "settings_context_label";
	
	private static final String EXISTS_WHERE_CLAUSE = "app_id = ? AND lab_id = ?";
	private static final String APP_ID_WHERE = APP_ID + "=?";
	
	private static final String CONTEXT_LABEL = "context_label";
	private static final String CONTEXT_DURATION_START = "context_duration_start";
	private static final String CONTEXT_DURATION_END = "context_duration_end";
	private static final String CONTEXT_DURATION_DAYS = "context_duration_days";
	private static final String CONTEXT_PLACES = "context_places";
	
	private static final String CONTEXT_LABEL_WHERE = CONTEXT_LABEL + " = ?";

	static SQLiteOpenHelper mLLSettingsDBHelper;
	
	private static LivingLabsAccessControlDB llsettingsDBInstance = null;
	
	private static JSONObject loadParams, probesFromServer;
	private static LivingLabFunfPDS pds;
	private static Connection connection;
	private static Context context;
	
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
		this.context = context;
	}
	
	private static String[] whereArgsSettings(LivingLabSettingItem llsItem) {
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
	synchronized static void saveLivingLabSettingItem(LivingLabSettingItem llsi) {
		
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
	
	synchronized static JSONObject saveLivingLabProbeItem(String global_type) throws JSONException {
		
		ContentValues llpiValues = new ContentValues();
		JSONObject llpiValuesReturn = new JSONObject();
		
		String[] probes = {"activity_probe", "sms_probe", "call_log_probe", "bluetooth_probe", "wifi_probe", "simple_location_probe",
				"screen_probe", "running_applications_probe", "hardware_info_probe", "app_usage_probe"};
		if(global_type == null){
			ArrayList<String> searchDataInput = new ArrayList<String>();
			searchDataInput.add("Living Lab");
			searchDataInput.add("all");
			
			LivingLabSettingItem llsi = retrieveLivingLabSettingItem(searchDataInput);
			
			llpiValues.put(probes[0], llsi.getActivityProbe());
			llpiValues.put(probes[1], llsi.getSMSProbe());
			llpiValues.put(probes[2], llsi.getCallLogProbe());
			llpiValues.put(probes[3], llsi.getBluetoothProbe());
			llpiValues.put(probes[4], llsi.getWifiProbe());
			llpiValues.put(probes[5], llsi.getSimpleLocationProbe());
			llpiValues.put(probes[6], llsi.getScreenProbe());
			llpiValues.put(probes[7], llsi.getRunningApplicationsProbe());
			llpiValues.put(probes[8], llsi.getHardwareInfoProbe());
			llpiValues.put(probes[9], llsi.getAppUsageProbe());
			
			
			llpiValuesReturn.put(probes[0], llsi.getActivityProbe());
			llpiValuesReturn.put(probes[1], llsi.getSMSProbe());
			llpiValuesReturn.put(probes[2], llsi.getCallLogProbe());
			llpiValuesReturn.put(probes[3], llsi.getBluetoothProbe());
			llpiValuesReturn.put(probes[4], llsi.getWifiProbe());
			llpiValuesReturn.put(probes[5], llsi.getSimpleLocationProbe());
			llpiValuesReturn.put(probes[6], llsi.getScreenProbe());
			llpiValuesReturn.put(probes[7], llsi.getRunningApplicationsProbe());
			llpiValuesReturn.put(probes[8], llsi.getHardwareInfoProbe());
			llpiValuesReturn.put(probes[9], llsi.getAppUsageProbe());			
			
		} else if(Boolean.valueOf(global_type)){
			for(int i=0; i<probes.length; i++){
				llpiValues.put(probes[i], 1);
				
				llpiValuesReturn.put(probes[i], 1);
			}
		} else if(!Boolean.valueOf(global_type)){
			for(int i=0; i<probes.length; i++){
				llpiValues.put(probes[i], 0);
				
				llpiValuesReturn.put(probes[i], 0);
			}
		}
		
		SQLiteDatabase db = mLLSettingsDBHelper.getWritableDatabase();

		
		long row_id;
		int rows;
		row_id = db.insert(LIVINGLABS_PROBES_TABLE, null, llpiValues);
		//llpi.sql_id = row_id;
		rows = db.update(LIVINGLABS_PROBES_TABLE, llpiValues, null, null);
		db.close();
		mLLSettingsDBHelper.close();
		
		
		
		return llpiValuesReturn;
		
	}	
	
	synchronized static JSONObject retrieveLivingLabProbeItem(String global_type) throws JSONException {
		JSONObject llpiValuesReturn = new JSONObject();
		
		String[] probes = {"activity_probe", "sms_probe", "call_log_probe", "bluetooth_probe", "wifi_probe", "simple_location_probe",
				"screen_probe", "running_applications_probe", "hardware_info_probe", "app_usage_probe"};
		if(global_type == null){
			ArrayList<String> searchDataInput = new ArrayList<String>();
			searchDataInput.add("Living Lab");
			searchDataInput.add("all");
			
			LivingLabSettingItem llsi = retrieveLivingLabSettingItem(searchDataInput);
			
			
			llpiValuesReturn.put(probes[0], llsi.getActivityProbe());
			llpiValuesReturn.put(probes[1], llsi.getSMSProbe());
			llpiValuesReturn.put(probes[2], llsi.getCallLogProbe());
			llpiValuesReturn.put(probes[3], llsi.getBluetoothProbe());
			llpiValuesReturn.put(probes[4], llsi.getWifiProbe());
			llpiValuesReturn.put(probes[5], llsi.getSimpleLocationProbe());
			llpiValuesReturn.put(probes[6], llsi.getScreenProbe());
			llpiValuesReturn.put(probes[7], llsi.getRunningApplicationsProbe());
			llpiValuesReturn.put(probes[8], llsi.getHardwareInfoProbe());
			llpiValuesReturn.put(probes[9], llsi.getAppUsageProbe());			
			
		}
		
		return llpiValuesReturn;
		
	}	
	
	synchronized static void loadLivingLabProbeItem(JSONObject llpi) throws JSONException {

		SQLiteDatabase db = mLLSettingsDBHelper.getWritableDatabase();

		ContentValues llpiValues = new ContentValues();


		String[] probes = {"activity_probe", "sms_probe", "call_log_probe", "bluetooth_probe", "wifi_probe", "simple_location_probe",
				"screen_probe", "running_applications_probe", "hardware_info_probe", "app_usage_probe"};

		for(int i=0; i<probes.length; i++){
			int value = 0;
			if(llpi.has(probes[i])){
				Object probeValue = llpi.get(probes[i]);
				if(probeValue instanceof Integer) {
					value = (Integer) probeValue;
				} else if(probeValue instanceof Boolean){
					//if(llpi.getBoolean(probes[i]))
					if((Boolean) probeValue)
						value = 1;
				}
			}
			llpiValues.put(probes[i], value);
		}

		long row_id;
		int rows;
		row_id = db.insert(LIVINGLABS_PROBES_TABLE, null, llpiValues);
		//llpi.sql_id = row_id;
		rows = db.update(LIVINGLABS_PROBES_TABLE, llpiValues, null, null);
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
	
	public Cursor getMapsCursorProbes() {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
		String[] fields = new String[] {ACTIVITY_PROBE, SMS_PROBE, 
				CALL_LOG_PROBE, BLUETOOTH_PROBE, WIFI_PROBE, SIMPLE_LOCATION_PROBE, SCREEN_PROBE, RUNNING_APPLICATIONS_PROBE, HARDWARE_INFO_PROBE, APP_USAGE_PROBE};
		
		Cursor cursor = db.query(LIVINGLABS_PROBES_TABLE, fields, null, null, null, null, null, null);
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
	
	static JSONObject retrieveLivingLabProbesItem(Cursor cursor) throws JSONException {
		JSONObject llpObject = new JSONObject();
		
		llpObject.put("activity_probe", cursor.getInt(cursor.getColumnIndex(ACTIVITY_PROBE)));
		llpObject.put("sms_probe", cursor.getInt(cursor.getColumnIndex(SMS_PROBE)));
		llpObject.put("call_log_probe", cursor.getInt(cursor.getColumnIndex(CALL_LOG_PROBE)));
		llpObject.put("bluetooth_probe", cursor.getInt(cursor.getColumnIndex(BLUETOOTH_PROBE)));
		llpObject.put("wifi_probe", cursor.getInt(cursor.getColumnIndex(WIFI_PROBE)));
		llpObject.put("simple_location_probe", cursor.getInt(cursor.getColumnIndex(SIMPLE_LOCATION_PROBE)));
		llpObject.put("screen_probe", cursor.getInt(cursor.getColumnIndex(SCREEN_PROBE)));
		llpObject.put("running_applications_probe", cursor.getInt(cursor.getColumnIndex(RUNNING_APPLICATIONS_PROBE)));
		llpObject.put("hardware_info_probe", cursor.getInt(cursor.getColumnIndex(HARDWARE_INFO_PROBE)));
		llpObject.put("app_usage_probe", cursor.getInt(cursor.getColumnIndex(APP_USAGE_PROBE)));	
		
		return llpObject;
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
	
	public static JSONObject retrieveLivingLabProbeItem() throws JSONException {
		SQLiteDatabase db = mLLSettingsDBHelper.getReadableDatabase();
	
		Cursor cursor = db.query(LIVINGLABS_PROBES_TABLE, null, null, null, null, null, null);
		
		if (cursor.getCount()<1) return null;
		
		cursor.moveToFirst();
		
		JSONObject llpObject = new JSONObject();

		llpObject = retrieveLivingLabProbesItem(cursor);
		
		cursor.close();
		mLLSettingsDBHelper.close();
		
		return llpObject;
	}
	
	public static LivingLabSettingItem retrieveLivingLabSettingItem(ArrayList<String> searchDataInput) throws JSONException {
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
	public static boolean llsiExists(ArrayList<String> searchDataInput) {
		
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
		
		private Context context;
		//make private?
		private LLSettingsDatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			
			
			db.execSQL("DROP TABLE IF EXISTS " + LIVINGLABS_SETTINGS_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + LIVINGLABS_CONTEXT_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + LIVINGLABS_PROBES_TABLE);
			
			
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
			
			probesTableHandling(db);
			
			

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			
			if(newVersion == 2){
				probesTableHandling(db);
			}
		}
		
	}
	public SQLiteDatabase getWritableDatabase() {
		return null;
	}
	private static void probesTableHandling(SQLiteDatabase db){
		db.execSQL("CREATE TABLE IF NOT EXISTS " + LIVINGLABS_PROBES_TABLE + " ("
				+ ACTIVITY_PROBE + " INTEGER, "
				+ SMS_PROBE + " INTEGER, "
				+ CALL_LOG_PROBE + " INTEGER, "
				+ BLUETOOTH_PROBE + " INTEGER, "
				+ WIFI_PROBE + " INTEGER, "
				+ SIMPLE_LOCATION_PROBE + " INTEGER, "
				+ SCREEN_PROBE + " INTEGER, "
				+ RUNNING_APPLICATIONS_PROBE + " INTEGER, "
				+ HARDWARE_INFO_PROBE + " INTEGER, "
				+ APP_USAGE_PROBE + " INTEGER "				
				+ ");");
		
		db.execSQL("INSERT INTO " + LIVINGLABS_PROBES_TABLE + " ("
				+ ACTIVITY_PROBE + ", "
				+ SMS_PROBE + ", "
				+ CALL_LOG_PROBE + ", "
				+ BLUETOOTH_PROBE + ", "
				+ WIFI_PROBE + ", "
				+ SIMPLE_LOCATION_PROBE + ", "
				+ SCREEN_PROBE + ", "
				+ RUNNING_APPLICATIONS_PROBE + ", "
				+ HARDWARE_INFO_PROBE + ", "
				+ APP_USAGE_PROBE
				+ ") VALUES ("
				+ "0,0,0,0,0,0,0,0,0,0"
				+ ");");
				
		loadParams = new JSONObject();
		try {
			pds = new LivingLabFunfPDS(context);
			connection = new Connection(context);
			connection.execute(loadParams).get(3000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static class Connection extends AsyncTask<JSONObject, Object, Object> {
		 
		private Context mContext;
		
        @Override
        protected Object doInBackground(JSONObject... object) {
        	try {
        		
        		
	    		PreferencesWrapper prefs = new PreferencesWrapper(mContext);
	    		String uuid = prefs.getUUID();
	    		loadParams.put("datastore_owner", uuid); 
	    		JSONObject result = new JSONObject(pds.accessControlData(loadParams, "load"));
	    		
	    		Log.v(TAG, result.toString());
	    		probesFromServer = (JSONObject) result.getJSONObject("probes");
	    		
	    		loadLivingLabProbeItem(probesFromServer); //not really similar to settings

			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }

        public Connection(Context context) {
            this.mContext = context;
        }
 
    }

}