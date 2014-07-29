package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.media.openpds.client.PreferencesWrapper;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleActivity;
import edu.mit.mitmobile2.objs.LivingLabDataItem;
import edu.mit.mitmobile2.objs.LivingLabItem;
import edu.mit.mitmobile2.objs.LivingLabSettingItem;
import edu.mit.mitmobile2.objs.LivingLabVisualizationItem;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

 
public class LivingLabSettingsProbesActivity extends NewModuleActivity implements OnClickListener, OnCheckedChangeListener {
	private static final String TAG = "LLSettingsProbesActivity";
    String settings_context_label = null;
    private JSONObject llsiJson, loadParams;
    private JSONArray labProbes = new JSONArray();
    
    String app_id, lab_id;
    
    Set<Integer> requiredIdsList = new HashSet<Integer>();
    Map<String, Set<String>> purposes = new HashMap<String, Set<String>>();
    
	private LivingLabFunfPDS pds;
	private Connection connection;
	
	private JSONArray settingsFromServer, contextsFromServer;
	private JSONObject probesFromServer; //returns union of the probes (and their statuses) on the server.
	
	private boolean loadFlag = false;
	
	private int textId = 0;
	private int selectAllButtonId = 1;
	private int nextButtonId = 2;
	private int nextButtonTextId = 3;
	
	private int probeId = 4;

	private JSONObject probesMap = new JSONObject();
	private ArrayList<Integer> probesIds = new ArrayList<Integer>();

	private HashMap<String, Boolean> probeSettings = new HashMap<String, Boolean>();
	
	private LivingLabsAccessControlDB mLivingLabAccessControlDB;
	
	private boolean aggregationFlag = false;
	private int aggregationSwitchId = 0;
	
	private boolean probesSetFlag = false;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		//If user is not logged in, redirect to touchstone
		try {
			pds = new LivingLabFunfPDS(this);
		} catch (Exception e) {
			Intent intent = new Intent(this, LivingLabsLoginActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		setContentView(R.layout.living_lab_scrollview);
	    LinearLayout ll = (LinearLayout)findViewById(R.id.livinglabLinearLayout);

		
//		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	    View view = getLayoutInflater().inflate(R.layout.living_lab_scrollview, null);
	    
		populateProbesMap();
		
		mLivingLabAccessControlDB = LivingLabsAccessControlDB.getInstance(this);
        app_id = "Living Lab";       		
        LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
        
        ArrayList<LivingLabVisualizationItem> visualization = labItem.getVisualizations();
        lab_id = labItem.getName();
        
//        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 60, 0, 0);//60dp at top
//        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        ll.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.FILL_PARENT, ScrollView.LayoutParams.FILL_PARENT));
        
        TextView labText = new TextView(this);
        labText.setText(Html.fromHtml("<h4>" + lab_id + ": Data Permissions</h4>"+ "Turn on the following data for <b>" + lab_id + "</b>. If you don't, " + lab_id + " will not work optimally."));
        labText.setTextSize(14);
        labText.setId(textId);
        ll.addView(labText);
        
        RelativeLayout rl = new RelativeLayout(this);
        rl.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));

