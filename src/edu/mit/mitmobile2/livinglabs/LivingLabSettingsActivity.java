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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

 
public class LivingLabSettingsActivity extends Activity implements OnClickListener, OnTouchListener, OnItemSelectedListener {
	private static final String TAG = "LLSettingsActivity";
    private int[] buttonIds = new int[2];
    private int editContextId, probeErrorTextId, contextErrorTextId;
    String settings_context_label = null;
    private JSONObject llsiJson, loadParams;
    
    private ArrayList<CheckBox> checkBoxes = new ArrayList<CheckBox>();
    
    String app_id, lab_id;
    
    Set<Integer> requiredIdsList = new HashSet<Integer>();
    Map<String, Set<String>> purposes = new HashMap<String, Set<String>>();
    
	private LivingLabFunfPDS pds;
	private Connection connection;
	
	private JSONArray settingsFromServer, contextsFromServer;
	
	private boolean loadFlag = false, saveFlag = false;
	
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
        labText.setText(lab_id + " can access the following probes.");
        ll.addView(labText);
        
        RelativeLayout rl = new RelativeLayout(this);
        rl.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));
        
        Set<String> requiredProbesSet = new HashSet<String>();
		Set<String> nonRequiredProbesSet = new HashSet<String>();
		Set<String> allProbesSet = new HashSet<String>();
        
        for(int i=0; i<visualization.size(); i++){
        	LivingLabVisualizationItem visualizationItem = visualization.get(i);
			try {
				JSONObject dataObject = visualizationItem.getData();
				JSONArray requiredProbesArray = dataObject.getJSONArray("required");
				JSONArray nonRequiredProbesArray = dataObject.getJSONArray("non-required");
				
				for(int j=0; j<requiredProbesArray.length(); j++){
					//String tempData = requiredProbesArray.getString(j);
					LivingLabDataItem dataItem = (LivingLabDataItem) requiredProbesArray.get(j);
					String tempData = dataItem.getKey();
					
					Set<String> existingPurposes = purposes.get(tempData);
					if(existingPurposes == null){
						Set<String> tempPurposes = new HashSet<String>();
						tempPurposes.addAll(dataItem.getPurposes());
						purposes.put(tempData, tempPurposes);
					} else{
						Set<String> tempPurposes = existingPurposes;
						tempPurposes.addAll(dataItem.getPurposes());
						purposes.put(tempData, tempPurposes);
					}
					

					if(tempData.contains("Probe")){
						requiredProbesSet.add(tempData);
						allProbesSet.add(tempData);
					}						
				}
				for(int k=0; k<nonRequiredProbesArray.length(); k++){
					//String tempData = nonRequiredProbesArray.getString(k);
					LivingLabDataItem dataItem = (LivingLabDataItem) nonRequiredProbesArray.get(k);
					String tempData = dataItem.getKey();

					Set<String> existingPurposes = purposes.get(tempData);
					if(existingPurposes == null){
						Set<String> tempPurposes = new HashSet<String>();
						tempPurposes.addAll(dataItem.getPurposes());
						purposes.put(tempData, tempPurposes);
					} else{
						Set<String> tempPurposes = existingPurposes;
						tempPurposes.addAll(dataItem.getPurposes());
						purposes.put(tempData, tempPurposes);
					}
					
					if(tempData.contains("Probe")){
						nonRequiredProbesSet.add(tempData);
						allProbesSet.add(tempData);
					}						
				}
			} catch (JSONException e) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        boolean requiredSelected = true;
        for(String probe : allProbesSet) {
        	
        	TableRow probeRow = new TableRow(this);
            probeRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
                    
            CheckBox probeCheckBox = new CheckBox(this);
            probeCheckBox.setId(id);
            
            boolean requiredProbe = false;
            if(requiredProbesSet.contains(probe)){
            	probeCheckBox.setText(probe + " (Required) for " + purposes.get(probe).toString());
            	requiredProbe = true;
            	requiredIdsList.add(id);
            } else{
            	probeCheckBox.setText(probe + " for " + purposes.get(probe).toString());
            	probeCheckBox.setOnClickListener(this);
            }

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
		               
	            	int context_label_id = settingFromServer.getInt("context_label_id");
		            settings_context_label = getContextLabel(context_label_id);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            } else {
            	probeChecked = false;
            }
            
            if(requiredProbe == true){
            	//probeChecked = true;
            	if(!probeChecked){
            		requiredSelected = false;
            	}
            }
            probeCheckBox.setChecked(probeChecked);
            probeCheckBox.setOnClickListener(this);
            checkBoxes.add(probeCheckBox);
           
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    
            probeRow.addView(probeCheckBox);  
            ll.addView(probeRow, params);
            id++;
        }
        
        TextView probeErrorMessage = new TextView(this);
        probeErrorMessage.setText("");
        probeErrorMessage.setId(id);
        probeErrorTextId = id;
        id++;
        ll.addView(probeErrorMessage);
        
        ArrayList<String> context_labels = new ArrayList<String>();
        int toHighlight = 0;
		try {
			context_labels.add("Select...");
			for(int i=0; i<contextsFromServer.length(); i++){
				String label = contextsFromServer.getJSONObject(i).getString("context_label");
	        	if(label.equalsIgnoreCase(settings_context_label))
	        		toHighlight = i;
	        	context_labels.add(label);
	        }
	        context_labels.add("Create a new context");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TextView context = new TextView(this);
		context.setPadding(0, 30, 0, 0);
        context.setText("Context");
        ll.addView(context);
        
        final Spinner spinner = new Spinner(this);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, context_labels);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setSelection(toHighlight+1); //+1 since we start with "Select..."
        spinner.setId(id);
        id++;
        spinner.setOnItemSelectedListener(this);
        ll.addView(spinner);
        
        TextView contextErrorMessage = new TextView(this);
        contextErrorMessage.setText("");
        contextErrorMessage.setId(id);
        contextErrorTextId = id;
        id++;
        ll.addView(contextErrorMessage);
        
        TextView editContext = new TextView(this);
        editContext.setText("(Edit context)");
        editContext.setId(id);
        editContextId = id;
        id++;
        editContext.setOnTouchListener(this);
        ll.addView(editContext);
        
        int saveButtonId = id;
        int cancelButtonId = saveButtonId + 1;
	    TableLayout buttonsLayout = new TableLayout(this);
        TableRow buttonsRow = new TableRow(this);
	    buttonsRow.setPadding(20,50,40,0);
	    
	    Button saveButton = new Button(this);
	    saveButton.setText( "Save");
	    saveButton.setId(saveButtonId);
	    saveButton.setOnClickListener(this);
	    
	    Button cancelButton = new Button(this);	    
	    cancelButton.setText( "Cancel");
	    cancelButton.setId(cancelButtonId);
	    cancelButton.setOnClickListener(this);
	    
	    buttonIds[0] = saveButtonId;
	    buttonIds[1] = cancelButtonId;
	    
	    buttonsRow.addView(saveButton);
	    buttonsRow.addView(cancelButton);
	    buttonsRow.setGravity(Gravity.CENTER);

	    buttonsLayout.addView(buttonsRow);
	    ll.addView(buttonsLayout);
        
	    ScrollView sv = new ScrollView(this);
	    sv.addView(ll);
	    setContentView(sv);
	    
        if(!requiredSelected){
			TextView probeError = (TextView) findViewById(probeErrorTextId);
			probeError.setText("Lab may not function as expected if required probes are not selected.");
			probeError.setTextColor(Color.parseColor("#FF0000"));
        }

        
    }
	
	@Override
	public void onClick(View v) {
		try{	
			if(v.getId() == buttonIds[0]){
				
				boolean requiredSelected = true;
				for(int ids: requiredIdsList){
					CheckBox tempCheckBox = (CheckBox) findViewById(ids);
					boolean state = tempCheckBox.isChecked() ? true : false;
					if(!state){
						requiredSelected = false;
					}
				}
				if(!requiredSelected){
					TextView probeError = (TextView) findViewById(probeErrorTextId);
					probeError.setText("Lab may not function if required probes are not selected.");
					probeError.setTextColor(Color.parseColor("#FF0000"));
				}
				
					
				if(settings_context_label != null){
					
					String probe_id_raw = "";
					String probe_id = "";
					llsiJson = new JSONObject();
					
					llsiJson.put("app_id", app_id);
					llsiJson.put("lab_id", lab_id);
					for(int i=0; i<checkBoxes.size(); i++) {
						
						
						CheckBox checkBox = checkBoxes.get(i);
						
						probe_id_raw = (String) checkBox.getText();
						probe_id = "";
						
						String[] initial_tokens = probe_id_raw.split(" for ");
						String[] tokens = initial_tokens[0].split(" ");
					
						for(int k = 0; k<tokens.length; k++){
							if(tokens[k].contains("Required"))
								continue;
							char firstLetter = Character.toLowerCase(tokens[k].charAt(0));
							probe_id += firstLetter + tokens[k].substring(1, tokens[k].length()) + "_";
						}
						probe_id = probe_id.substring(0, probe_id.length()-1);
						
					
						int checked = checkBox.isChecked() ? 1 : 0;
						llsiJson.put(probe_id, checked);
					
						llsiJson.put("settings_context_label", settings_context_label);
					}
					
					connection = new Connection(this);
					saveFlag = true;
					connection.execute(llsiJson).get(1000, TimeUnit.MILLISECONDS);
					saveFlag = false;
					finish();
				} else{
					TextView contextError = (TextView) findViewById(contextErrorTextId);
					contextError.setText("Please select a context.");
					contextError.setTextColor(Color.parseColor("#FF0000"));
				}
			} else if(v.getId() == buttonIds[1]){
				finish();
			} else if(requiredIdsList.contains(v.getId())){
				CheckBox tempCheckBox = (CheckBox) findViewById(v.getId());
				boolean state = tempCheckBox.isChecked() ? true : false;
				if(!state){
					TextView probeError = (TextView) findViewById(probeErrorTextId);
					probeError.setText("Lab may not function if required probes are not selected.");
					probeError.setTextColor(Color.parseColor("#FF0000"));
				}
			}
			
		} catch(Exception e){
			e.printStackTrace();
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
        		} else if(saveFlag){
	        		PreferencesWrapper prefs = new PreferencesWrapper(mContext);
	        		
	        		String uuid = prefs.getUUID();
	        		llsiJson.put("datastore_owner", uuid); 
	        		llsiJson.put("context_setting_flag", 1); //1 - setting
	        		String result = pds.saveAccessControlData(llsiJson);
        		} 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return null;
        }

        public Connection(Context context) {
            this.mContext = context;
        }
 
    }

	@Override
	public boolean onTouch(View v, MotionEvent arg1) { 
		if(v.getId() == editContextId){
			Intent activityIntent = new Intent(LivingLabSettingsActivity.this, LivingLabContextActivity.class);
			LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
        	activityIntent.putExtra("lab", labItem);
        	activityIntent.putExtra("contexts", contextsFromServer.toString());
        	
        	activityIntent.putExtra("context_label", settings_context_label);
			LivingLabSettingsActivity.this.startActivity(activityIntent);
		}
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		String context = arg0.getSelectedItem().toString();
		if(!context.equalsIgnoreCase("Select..."))
				settings_context_label = context;
    	if(settings_context_label != null){
	    	if(settings_context_label.equalsIgnoreCase("Create a new context")){
	    		Intent activityIntent = new Intent(LivingLabSettingsActivity.this, LivingLabContextActivity.class);
				LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
	        	activityIntent.putExtra("lab", labItem);
				LivingLabSettingsActivity.this.startActivity(activityIntent);
	    	}
    	}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
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
	

	
}