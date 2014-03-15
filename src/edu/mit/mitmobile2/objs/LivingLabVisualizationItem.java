package edu.mit.mitmobile2.objs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import android.util.Log;

public class LivingLabVisualizationItem implements Serializable {

	private static final String TAG = "LLVisualizationctivity";
	
	private String mTitle;
	private String mKey;
	private String mData;
	
	public LivingLabVisualizationItem(String title, String key, String data) {
		assert(title != null && key != null && data != null);
		setTitle(title);
		setKey(key);
		setData(data);
	}
	
	public LivingLabVisualizationItem(JSONObject viewJson) throws JSONException {
		this(viewJson.optString("title"), viewJson.optString("key"), viewJson.optJSONObject("data").toString());	
	}
	
	public LivingLabVisualizationItem(Bundle bundle) {
		this(bundle.getString("title"), bundle.getString("key"), bundle.getString("data"));
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		this.mTitle = title;
	}

	public String getKey() {
		return mKey;
	}

	public void setKey(String key) {
		this.mKey = key;
	}
	
	public String getData(){
		return mData;
	}
	
	public void setData(String data){
		this.mData = data;
	}

	public Bundle getAsBundle() {
		Bundle bundle = new Bundle();
		bundle.putString("title", getTitle());
		bundle.putString("key", getKey());
		bundle.putString("data", getData());
		return bundle;
	}
}