		Set<String> allProbesSet = new HashSet<String>();
        
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
						labProbes.put(tempData);
					}		
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }

        ArrayList<String> searchDataInput = new ArrayList<String>();
        searchDataInput.add(app_id);
        searchDataInput.add(lab_id);
        //change this so we only save if data not present.
		try {
			loadFlag = true;
			loadParams = new JSONObject();
			loadParams.put("app_id", app_id);
			loadParams.put("lab_id", lab_id);
			connection = new Connection(this);
			connection.execute(loadParams).get(3000, TimeUnit.MILLISECONDS);
			loadFlag = false;
			Log.v(TAG, "probesFromServer: " + probesFromServer);
			Log.v(TAG, "settingsFromServer: "  + settingsFromServer);
			//LivingLabsAccessControlDB.loadLivingLabProbeItem(probesFromServer); //setting
			LivingLabsAccessControlDB.loadLivingLabProbeItem(settingsFromServer.getJSONObject(0)); //setting
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		TableLayout tableLayout = new TableLayout(this);
		tableLayout.setColumnShrinkable(1, true);
		TableLayout.LayoutParams layoutRow = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

		TableRow tableRow = null;
		for(String probe : allProbesSet){
			
            boolean probeChecked = false;
//            Log.v(TAG, settingsFromServer.toString());
            if(settingsFromServer.length() > 0){
	            JSONObject settingFromServer;
				try {
					settingFromServer = settingsFromServer.getJSONObject(0);
					probeChecked = settingFromServer.getBoolean(probe);
		               
	            	int context_label_id = settingFromServer.getInt("context_label_id");
		            settings_context_label = getContextLabel(context_label_id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
            } else {
            	probeChecked = false;
            }
            
            
			tableRow = new TableRow(this);
			
	        tableRow.setLayoutParams(layoutRow);
	        
	        Switch probeButton = new Switch(this);	
			try {
				probeId = (Integer) ((JSONObject) probesMap.get(probe)).get("id");
				probesIds.add(probeId); //accumulate the ids
			} catch (JSONException e) {
				e.printStackTrace();
			}
			

			probesSetFlag = probesSetFlag || probeChecked;
			
			Log.v(TAG, "probesSetFlag: " + probesSetFlag);
			probeButton.setChecked(probeChecked);
			probeButton.setId(probeId);	
			probeId++;
			probeButton.setOnCheckedChangeListener(this);
			
			probeSettings.put(probe, probeChecked);
			
			tableRow.addView(probeButton, 400, 200);
			
			TableRow.LayoutParams layoutPurpose = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			TextView probePurpose = new TextView(this);
			
			String[] probe_name_parts = ((String)probe.split("_probe")[0]).split("_");
			StringBuilder probe_name = new StringBuilder();
			for(int i=0; i<probe_name_parts.length; i++){
				probe_name.append(probe_name_parts[i].substring(0,1).toUpperCase() + probe_name_parts[i].substring(1) + " ");
			}
			
			probePurpose.setText(Html.fromHtml("<b>" + probe_name.toString() +"</b>: <br/>" + purposes.get(probe).toString()));
	        probePurpose.setLayoutParams(layoutPurpose);
	        probePurpose.setGravity(Gravity.CENTER);
	        tableRow.setGravity(Gravity.CENTER);
	        
			tableRow.addView(probePurpose);
			tableLayout.addView(tableRow);
		}

		ll.addView(tableLayout);
		
		Space beforeAggregation = new Space(this);
		beforeAggregation.setLayoutParams(layoutRow);
		beforeAggregation.getLayoutParams().height = 20;
		ll.addView(beforeAggregation);
		
		TextView dataAggregationText = new TextView(this);
		dataAggregationText.setText(Html.fromHtml("<b>Opt-in to data aggregation</b>"));
		dataAggregationText.setTextSize(14);
        
		
//		///////
//        Switch dataAggregationButton = new Switch(this);
//        dataAggregationButton.setChecked(aggregationFlag);
//        dataAggregationButton.setId(probeId);	
//        aggregationSwitchId = probeId;
//		probeId++;
//		dataAggregationButton.setOnCheckedChangeListener(this);
//		///////
		
        CheckBox dataAggregationCheckBox = new CheckBox(this);
        dataAggregationCheckBox.setChecked(aggregationFlag);
        dataAggregationCheckBox.setId(probeId);	
        aggregationSwitchId = probeId;
		probeId++;
		dataAggregationCheckBox.setOnCheckedChangeListener(this);
        
		tableLayout = new TableLayout(this);
		tableLayout.setColumnShrinkable(1, true);
		layoutRow = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		tableRow = new TableRow(this);
		
        tableRow.setLayoutParams(layoutRow);
        tableRow.addView(dataAggregationCheckBox);
//        tableRow.addView(dataAggregationButton);
        tableRow.addView(dataAggregationText);

        
        tableLayout.addView(tableRow);
        ll.addView(tableLayout);

	    TableLayout buttonsLayout = new TableLayout(this);
	    buttonsLayout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT));
        
        TableRow buttonsRow = new TableRow(this);
        buttonsRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        
        LinearLayout.LayoutParams buttonLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.weight = 1;
	    
        
        Button selectAllButton = new Button(this);
        selectAllButton.setText("Select all data");
        selectAllButton.setId(selectAllButtonId);
        selectAllButton.setOnClickListener(this);
        buttonLayoutParams.gravity = Gravity.LEFT;
        selectAllButton.setLayoutParams(buttonLayoutParams);
        
	    Button nextButton = new Button(this);
	    nextButton.setText("Continue");
	    nextButton.setId(nextButtonId);
	    Log.v(TAG, "flag is again: " + probesSetFlag);
	    nextButton.setEnabled(probesSetFlag);
	    nextButton.setOnClickListener(this);
	    buttonLayoutParams.gravity = Gravity.RIGHT;
	    nextButton.setLayoutParams(buttonLayoutParams);
	    
	    
	    buttonsRow.setWeightSum(2);
	    buttonsRow.addView(selectAllButton);
	    buttonsRow.addView(nextButton);
	    buttonsRow.setGravity(Gravity.CENTER);

	    buttonsLayout.addView(buttonsRow);
	    ll.addView(buttonsLayout);

        TextView nextButtonText = new TextView(this);
        nextButtonText.setText("Click next to proceed.");
        nextButtonText.setTextColor(Color.BLUE);
        nextButtonText.setTextSize(12);
        nextButtonText.setVisibility(View.GONE);
        nextButtonText.setId(nextButtonTextId);
        ll.addView(nextButtonText);
        
	    //ScrollView sv = new ScrollView(this);
        //ScrollView sv = (ScrollView) findViewById(R.id.livinglabScrollView);
//        ScrollView sv = (ScrollView) view.findViewById(R.id.livinglabScrollView);
//	    sv.addView(ll);
//	    setContentView(sv);
	    
	    View textIdView = findViewById(textId);
	    View rootView = textIdView.getRootView();
	    rootView.setBackgroundColor(getResources().getColor(android.R.color.white));
	    
//	    try {
//			checkProbesSet(probesFromServer);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
  
    }
	
	@Override
	public void onClick(View v) {
		if(v.getId() == selectAllButtonId){
			try{
				Iterator<String> keys = probesMap.keys();
		        while(keys.hasNext()){
		            JSONObject probeDetails = probesMap.getJSONObject(keys.next());
		            
		            int probeIdValue = probeDetails.getInt("id");
		            if(probesIds.contains(probeIdValue))
		            	selectProbe(probeIdValue);
		        }
		        TextView nextButtonTextView = (TextView) findViewById(nextButtonTextId);
		        nextButtonTextView.setVisibility(View.VISIBLE);
	        } catch(Exception e){
	        	e.printStackTrace();
	        }
			
		} else if(v.getId() == nextButtonId){
			try {
				//save locally and go to context.			
				JSONObject probesToSave = prepareProbesForSave(probeSettings);
				
				LivingLabSettingItem llsi = new LivingLabSettingItem(probesToSave);
				LivingLabsAccessControlDB.saveLivingLabSettingItem(llsi);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			Intent intent = new Intent(this, LivingLabSettingsContextActivity.class);
			LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
			intent.putExtra("lab", labItem);
			intent.putExtra("probeSettings", probeSettings);
			intent.putExtra("context_label", settings_context_label);
			intent.putExtra("contextsFromServer", contextsFromServer.toString());
			intent.putExtra("data_aggregation", aggregationFlag);
			startActivity(intent);
		}
	}
	
	public void selectProbe(int probeId){
		Switch probeButton = (Switch) findViewById(probeId);
		probeButton.setChecked(true);
	}
	
	private class Connection extends AsyncTask<JSONObject, Object, Object> {
		 
		private Context mContext;
		
        @Override
        protected Object doInBackground(JSONObject... object) {
        	try {
        		
        		if(loadFlag){
	        		PreferencesWrapper prefs = new PreferencesWrapper(mContext);
	        		String uuid = prefs.getUUID();
	        		loadParams.put("datastore_owner", uuid); 
	        		JSONObject result = new JSONObject(pds.accessControlData(loadParams, "load"));

	        		contextsFromServer = (JSONArray) result.get("contexts");
	        		settingsFromServer = (JSONArray) result.get("settings");
	        		probesFromServer = (JSONObject) result.getJSONObject("probes");
	        		aggregationFlag = result.getBoolean("data_aggregation");
	        		
	        		//checkProbesSet(probesFromServer);
        		} 
			} catch (Exception e) {
				e.printStackTrace();
			}
            return null;
        }

        public Connection(Context context) {
            this.mContext = context;
        }
 
    }

	@Override
	public void onCheckedChanged(CompoundButton probeButton, boolean isChecked) {
		if(probeButton.getId() == aggregationSwitchId){
			aggregationFlag = isChecked;
		} else{
			try{
				
				Iterator<String> keys = probesMap.keys();
		        while(keys.hasNext()){
		        	String probeName = keys.next();
		            JSONObject probeDetails = probesMap.getJSONObject(probeName);
		            if(probeButton.getId() == probeDetails.getInt("id")){
		            	probeSettings.put(probeName, isChecked);
		            }
		        }
		        
		        Log.v(TAG, "probeSettings: " + probeSettings.toString());
				checkProbesSet(probeSettings);
	        } catch(Exception e){
	        	e.printStackTrace();
	        }
		}
	}
	
	private String getContextLabel(int context_label_id) throws JSONException{
		String label = "New";
		for(int i=0; i < contextsFromServer.length(); i++){
			int id_value = contextsFromServer.getJSONObject(i).getInt("id");
			if(id_value == context_label_id)
				label = contextsFromServer.getJSONObject(i).getString("context_label");
		}
		return label;
	}
	
//	@Override
//	public void onBackPressed() {
//	}
	
	public void populateProbesMap(){
		try{			
			probesMap.put("activity_probe", new JSONObject() {{
		        put("id",4);
		    }});
			probesMap.put("sms_probe", new JSONObject() {{
		        put("id",5);
		    }});
			probesMap.put("call_log_probe", new JSONObject() {{
		        put("id",6);
		    }});
			probesMap.put("bluetooth_probe", new JSONObject() {{
		        put("id",7);
		    }});
			probesMap.put("wifi_probe", new JSONObject() {{
		        put("id",8);
		    }});
			probesMap.put("simple_location_probe", new JSONObject() {{
		        put("id",9);
		    }});
			probesMap.put("screen_probe", new JSONObject() {{
		        put("id",10);
		    }});
			probesMap.put("running_applications_probe", new JSONObject() {{
		        put("id",11);
		    }});
			probesMap.put("hardware_info_probe", new JSONObject() {{
		        put("id",12);
		    }});
			probesMap.put("app_usage_probe", new JSONObject() {{
		        put("id",13);
		    }});			

		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private JSONObject prepareProbesForSave(Map<String, Boolean> probesMap) throws JSONException{
		JSONObject result = new JSONObject();
		
		result.put("app_id", app_id);
		result.put("lab_id", lab_id);
		for(Entry<String, Boolean> probe: probesMap.entrySet()){
			result.put(probe.getKey(), probe.getValue()? 1 : 0);
		}
//		Log.v(TAG, result.toString());
		return result;
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
	
	private void checkProbesSet(HashMap<String, Boolean> probes) throws JSONException{
		
		boolean probesFlag = false;
		for (Map.Entry<String, Boolean> entry : probes.entrySet()) {
		    if(entry.getValue()){
		    	probesFlag = true;
		    	break;
		    }
		}
		
		Button nextButton = (Button) findViewById(nextButtonId);
		nextButton.setEnabled(probesFlag);
		
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
	protected boolean isModuleHomeActivity() {
		return false;
	}
	
	public void livingLabSettings(){
		Log.v(TAG, "LivingLabSettings");
	}
}