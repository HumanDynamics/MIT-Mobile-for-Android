package edu.mit.mitmobile2.objs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LivingLabVisualizationItem implements Serializable {

	private static final String TAG = "LLVisualizationItem";
	/**
	 * 
	 */
	private static final long serialVersionUID = -9008681946449251965L;
	
	private String mTitle;
	private String mKey;
	private List<LivingLabDataItem> mAnswerItems;
	
	public LivingLabVisualizationItem(String title, String key, JSONArray answers) throws Exception {
		assert(title != null && key != null && answers != null);
		setTitle(title);
		setKey(key);
		mAnswerItems = new ArrayList<LivingLabDataItem>();
		setAnswerItems(answers);
	}
	
	public LivingLabVisualizationItem(JSONObject viewJson) throws Exception {
		this(viewJson.optString("title"), viewJson.optString("key"), viewJson.optJSONArray("answers"));	
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
	
	public JSONObject getData() throws JSONException {
		JSONObject dataJson = new JSONObject();
		Set<LivingLabDataItem> probes = new HashSet<LivingLabDataItem>();
		
		for (LivingLabDataItem dataItem : getAnswerItems()) {
			Set <LivingLabDataItem> probeCollection = dataItem.getProbes();
			for(LivingLabDataItem probe : probeCollection){
				probe.setPurposes(dataItem.getPurposes());
			}
			probes.addAll(probeCollection);
			//probes.addAll(dataItem.getProbes());
		}
		
		try {
			dataJson.put("required", new JSONArray());
			dataJson.put("non-required", new JSONArray());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (LivingLabDataItem dataItem : probes) {
			if (dataItem.isRequired()) {
				// NOTE: The key is either human readable, or must be mapped to work with the current system
				try {
					//dataJson.accumulate("required", dataItem.getKey());
					dataJson.accumulate("required", dataItem);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					//dataJson.accumulate("non-required", dataItem.getKey());
					dataJson.accumulate("non-required", dataItem);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		return dataJson;
	}
	
	public List<LivingLabDataItem> getAnswerItems() {
		return mAnswerItems;
	}

	public void setAnswerItems(JSONArray answersJson) throws Exception {
		mAnswerItems.clear();
		
		for (int i = 0; i < answersJson.length(); i++) {
			JSONObject answerJson = answersJson.optJSONObject(i);
			if (answerJson != null) {
				mAnswerItems.add(LivingLabDataItemFactory.INSTANCE.getDataItem(answerJson));
			} else {
				mAnswerItems.add(LivingLabDataItemFactory.INSTANCE.getDataItem(answersJson.optString(i)));
			}
		}
	}
}
