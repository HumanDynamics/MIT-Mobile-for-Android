package edu.mit.mitmobile2.objs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.media.funf.probe.builtin.ActivityProbe;
import edu.mit.media.funf.probe.builtin.BluetoothProbe;
import edu.mit.media.funf.probe.builtin.CallLogProbe;
import edu.mit.media.funf.probe.builtin.HardwareInfoProbe;
import edu.mit.media.funf.probe.builtin.RunningApplicationsProbe;
import edu.mit.media.funf.probe.builtin.ScreenProbe;
import edu.mit.media.funf.probe.builtin.SimpleLocationProbe;
import edu.mit.media.funf.probe.builtin.SmsProbe;
import edu.mit.media.funf.probe.builtin.WifiProbe;

public class LivingLabSettingItem implements Serializable {
	private static final long serialVersionUID = -7377069315139664175L; //??
	public long sql_id = -1;  // not to confuse with any other "id"
	private String mAppId, mLabId;
	private int mActivityProbe, mSMSProbe, mCallLogProbe, 
		mBluetoothProbe, mWifiProbe, mSimpleLocationProbe, mScreenProbe, mRunningApplicationsProbe, mHardwareInfoProbe, mAppUsageProbe;
	private HashSet<String> mEnabledProbes;

	private String mSettingsContextLabel;
	private static Map<String, Class> PROBE_MAPPING;
	
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
	
	private static String probeNameFromColumn(String column) {
		StringBuilder stringBuilder = new StringBuilder();
		
		for (int i = 0; i < column.length(); i++) {
			char c = column.charAt(i);
			if (c == '_' && i + 1 < column.length()) {
				i++;
				stringBuilder.append(Character.toUpperCase(column.charAt(i)));
			} else {
				stringBuilder.append(column.charAt(i));
			}
		}
		
		return stringBuilder.toString();
	}
	
	
	protected HashMap<String,Object> itemData;
	
	public LivingLabSettingItem() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public LivingLabSettingItem(JSONObject labSettingJson) throws JSONException {
		assert(labSettingJson != null && labSettingJson.has("app_id") && labSettingJson.has("lab_id"));
		mAppId = labSettingJson.optString("app_id");
		mLabId = labSettingJson.optString("lab_id");
		mEnabledProbes = new HashSet<String>();
		
		mActivityProbe = labSettingJson.optInt("activity_probe");
		mSMSProbe = labSettingJson.optInt("sms_probe");
		mCallLogProbe = labSettingJson.optInt("call_log_probe");
		mBluetoothProbe = labSettingJson.optInt("bluetooth_probe");
		mWifiProbe = labSettingJson.optInt("wifi_probe");
		mSimpleLocationProbe = labSettingJson.optInt("simple_location_probe");
		mScreenProbe = labSettingJson.optInt("screen_probe");
		mRunningApplicationsProbe = labSettingJson.optInt("running_applications_probe");
		mHardwareInfoProbe = labSettingJson.optInt("hardware_info_probe");
		mAppUsageProbe = labSettingJson.optInt("app_usage_probe");
		
		Iterator<String> keysIterator = labSettingJson.keys();
		
		while (keysIterator.hasNext()) {
			String key = keysIterator.next();
			
			if (PROBE_MAPPING.containsKey(key) && labSettingJson.optInt(key) == 1) {
				mEnabledProbes.add(PROBE_MAPPING.get(key).getCanonicalName());
			}
		}
		
		mSettingsContextLabel = labSettingJson.optString("settings_context_label");
	}

	public HashSet<String> getEnabledProbes() {
		return mEnabledProbes;
	}

	
	public HashMap<String,Object> getItemData() {
		return itemData;
	}

	public void setItemData(HashMap<String,Object> itemData) {
		this.itemData = itemData;
	}
	
	public String getAppId() {
		return mAppId;
	}

	public void setAppId(String mAppId) {
		this.mAppId = mAppId;
	}
	
	public String getLabId() {
		return mLabId;
	}

	public void setLabId(String mLabId) {
		this.mLabId = mLabId;
	}
	
	public int getActivityProbe(){
		return mActivityProbe;
	}
	
	public void setActivityProbe(int mActivityProbe){
		this.mActivityProbe = mActivityProbe;
	}
	
	public int getSMSProbe(){
		return mSMSProbe;
	}
	
	public void setSMSProbe(int mSMSProbe){
		this.mSMSProbe = mSMSProbe;
	}
	
	public int getCallLogProbe(){
		return mCallLogProbe;
	}
	
	public void setCallLogProbe(int mCallLogProbe){
		this.mCallLogProbe = mCallLogProbe;
	}
	
	public int getBluetoothProbe(){
		return mBluetoothProbe;
	}
	
	public void setBluetoothProbe(int mBluetoothProbe){
		this.mBluetoothProbe = mBluetoothProbe;
	}
	
	public int getWifiProbe(){
		return mWifiProbe;
	}
	
	public void setWifiProbe(int mWifiProbe){
		this.mWifiProbe = mWifiProbe;
	}
	
	public int getSimpleLocationProbe(){
		return mSimpleLocationProbe;
	}
	
	public void setSimpleLocationProbe(int mSimpleLocationProbe){
		this.mSimpleLocationProbe = mSimpleLocationProbe;
	}
	
	public int getScreenProbe(){
		return mScreenProbe;
	}
	
	public void setScreenProbe(int mScreenProbe){
		this.mScreenProbe = mScreenProbe;
	}
	
	public void setRunningApplicationsProbe(int mRunningApplicationsProbe){
		this.mRunningApplicationsProbe = mRunningApplicationsProbe;
	}
	
	public int getRunningApplicationsProbe(){
		return mRunningApplicationsProbe;
	}
	
	public void setHardwareInfoProbe(int mHardwareInfoProbe){
		this.mHardwareInfoProbe = mHardwareInfoProbe;
	}
	
	public int getHardwareInfoProbe(){
		return mHardwareInfoProbe;
	}
	
	public void setAppUsageProbe(int mAppUsageProbe){
		this.mAppUsageProbe = mAppUsageProbe;
	}
	
	public int getAppUsageProbe(){
		return mAppUsageProbe;
	}
	
	public String getSettingsContextLabel() {
		return mSettingsContextLabel;
	}

	public void setSettingsContextLabel(String mSettingsContextLabel) {
		this.mSettingsContextLabel = mSettingsContextLabel;
	}
	
	@Override
	public String toString() {
		return getAppId();
	}
}
