package edu.mit.mitmobile2.objs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

public class LivingLabDataItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1425677475272105802L;
	private String mKey;
	private boolean mRequired;
	private LivingLabDataItemType mType;
	private List<LivingLabDataItem> mDependencies;
	
	
	protected LivingLabDataItem(JSONObject dataJson) {
		assert(dataJson != null && dataJson.has("key"));
		setKey(dataJson.optString("key"));
		setRequired(dataJson.has("required")? dataJson.optBoolean("required") : true);
		// We're setting dependencies to empty for now as we want to fill it in with the actual answers
		setDependencies(new ArrayList<LivingLabDataItem>());
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