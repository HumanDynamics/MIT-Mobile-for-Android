package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.media.openpds.client.PreferencesWrapper;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

 
public class LivingLabSettingsProbesActivity extends Activity implements OnClickListener, OnCheckedChangeListener {
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
	
	private int nextButtonId = 1;
	private final int activityProbeId = 2;
	private final int smsProbeId = 3;
	private final int calllogProbeId = 4;
	private final int bluetoothProbeId = 5;
	private final int wifiProbeId = 6;
	private final int simplelocationProbeId = 7;
	private final int screenProbeId = 8;
	private final int runningapplicationsProbeId = 9;
	private final int hardwareinfoProbeId = 10;
	private final int appusageProbeId = 11;
	
	private HashMap<String, Boolean> probeSettings = new HashMap<String, Boolean>();
	
	
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
		
        app_id = "Living Lab";       		
        LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
        
        ArrayList<LivingLabVisualizationItem> visualization = labItem.getVisualizations();
        lab_id = labItem.getName();
        
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 60, 0, 0);//60dp at top
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        
        TextView labText = new TextView(this);
        labText.setText("Select data for " + lab_id + " to use. Indicated functionality may not work if associated data not selected.");
        labText.setTextSize(16);
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
					if(existingPurposes == null){
						Set<String> tempPurposes = new HashSet<String>();
						tempPurposes.addAll(probe.getPurposes());
						purposes.put(tempData, tempPurposes);
					} else{
						Set<String> tempPurposes = existingPurposes;
						tempPurposes.addAll(probe.getPurposes());
						purposes.put(tempData, tempPurposes);
					}
					

					if(tempData.contains("Probe")){
						allProbesSet.add(tempData);
						labProbes.put(tempData);
					}		
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        int id = 0;
        
    	
        
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
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		TableLayout tableLayout = new TableLayout(this);
		TableLayout.LayoutParams layoutRow = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

		TableRow tableRow = null;
		for(String probe : allProbesSet){
			
            boolean probeChecked = false;
            
            if(settingsFromServer.length() > 0){
	            JSONObject settingFromServer;
				try {
					settingFromServer = settingsFromServer.getJSONObject(0);
	            	if(probe.equalsIgnoreCase("Activity Probe"))
	            		probeChecked = settingFromServer.getBoolean("activity_probe");
	            	else if(probe.equalsIgnoreCase("Sms Probe"))
	            		probeChecked = settingFromServer.getBoolean("sms_probe");
	            	else if(probe.equalsIgnoreCase("Call Log Probe"))
	            		probeChecked = settingFromServer.getBoolean("call_log_probe");
		            else if(probe.equalsIgnoreCase("Bluetooth Probe"))
		                probeChecked = settingFromServer.getBoolean("bluetooth_probe");
		            else if(probe.equalsIgnoreCase("Wifi Probe"))
		                probeChecked = settingFromServer.getBoolean("wifi_probe");
		            else if(probe.equalsIgnoreCase("Simple Location Probe"))
		                probeChecked = settingFromServer.getBoolean("simple_location_probe");
		            else if(probe.equalsIgnoreCase("Screen Probe"))
		                probeChecked = settingFromServer.getBoolean("screen_probe");
		            else if(probe.equalsIgnoreCase("Running Applications Probe"))
		                probeChecked = settingFromServer.getBoolean("running_applications_probe");
		            else if(probe.equalsIgnoreCase("Hardware Info Probe"))
		                probeChecked = settingFromServer.getBoolean("hardware_info_probe");
		            else if(probe.equalsIgnoreCase("App Usage Probe"))
		                probeChecked = settingFromServer.getBoolean("app_usage_probe");
		               
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
	        
			ToggleButton probeButton = new ToggleButton(this);
			Drawable icon = null;
			StringBuilder iconPath = new StringBuilder("@drawable/");
			String probeText = "";
			String iconName = "";
			int probeId = 0;
			
			if(probe.equalsIgnoreCase("Call Log Probe")){
				iconName = "livinglab_calllog_probe";
				probeId = calllogProbeId;
			}
			else if(probe.equalsIgnoreCase("Screen Probe")){
				iconName = "livinglab_screen_probe";
				probeId = screenProbeId;
			}
			else if(probe.equalsIgnoreCase("Activity Probe")){
				iconName = "livinglab_activity_probe";
				probeId = activityProbeId;
			}
			else if(probe.equalsIgnoreCase("Sms Probe")){
				iconName = "livinglab_sms_probe";
				probeId = smsProbeId;
			}
			else if(probe.equalsIgnoreCase("Bluetooth Probe")){
				iconName = "livinglab_bluetooth_probe";
				probeId = bluetoothProbeId;
			}
			
			int iconValue = getResources().getIdentifier(iconName , "drawable", getPackageName());
			iconPath.append(iconName);
			Log.v(TAG, iconPath.toString());
			
			icon = Drawable.createFromPath(iconPath.toString());		
			//probeButton.setBackgroundDrawable(getResources().getDrawable(iconValue));
			probeButton.setCompoundDrawablesWithIntrinsicBounds(0, iconValue, 0, 0);
			probeButton.setChecked(probeChecked);
			probeButton.setTextColor(Color.BLUE);
			probeButton.setId(probeId);	
			probeButton.setOnCheckedChangeListener(this);
			
			probeSettings.put(probe, probeChecked);
			
			tableRow.addView(probeButton, 200, 200);
			
			TableRow.LayoutParams layoutPurpose = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			TextView probePurpose = new TextView(this);
	        probePurpose.setText(" for " + purposes.get(probe).toString());
	        probePurpose.setLayoutParams(layoutPurpose);
	        probePurpose.setGravity(Gravity.CENTER);
	        tableRow.setGravity(Gravity.CENTER);
	        
			tableRow.addView(probePurpose);
			tableLayout.addView(tableRow);
		}

		ll.addView(tableLayout);
        
	    TableLayout buttonsLayout = new TableLayout(this);
        TableRow buttonsRow = new TableRow(this);
	    //buttonsRow.setPadding(20,50,40,0);
	    
	    Button nextButton = new Button(this);
	    nextButton.setText("Next");
	    nextButton.setId(nextButtonId);
	    nextButton.setOnClickListener(this);
	    
	    buttonsRow.addView(nextButton);
	    buttonsRow.setGravity(Gravity.RIGHT);

	    buttonsLayout.addView(buttonsRow);
	    ll.addView(buttonsLayout);
        
	    ScrollView sv = new ScrollView(this);
	    sv.addView(ll);
	    setContentView(sv);
  
    }
	
	@Override
	public void onClick(View v) {
		if(v.getId() == nextButtonId){
			Intent intent = new Intent(this, LivingLabSettingsContextActivity.class);
			LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
			intent.putExtra("lab", labItem);
			intent.putExtra("probeSettings", probeSettings);
			intent.putExtra("context_label", settings_context_label);
			intent.putExtra("contextsFromServer", contextsFromServer.toString());
			startActivity(intent);
		}
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
	        		JSONObject result = new JSONObject(pds.loadAccessControlData(loadParams));
	        		
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
		switch(probeButton.getId()){
			case activityProbeId:
				probeSettings.put("Activity Probe", isChecked);
				break;
			case smsProbeId:
				probeSettings.put("Sms Probe", isChecked);
				break;				
			case calllogProbeId:
				probeSettings.put("Call Log Probe", isChecked);
				break;	
			case bluetoothProbeId:
				probeSettings.put("Bluetooth Probe", isChecked);
				break;	
			case wifiProbeId:
				probeSettings.put("Wifi Probe", isChecked);
				break;	
			case simplelocationProbeId:
				probeSettings.put("Simple Location Probe", isChecked);
				break;	
			case screenProbeId:
				probeSettings.put("Screen Probe", isChecked);
				break;	
			case runningapplicationsProbeId:
				probeSettings.put("Running Applications Probe", isChecked);
				break;	
			case hardwareinfoProbeId:
				probeSettings.put("Hardware Info Probe", isChecked);
				break;	
			case appusageProbeId:
				probeSettings.put("App Usage Probe", isChecked);
				break;	
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
	
	@Override
	public void onBackPressed() {
	}
}