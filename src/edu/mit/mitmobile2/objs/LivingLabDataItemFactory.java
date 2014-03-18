package edu.mit.mitmobile2.objs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LivingLabDataItemFactory {
	
	public static LivingLabDataItemFactory INSTANCE = new LivingLabDataItemFactory();
	
	private Map<String, LivingLabDataItem> mCachedItems;
	
	private LivingLabDataItemFactory() {
		mCachedItems = new HashMap<String, LivingLabDataItem>();
	}
	
	public List<LivingLabDataItem> getDataItems(JSONArray itemsJson) {
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
	
	public LivingLabDataItem getDataItem(JSONObject dataJson) {
		String key = dataJson.optString("key");
		LivingLabDataItem dataItem = null;
		if (key.length() > 0 && mCachedItems.containsKey(key)) {
			dataItem = mCachedItems.get(key);
		} 
		
		if (dataItem == null) {
			// Warning: a hack, but maybe there isn't a better way to check for if something's a probe?
			dataItem = key.endsWith("Probe")? new LivingLabDataItem(dataJson) : new LivingLabAnswerItem(dataJson);
			mCachedItems.put(key, dataItem);
		}
		
		JSONArray dependenciesJson = dataJson.optJSONArray("data");
		
		if (dependenciesJson != null && dependenciesJson.length() != dataItem.getDependencies().size()) {
			// Implies we've loaded up an answer that has been previously instantiated (as a dependency), but not fully declared
			// Maybe we should be adding the dependencies, rather than clearing out the old ones?
			dataItem.setDependencies(getDataItems(dependenciesJson));
		}
		
		return dataItem;
	}
	
	public LivingLabDataItem getDataItem(String dataKey) {
		JSONObject dataJson = new JSONObject();
		try {
			dataJson.putOpt("key", dataKey);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getDataItem(dataJson);
	}
}
