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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

 
public class LivingLabSettingsProbesActivity extends NewModuleActivity  implements OnClickListener, OnCheckedChangeListener {
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

	private JSONObject probesMap = new JSONObject();
	private ArrayList<Integer> probesIds = new ArrayList<Integer>();

	private HashMap<String, Boolean> probeSettings = new HashMap<String, Boolean>();
	
	private LivingLabsAccessControlDB mLivingLabAccessControlDB;
	
	
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
		
		populateProbesMap();
		
		mLivingLabAccessControlDB = LivingLabsAccessControlDB.getInstance(this);
        app_id = "Living Lab";       		
        LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
        
        ArrayList<LivingLabVisualizationItem> visualization = labItem.getVisualizations();
        lab_id = labItem.getName();
        
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 60, 0, 0);//60dp at top
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        
        TextView labText = new TextView(this);
        labText.setText(Html.fromHtml("<h4>" + lab_id + ": Data</h4>"+ "Turn on data for <b>" + lab_id + "</b>. If you don't, " + lab_id + " will not work as expected."));
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
		try {
			loadFlag = true;
			loadParams = new JSONObject();
			loadParams.put("app_id", app_id);
			loadParams.put("lab_id", lab_id);
			connection = new Connection(this);
			connection.execute(loadParams).get(3000, TimeUnit.MILLISECONDS);
			loadFlag = false;
			LivingLabsAccessControlDB.saveLivingLabProbeItem(probesFromServer);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		TableLayout tableLayout = new TableLayout(this);
		tableLayout.setColumnShrinkable(1, true);
		TableLayout.LayoutParams layoutRow = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

		TableRow tableRow = null;
		for(String probe : allProbesSet){
			
            boolean probeChecked = false;
            
            if(settingsFromServer.length() > 0){
	            JSONObject settingFromServer;
				try {
					settingFromServer = settingsFromServer.getJSONObject(0);
//	            	if(probe.equalsIgnoreCase("Activity Probe"))
//	            		probeChecked = settingFromServer.getBoolean("activity_probe");
//	            	else if(probe.equalsIgnoreCase("Sms Probe"))
//	            		probeChecked = settingFromServer.getBoolean("sms_probe");
//	            	else if(probe.equalsIgnoreCase("Call Log Probe"))
//	            		probeChecked = settingFromServer.getBoolean("call_log_probe");
//		            else if(probe.equalsIgnoreCase("Bluetooth Probe"))
//		                probeChecked = settingFromServer.getBoolean("bluetooth_probe");
//		            else if(probe.equalsIgnoreCase("Wifi Probe"))
//		                probeChecked = settingFromServer.getBoolean("wifi_probe");
//		            else if(probe.equalsIgnoreCase("Simple Location Probe"))
//		                probeChecked = settingFromServer.getBoolean("simple_location_probe");
//		            else if(probe.equalsIgnoreCase("Screen Probe"))
//		                probeChecked = settingFromServer.getBoolean("screen_probe");
//		            else if(probe.equalsIgnoreCase("Running Applications Probe"))
//		                probeChecked = settingFromServer.getBoolean("running_applications_probe");
//		            else if(probe.equalsIgnoreCase("Hardware Info Probe"))
//		                probeChecked = settingFromServer.getBoolean("hardware_info_probe");
//		            else if(probe.equalsIgnoreCase("App Usage Probe"))
//		                probeChecked = settingFromServer.getBoolean("app_usage_probe");
					
					probeChecked = settingFromServer.getBoolean(probe);
		               
	            	Log.v(TAG, "probe: " + probe + ", probeChecked: " + probeChecked);
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
	        
//			ToggleButton probeButton = new ToggleButton(this);
	        Switch probeButton = new Switch(this);
//			Drawable icon = null;
//			StringBuilder iconPath = new StringBuilder("@drawable/");
//			String probeText = "";
//			String iconName = "";
			int probeId = 0;
//			
			try {
//				iconName = (String) ((JSONObject) probesMap.get(probe)).get("icon");
				probeId = (Integer) ((JSONObject) probesMap.get(probe)).get("id");
				probesIds.add(probeId); //accumulate the ids
				
				Log.v(TAG, "going to assign probeId: " + probeId);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			
//			int iconValue = getResources().getIdentifier(iconName , "drawable", getPackageName());
//			iconPath.append(iconName);
//			Log.v(TAG, iconPath.toString());
//			
//			icon = Drawable.createFromPath(iconPath.toString());		
//			//probeButton.setBackgroundDrawable(getResources().getDrawable(iconValue));
//			probeButton.setCompoundDrawablesWithIntrinsicBounds(0, iconValue, 0, 0);
			probeButton.setChecked(probeChecked);
//			probeButton.setTextColor(Color.BLUE);
			probeButton.setId(probeId);	
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
			
//	        probePurpose.setText(Html.fromHtml("<b>" + probe.split(" Probe")[0] +"</b>: <br/>" + purposes.get(probe).toString()));
			Log.v(TAG, probe_name.toString());
			probePurpose.setText(Html.fromHtml("<b>" + probe_name.toString() +"</b>: <br/>" + purposes.get(probe).toString()));
	        probePurpose.setLayoutParams(layoutPurpose);
	        probePurpose.setGravity(Gravity.CENTER);
	        tableRow.setGravity(Gravity.CENTER);
	       
	        
	        
			tableRow.addView(probePurpose);
			tableLayout.addView(tableRow);
		}

		ll.addView(tableLayout);
		
		//int dataAggregationId = probeId;
		TextView dataAggregationText = new TextView(this);
		dataAggregationText.setText(Html.fromHtml("Opt-in to data aggregation."));
		dataAggregationText.setTextSize(14);
		//dataAggregationText.setId(textId);
        ll.addView(dataAggregationText);
        
        Switch dataAggregationButton = new Switch(this);
        ll.addView(dataAggregationButton);
        
        
        
        
        
	    TableLayout buttonsLayout = new TableLayout(this);
	    buttonsLayout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT));
        
        TableRow buttonsRow = new TableRow(this);
	    //buttonsRow.setPadding(20,50,40,0);
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
	    nextButton.setText("Next");
	    nextButton.setId(nextButtonId);
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
        
	    ScrollView sv = new ScrollView(this);
	    sv.addView(ll);
	    setContentView(sv);
	    
	    View textIdView = findViewById(textId);
	    View rootView = textIdView.getRootView();
	    rootView.setBackgroundColor(getResources().getColor(android.R.color.white));
  
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
//				LivingLabsAccessControlDB.saveLivingLabProbeItem(probesFromServer);
//				LivingLabsAccessControlDB.saveLivingLabProbeItem(probesFromServer);				
				JSONObject probesToSave = prepareProbesForSave(probeSettings);
				LivingLabsAccessControlDB.saveLivingLabProbeItem(probesToSave);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			Intent intent = new Intent(this, LivingLabSettingsContextActivity.class);
			LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
			intent.putExtra("lab", labItem);
			intent.putExtra("probeSettings", probeSettings);
			intent.putExtra("context_label", settings_context_label);
			intent.putExtra("contextsFromServer", contextsFromServer.toString());
			startActivity(intent);
		}
	}
	
	public void selectProbe(int probeId){
//		Log.v(TAG, "probeId: " + probeId);
//		ToggleButton probeButton = (ToggleButton) findViewById(probeId);
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
//	        		JSONObject result = new JSONObject(pds.loadAccessControlData(loadParams));
	        		JSONObject result = new JSONObject(pds.accessControlData(loadParams, "load"));
	        		
	        		Log.v(TAG, result.toString());
	        		contextsFromServer = (JSONArray) result.get("contexts");
	        		settingsFromServer = (JSONArray) result.get("settings");
	        		probesFromServer = (JSONObject) result.getJSONObject("probes");
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
		try{
			Iterator<String> keys = probesMap.keys();
	        while(keys.hasNext()){
	        	String probeName = keys.next();
	            JSONObject probeDetails = probesMap.getJSONObject(probeName);
	            if(probeButton.getId() == probeDetails.getInt("id")){
	            	probeSettings.put(probeName, isChecked);
	            }
	        }
        } catch(Exception e){
        	e.printStackTrace();
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
//			probesMap.put("Activity Probe", new JSONObject() {{
//		        put("id",4);
//		        put("icon", "livinglab_activity_probe");
//		    }});
//			probesMap.put("Sms Probe", new JSONObject() {{
//		        put("id",5);
//		        put("icon", "livinglab_sms_probe");
//		    }});
//			probesMap.put("Call Log Probe", new JSONObject() {{
//		        put("id",6);
//		        put("icon", "livinglab_calllog_probe");
//		    }});
//			probesMap.put("Bluetooth Probe", new JSONObject() {{
//		        put("id",7);
//		        put("icon", "livinglab_bluetooth_probe");
//		    }});
//			probesMap.put("Wifi Probe", new JSONObject() {{
//		        put("id",8);
//		        put("icon", "livinglab_screen_probe");
//		    }});
//			probesMap.put("Simple Location Probe", new JSONObject() {{
//		        put("id",9);
//		        put("icon", "livinglab_location_probe");
//		    }});
//			probesMap.put("Screen Probe", new JSONObject() {{
//		        put("id",10);
//		        put("icon", "livinglab_screen_probe");
//		    }});
//			probesMap.put("Running Applications Probe", new JSONObject() {{
//		        put("id",11);
//		        put("icon", "livinglab_screen_probe");
//		    }});
//			probesMap.put("Hardware Info Probe", new JSONObject() {{
//		        put("id",12);
//		        put("icon", "livinglab_screen_probe");
//		    }});
//			probesMap.put("App Usage Probe", new JSONObject() {{
//		        put("id",13);
//		        put("icon", "livinglab_screen_probe");
//		    }});
			
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
		
		for(Entry<String, Boolean> probe: probesMap.entrySet()){
			result.put(probe.getKey(), probe.getValue());
		}
		Log.v(TAG, result.toString());
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

	@Override
	protected NewModule getNewModule() {
		// TODO Auto-generated method stub
		//return null;
		return new LivingLabsModule();
	}

	@Override
	protected boolean isScrollable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onOptionSelected(String optionId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isModuleHomeActivity() {
		// TODO Auto-generated method stub
		return false;
	}
}