package edu.mit.mitmobile2.objs;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class LivingLabSettingItem implements Serializable {
	private static final long serialVersionUID = -7377069315139664175L; //??
	public long sql_id = -1;  // not to confuse with any other "id"
	private String mAppId, mLabId, mServiceId;
	private int mEnabled, mActivityProbe, mSMSProbe, mCallLogProbe, 
		mBluetoothProbe, mWifiProbe, mSimpleLocationProbe, mScreenProbe, mRunningApplicationsProbe, mHardwareInfoProbe, mAppUsageProbe;
	protected HashMap<String,Object> itemData;
	
	public LivingLabSettingItem() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public LivingLabSettingItem(JSONObject labSettingJson) throws JSONException {
		assert(labSettingJson != null && labSettingJson.has("app_id") && labSettingJson.has("lab_id") && labSettingJson.has("service_id"));
		mAppId = labSettingJson.optString("app_id");
		mLabId = labSettingJson.optString("lab_id");
		mServiceId = labSettingJson.optString("service_id");
		
		mEnabled = labSettingJson.optInt("enabled");
		
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
	
	public String getServiceId() {
		return mServiceId;
	}

	public void setServiceId(String mServiceId) {
		this.mServiceId = mServiceId;
	}
	
	public int getEnabled(){
		return mEnabled;
	}
	
	public void setEnabled(int mEnabled){
		this.mEnabled = mEnabled;
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
	
	@Override
	public String toString() {
		return getServiceId();
	}
}
