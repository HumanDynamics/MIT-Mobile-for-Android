package edu.mit.mitmobile2.objs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LivingLabDataItemFactory {
	
	private static final String TAG = "LLDataItemFactory";
	public static LivingLabDataItemFactory INSTANCE = new LivingLabDataItemFactory();
	
	private Map<String, List<LivingLabDataItem>> mCachedDependencies;
	private Map<String, List<String>> mCachedPurposes;
	
	private LivingLabDataItemFactory() {
		mCachedDependencies = new HashMap<String, List<LivingLabDataItem>>();
		mCachedPurposes = new HashMap<String, List<String>>();
	}
	
	public List<LivingLabDataItem> getDataItems(JSONArray itemsJson) throws Exception {
		List<LivingLabDataItem> dataItems = new ArrayList<LivingLabDataItem>();
		
		for (int i = 0; i < itemsJson.length(); i++) {
			JSONObject itemJson = itemsJson.optJSONObject(i);
			if (itemJson != null) {
				dataItems.add(getDataItem(itemJson));
			} else {
				dataItems.add(getDataItem(itemsJson.optString(i)));
			}
		}
		
		return dataItems;
	}
	
	public LivingLabDataItem getDataItem(JSONObject dataJson) throws Exception {
		String key = dataJson.optString("key");
		
		if (!mCachedDependencies.containsKey(key)) {
			mCachedDependencies.put(key, new ArrayList<LivingLabDataItem>());			
		}
		
		if (!mCachedPurposes.containsKey(key)) {
			mCachedPurposes.put(key, new ArrayList<String>());			
		}

		List<LivingLabDataItem> dependencies = mCachedDependencies.get(key);
		List<String> purposes = mCachedPurposes.get(key);
		
		// Warning: a hack, but maybe there isn't a better way to check for if something's a probe?

		LivingLabDataItem dataItem = key.endsWith("Probe")? new LivingLabDataItem(dataJson) : new LivingLabAnswerItem(dataJson);
		//mCachedItems.put(key, dataItem);
		

		
		
		JSONArray dependenciesJson = dataJson.optJSONArray("data");
		
		if (dependenciesJson != null && dependenciesJson.length() != dependencies.size()) {
			// Implies we've loaded up an answer that has been previously instantiated (as a dependency), but not fully declared
			// Maybe we should be adding the dependencies, rather than clearing out the old ones?
			dependencies.clear();
			List<LivingLabDataItem> dependenciesList = getDataItems(dependenciesJson);
			dependencies.addAll(dependenciesList);
		}
		
		JSONArray purposesJson = dataJson.optJSONArray("purpose");
		if (purposesJson != null) {
			purposes = getPurposeItems(purposesJson);
		}
		
		dataItem.setDependencies(dependencies);
		dataItem.setPurposes(purposes);
		
		mCachedPurposes.put(key, purposes);	
		
		return dataItem;
	}
	
	public LivingLabDataItem getDataItem(String dataKey) throws Exception {
		JSONObject dataJson = new JSONObject();
		try {
			dataJson.putOpt("key", dataKey);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getDataItem(dataJson);
	}
	
	public List<String> getPurposeItems(JSONArray itemsJson) {
		List<String> purposeItems = new ArrayList<String>();
		for (int i = 0; i < itemsJson.length(); i++) {
			String itemJson = itemsJson.optString(i);
			purposeItems.add(itemJson);
		}
		
		return purposeItems;
	}
}
