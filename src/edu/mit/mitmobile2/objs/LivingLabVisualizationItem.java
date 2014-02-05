package edu.mit.mitmobile2.objs;

import java.io.Serializable;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;

public class LivingLabVisualizationItem implements Serializable {

	private String mTitle;
	private String mKey;
	
	public LivingLabVisualizationItem(String title, String key) {
		assert(title != null && key != null);
		setTitle(title);
		setKey(key);
	}
	
	public LivingLabVisualizationItem(JSONObject viewJson) {
		this(viewJson.optString("title"), viewJson.optString("key"));		
	}
	
	public LivingLabVisualizationItem(Bundle bundle) {
		this(bundle.getString("title"), bundle.getString("key"));
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

	public Bundle getAsBundle() {
		Bundle bundle = new Bundle();
		bundle.putString("title", getTitle());
		bundle.putString("key", getKey());
		return bundle;
	}
}
