package edu.mit.mitmobile2.objs;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class LivingLabContextItem implements Serializable {
	private static final long serialVersionUID = -7377069315139664175L; //??
	public long sql_id = -1;  // not to confuse with any other "id"
	private String mContextLabel, mContextDurationStart, mContextDurationEnd, mContextDurationDays, mContextPlaces;
	protected HashMap<String,Object> itemData;
	
	public LivingLabContextItem() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public LivingLabContextItem(JSONObject labContextJson) throws JSONException {
		mContextLabel = labContextJson.optString("context_label");
		mContextDurationStart = labContextJson.optString("context_duration_start");
		mContextDurationEnd = labContextJson.optString("context_duration_end");
		mContextDurationDays = labContextJson.optString("context_duration_days");
		mContextPlaces = labContextJson.optString("context_places");
	}
	
	
	public HashMap<String,Object> getItemData() {
		return itemData;
	}

	public void setItemData(HashMap<String,Object> itemData) {
		this.itemData = itemData;
	}
	
	public String getContextLabel() {
		return mContextLabel;
	}

	public void setContextLabel(String mContextLabel) {
		this.mContextLabel = mContextLabel;
	}
	
	public String getContextDurationStart() {
		return mContextDurationStart;
	}

	public void setContextDurationStart(String mContextDurationStart) {
		this.mContextDurationStart = mContextDurationStart;
	}
	
	public String getContextDurationEnd() {
		return mContextDurationEnd;
	}

	public void setContextDurationEnd(String mContextDurationEnd) {
		this.mContextDurationEnd = mContextDurationEnd;
	}
	
	public String getContextDurationDays() {
		return mContextDurationDays;
	}

	public void setContextDurationDays(String mContextDurationDays) {
		this.mContextDurationDays = mContextDurationDays;
	}
	
	public String getContextPlaces() {
		return mContextPlaces;
	}

	public void setContextPlaces(String mContextPlaces) {
		this.mContextPlaces = mContextPlaces;
	}
}
