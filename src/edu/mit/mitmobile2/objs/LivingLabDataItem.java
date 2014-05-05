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

public class LivingLabDataItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1425677475272105802L;
	private static final String TAG = "LLDataItem";
	
	private String mKey;
	private boolean mRequired;
	private LivingLabDataItemType mType;
	private List<LivingLabDataItem> mDependencies;
	private List<String> mPurposes;
	
	
	protected LivingLabDataItem(JSONObject dataJson) throws JSONException {
		assert(dataJson != null && dataJson.has("key") && dataJson.has("purpose"));
		setKey(dataJson.optString("key"));
		setRequired(dataJson.has("required")? dataJson.optBoolean("required") : true);
		// We're setting dependencies to empty for now as we want to fill it in with the actual answers
		setDependencies(new ArrayList<LivingLabDataItem>());
		
		JSONArray purposesArray = dataJson.optJSONArray("purpose");
		List<String> purposesList = new ArrayList<String>();
		if(purposesArray != null){
			for(int i=0; i<purposesArray.length(); i++){
				purposesList.add(purposesArray.getString(i));
			}
		}
		setPurposes(purposesList);
	}

	public String getKey() {
		return mKey;
	}

	public void setKey(String mKey) {
		this.mKey = mKey;
	}
	
	public boolean isRequired() {
		return mRequired;
	}


	public void setRequired(boolean mRequired) {
		this.mRequired = mRequired;
	}

	public LivingLabDataItemType getType() {
		return mType;
	}

	public void setType(LivingLabDataItemType mType) {
		this.mType = mType;
	}

	public List<LivingLabDataItem> getDependencies() {
		return mDependencies;
	}

	public void setDependencies(List<LivingLabDataItem> dependencies) {
		this.mDependencies = dependencies;
	}
	
	public List<String> getPurposes() {
		return mPurposes;
	}

	public void setPurposes(List<String> purposes) {
		this.mPurposes = purposes;
	}

	public Set<LivingLabDataItem> getProbes() {
		Set<LivingLabDataItem> probes = new HashSet<LivingLabDataItem>();
		
		if (getDependencies().size() == 0) {
			probes.add(this);
			return probes;
		} else {
			for (LivingLabDataItem dataItem : getDependencies()) {
				probes.addAll(dataItem.getProbes());
			}
		}
		
		return probes;
	}
	
	public enum LivingLabDataItemType {
		PROBE,
		ANSWER
	}
}