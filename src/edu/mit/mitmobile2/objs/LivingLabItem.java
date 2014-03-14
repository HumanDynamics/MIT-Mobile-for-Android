package edu.mit.mitmobile2.objs;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LivingLabItem implements Serializable {
	private static final long serialVersionUID = -7377069315139664175L;
	private String mName;
	private ArrayList<LivingLabVisualizationItem> mVisualizations;
	
	public LivingLabItem(JSONObject labJson) throws JSONException {
		assert(labJson != null && labJson.has("name") && labJson.has("visualizations"));
		mName = labJson.optString("name");
		mVisualizations = new ArrayList<LivingLabVisualizationItem>();
		JSONArray viewsJsonVisualization = labJson.optJSONArray("visualizations");
		for (int i = 0; i < viewsJsonVisualization.length(); i++) {
			mVisualizations.add(new LivingLabVisualizationItem(viewsJsonVisualization.optJSONObject(i)));
		}
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}
	
	public ArrayList<LivingLabVisualizationItem> getVisualizations() {
		return mVisualizations;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
