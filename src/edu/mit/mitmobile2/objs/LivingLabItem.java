package edu.mit.mitmobile2.objs;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class LivingLabItem implements Serializable {
	private static final long serialVersionUID = -7377069315139664175L;
	private String mName;
	private ArrayList<LivingLabVisualizationItem> mVisualizations;
	private ArrayList<LivingLabDataItem> mAnswerItems;
	
	public LivingLabItem(JSONObject labJson) {
		assert(labJson != null && labJson.has("name") && labJson.has("visualizations"));
		mName = labJson.optString("name");
		mVisualizations = new ArrayList<LivingLabVisualizationItem>();
		mAnswerItems = new ArrayList<LivingLabDataItem>();
		JSONArray answersJson = labJson.optJSONArray("answers");
		mAnswerItems = new ArrayList<LivingLabDataItem>(); 
		
		if (answersJson != null) {
			mAnswerItems.addAll(LivingLabDataItemFactory.INSTANCE.getDataItems(answersJson));
		}
		
		JSONArray viewsJson = labJson.optJSONArray("visualizations");
		for (int i = 0; i < viewsJson.length(); i++) {
			mVisualizations.add(new LivingLabVisualizationItem(viewsJson.optJSONObject(i)));
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

	public ArrayList<LivingLabDataItem> getAnswerItems() {
		return mAnswerItems;
	}

	public void setAnswerItems(ArrayList<LivingLabDataItem> answerItems) {
		this.mAnswerItems = answerItems;
	}
}
