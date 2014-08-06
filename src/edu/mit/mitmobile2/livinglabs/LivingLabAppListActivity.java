package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleActivity;
import edu.mit.mitmobile2.livinglabs.R;
import edu.mit.mitmobile2.objs.LivingLabDataItem;
import edu.mit.mitmobile2.objs.LivingLabItem;
import edu.mit.mitmobile2.objs.LivingLabSettingItem;
import edu.mit.mitmobile2.objs.LivingLabVisualizationItem;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class LivingLabAppListActivity extends NewModuleActivity {
	private static final String TAG = "LivingLabAppListActivity";
	private ListView mLivingLabsListView, mLivingLabsGlobalSettingsListView;
	SharedPreferences isFirstVisitOfLab;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		isFirstVisitOfLab = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		setContentView(R.layout.living_labs_list);
		List<LivingLabItem> labs = new ArrayList<LivingLabItem>();
		try {
			String labsString = getString(R.string.living_labs_application_list);
			JSONArray labsJson = new JSONArray(labsString);
			for (int i = 0; i < labsJson.length(); i++) {
				labs.add(new LivingLabItem(labsJson.optJSONObject(i)));
			}
		} catch (JSONException e) {
			Log.e("LivingLabAppListActivity", "Error Constructing Labs List", e);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		LinearLayout layout = new LinearLayout(this);
		
		mLivingLabsListView = (ListView) findViewById(R.id.livingLabsListView);
		mLivingLabsListView.setAdapter(new LivingLabArrayAdapter(this, R.layout.living_lab_row, R.id.livingLabRowTitle, labs));	
		
		View rootView = mLivingLabsListView.getRootView();
	    rootView.setBackgroundColor(getResources().getColor(android.R.color.white));
	    
	    TextView livinglabHeaderTextView = (TextView) findViewById(R.id.livinglabHeaderTextView);
	    livinglabHeaderTextView.setText(Html.fromHtml("<h4>Your Living Labs</h4>"));
	    
	    TextView livinglabGlobalSettingsTextView = (TextView) findViewById(R.id.livinglabGlobalSettingsTextView);
	    livinglabGlobalSettingsTextView.setText(Html.fromHtml("<br/><h4>Global Settings</h4>"));
		
		mLivingLabsListView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				LivingLabItem labItem = (LivingLabItem) listView.getItemAtPosition(position);
				Intent labIntent = null;
				boolean firstVisitOfLabFlag = isFirstVisitOfLab.getBoolean(labItem.getName(), true);
				
				if(firstVisitOfLabFlag){
					SharedPreferences.Editor editor = isFirstVisitOfLab.edit();
				    editor.putBoolean(labItem.getName(), false);
				    editor.commit();
				    
				    labIntent = new Intent(LivingLabAppListActivity.this, LivingLabsWalkthroughActivity.class);
				} else {
					if(labItem.getName().equalsIgnoreCase("settings")){
						labItem = (LivingLabItem) listView.getItemAtPosition(0);
						labIntent = new Intent(LivingLabAppListActivity.this, LivingLabSettingsProbesActivity.class);
					} else {
						labIntent = new Intent(LivingLabAppListActivity.this, LivingLabActivity.class);
					}
				}
				
				labIntent.putExtra("lab", labItem);
				startActivity(labIntent);
			}
		});
		
		try {
			unionProbesFromLabs(labs);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		List<String> global_settings = new ArrayList<String>();
		global_settings.add("Data Collection & Use");
		
		mLivingLabsGlobalSettingsListView = (ListView) findViewById(R.id.livingLabsGlobalSettingsListView);
		mLivingLabsGlobalSettingsListView.setAdapter(new ArrayAdapter(this, R.layout.living_lab_global_settings_row, R.id.livingLabGlobalSettingsRowTitle, global_settings));	

		mLivingLabsGlobalSettingsListView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				String global_setting = listView.getItemAtPosition(position).toString();
				Intent labIntent = null;
				if(global_setting.equalsIgnoreCase("Data Collection & Use")){
					labIntent = new Intent(LivingLabAppListActivity.this, LivingLabGlobalSettingsActivity.class);
					startActivity(labIntent);
				}
			}
		});
	}
	
	@Override
	public boolean isModuleHomeActivity() {
		return true;
	}
	
	@Override
	protected NewModule getNewModule() {
		return new LivingLabsModule();
	}
	@Override
	protected boolean isScrollable() {
		return false;
	}
	@Override
	protected void onOptionSelected(String optionId) {
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.layout.living_lab_menu, menu);
		return true;
	}

	public void livingLabSettings(){
		Log.v(TAG, "LivingLabSettings");
	}
	
	private void unionProbesFromLabs(List<LivingLabItem> labs) throws JSONException{
		Map<String, Set<String>> purposes = new HashMap<String, Set<String>>();
		Set<String> allProbesSet = new HashSet<String>();
		for(LivingLabItem labItem : labs){
	        ArrayList<LivingLabVisualizationItem> visualization = labItem.getVisualizations();
	        
	        for(int i=0; i<visualization.size(); i++){
	        	LivingLabVisualizationItem visualizationItem = visualization.get(i);
	        	
	        	try {
					Set<LivingLabDataItem> probeSet = visualizationItem.getData();
					for(LivingLabDataItem probe : probeSet){
						String tempData = probe.getKey();
						
						Set<String> existingPurposes = purposes.get(tempData);
						String tempDataString = tempDataToString(tempData);
						if(existingPurposes == null){
							Set<String> tempPurposes = new HashSet<String>();
							tempPurposes.addAll(probe.getPurposes());
							purposes.put(tempDataString, tempPurposes);
						} else{
							Set<String> tempPurposes = existingPurposes;
							tempPurposes.addAll(probe.getPurposes());
							purposes.put(tempDataString, tempPurposes);
						}
						
	
						if(tempData.contains("Probe")){					
							allProbesSet.add(tempDataString);
						}		
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
	        }
		}
		
		JSONObject llsiObject = new JSONObject();
		
		llsiObject.put("app_id", "Living Lab");
		llsiObject.put("lab_id", "all");
		for(String probe: allProbesSet){
			llsiObject.put(probe, 1);
		}
		
		LivingLabSettingItem llsi = new LivingLabSettingItem(llsiObject);
		
		LivingLabsAccessControlDB mLivingLabAccessControlDB = LivingLabsAccessControlDB.getInstance(this);
		mLivingLabAccessControlDB.saveLivingLabSettingItem(llsi);
	}
	
	private String tempDataToString(String tempData){
		StringTokenizer tempDataTokenizer = new StringTokenizer(tempData, " ");
		StringBuilder tempDataBuilder = new StringBuilder();
		while(tempDataTokenizer.hasMoreTokens()){
			tempDataBuilder.append(tempDataTokenizer.nextToken().toLowerCase() + "_");
		}
		String tempDataString = tempDataBuilder.toString().substring(0, tempDataBuilder.toString().length() - 1);
		return tempDataString;
	}
		

}