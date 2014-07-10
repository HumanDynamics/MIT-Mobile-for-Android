package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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

 
public class LivingLabGlobalSettingsActivity extends Activity implements OnClickListener, OnCheckedChangeListener {
	private static final String TAG = "LLSettingsProbesActivity";
    String settings_context_label = null;
    private JSONObject llsiJson, loadParams;
//    private JSONArray labProbes = new JSONArray();
    
    String app_id, lab_id;
    
    Set<Integer> requiredIdsList = new HashSet<Integer>();
    Map<String, Set<String>> purposes = new HashMap<String, Set<String>>();
    
	private LivingLabFunfPDS pds;
	private Connection connection;
	
	private JSONArray settingsFromServer, contextsFromServer;
	private JSONObject probesFromServer; //returns union of the probes (and their statuses) on the server.
	
	private boolean saveFlag = false;
	
	private int textId = 0;
	private int selectAllButtonId = 1;
	private int saveButtonId = 2;
	private int saveButtonTextId = 3;

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
		
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 60, 0, 0);//60dp at top
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        
        TextView labText = new TextView(this);
        labText.setText(Html.fromHtml("<h4> Global Data Settings</h4>"+ "Turn on/off data for all labs here."));
        labText.setTextSize(14);
        labText.setId(textId);
        ll.addView(labText);
        
        RelativeLayout rl = new RelativeLayout(this);
        rl.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));

		Set<String> allProbesSet = new HashSet<String>();
		
		JSONObject probesObject = null;
		try {
			probesObject = mLivingLabAccessControlDB.retrieveLivingLabProbeItem();
			
			Iterator<?> keys = probesObject.keys();

	        while(keys.hasNext()) {
	            String key = (String)keys.next();
	            allProbesSet.add(key);
	        }
	        
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		
		TableLayout tableLayout = new TableLayout(this);
		tableLayout.setColumnShrinkable(1, true);
		TableLayout.LayoutParams layoutRow = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

		TableRow tableRow = null;
		for(String probe : allProbesSet){
			
            boolean probeChecked = false;

            try {
				if(probesObject.getInt(probe) == 1){
					probeChecked = true;
				} else {
					probeChecked = false;
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
            
			tableRow = new TableRow(this);
			
	        tableRow.setLayoutParams(layoutRow);
	        
	        Switch probeButton = new Switch(this);
			int probeId = 0;
			
			//Log.v(TAG, probesMap.toString());
			try {
				//Log.v(TAG, "has? " + probesMap.has(probe));
				probeId = (Integer) ((JSONObject) probesMap.get(probe)).get("id");
				probesIds.add(probeId); //accumulate the ids
				
				//Log.v(TAG, "going to assign probeId: " + probeId);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			probeButton.setChecked(probeChecked);
			probeButton.setId(probeId);	
			probeButton.setClickable(false);
//			probeButton.setOnCheckedChangeListener(this);
			
			probeSettings.put(probe, probeChecked);
			
			tableRow.addView(probeButton, 400, 200);
			
			TableRow.LayoutParams layoutPurpose = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			TextView probePurpose = new TextView(this);
			Log.v(TAG, probe);
			String[] probe_name_parts = ((String)probe.split("_probe")[0]).split("_");
			StringBuilder probe_name = new StringBuilder();
			for(int i=0; i<probe_name_parts.length; i++){
				probe_name.append(probe_name_parts[i].substring(0,1).toUpperCase() + probe_name_parts[i].substring(1) + " ");
			}
			
	        probePurpose.setText(Html.fromHtml("<b>" + probe_name.toString() +"</b>"));
	        probePurpose.setLayoutParams(layoutPurpose);
	        probePurpose.setGravity(Gravity.CENTER);
	        tableRow.setGravity(Gravity.CENTER);
	       
	        
	        
			tableRow.addView(probePurpose);
			tableLayout.addView(tableRow);
		}

		ll.addView(tableLayout);
        
	    TableLayout buttonsLayout = new TableLayout(this);
	    buttonsLayout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT));
        
        TableRow buttonsRow = new TableRow(this);
	    //buttonsRow.setPadding(20,50,40,0);
        buttonsRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        
        LinearLayout.LayoutParams buttonLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.weight = 1;
	    
        
        Button selectAllButton = new Button(this);
        selectAllButton.setText("De-select all data");
        selectAllButton.setId(selectAllButtonId);
        selectAllButton.setOnClickListener(this);
        buttonLayoutParams.gravity = Gravity.LEFT;
        selectAllButton.setLayoutParams(buttonLayoutParams);
        
//	    Button saveButton = new Button(this);
//	    saveButton.setText("Save");
//	    saveButton.setId(saveButtonId);
//	    saveButton.setOnClickListener(this);
//	    buttonLayoutParams.gravity = Gravity.RIGHT;
//	    saveButton.setLayoutParams(buttonLayoutParams);
	    
	    
	    buttonsRow.setWeightSum(2);
	    buttonsRow.addView(selectAllButton);
//	    buttonsRow.addView(saveButton);
	    buttonsRow.setGravity(Gravity.CENTER);

	    buttonsLayout.addView(buttonsRow);
	    ll.addView(buttonsLayout);

//        TextView nextButtonText = new TextView(this);
//        nextButtonText.setText("Click next to proceed.");
//        nextButtonText.setTextColor(Color.BLUE);
//        nextButtonText.setTextSize(12);
//        nextButtonText.setVisibility(View.GONE);
//        nextButtonText.setId(saveButtonTextId);
//        ll.addView(nextButtonText);
        
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
		            //Log.v(TAG, probeDetails.toString());
		            //Log.v(TAG, probesIds.toString());
		            
		            int probeIdValue = probeDetails.getInt("id");
		            if(probesIds.contains(probeIdValue))
		            	selectProbe(probeIdValue);
		        }
//		        TextView nextButtonTextView = (TextView) findViewById(saveButtonTextId);
//		        nextButtonTextView.setVisibility(View.VISIBLE);
		        JSONObject probesToSave = prepareProbesForDisabling(probeSettings);
		        LivingLabsAccessControlDB.saveLivingLabProbeItem(probesToSave);
		        
		        connection = new Connection(this);
				saveFlag = true;
				connection.execute(llsiJson).get(1000, TimeUnit.MILLISECONDS);
				saveFlag = false;
	        } catch(Exception e){
	        	e.printStackTrace();
	        }
			
		} else if(v.getId() == saveButtonId){
			try {
//				LivingLabsAccessControlDB.saveLivingLabProbeItem(probesFromServer);
//				LivingLabsAccessControlDB.saveLivingLabProbeItem(probesFromServer);				
				JSONObject probesToSave = prepareProbesForSave(probeSettings);
				LivingLabsAccessControlDB.saveLivingLabProbeItem(probesToSave);
				
				connection = new Connection(this);
				saveFlag = true;
				connection.execute(llsiJson).get(1000, TimeUnit.MILLISECONDS);
				saveFlag = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
			finish();
//			Intent intent = new Intent(this, LivingLabSettingsContextActivity.class);
//			LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
//			intent.putExtra("lab", labItem);
//			intent.putExtra("probeSettings", probeSettings);
//			intent.putExtra("context_label", settings_context_label);
//			intent.putExtra("contextsFromServer", contextsFromServer.toString());
//			startActivity(intent);
		}
	}
	
	public void selectProbe(int probeId){
//		Log.v(TAG, "probeId: " + probeId);
//		ToggleButton probeButton = (ToggleButton) findViewById(probeId);
		Switch probeButton = (Switch) findViewById(probeId);
		//probeButton.setChecked(true);
		probeButton.setChecked(false);
	}
	
	private class Connection extends AsyncTask<JSONObject, Object, Object> {
		 
		private Context mContext;
		
        @Override
        protected Object doInBackground(JSONObject... object) {
        	try {
        		
        		if(saveFlag){
	        		PreferencesWrapper prefs = new PreferencesWrapper(mContext);
	        		
	        		String uuid = prefs.getUUID();
	        		JSONObject accesscontrolObject = new JSONObject();
	        		accesscontrolObject.put("datastore_owner", uuid); 
//	        		accesscontrolObject.put("global_setting", true);
//	        		accesscontrolObject.put("setting_object", llsiJson); 
//	        		accesscontrolObject.put("context_object", null); 
	        		
	        		//String result = pds.saveAccessControlData(llsiJson);
//	        		pds.globalAccessControlData(accesscontrolObject);
	        		
	        		pds.accessControlData(accesscontrolObject, "global");
	        		//Log.v(TAG, "data: " + llsiJson.toString());
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
		JSONObject result = null;
		
		for(Entry<String, Boolean> probe: probesMap.entrySet()){
			result.put(probe.getKey(), probe.getValue());
		}
		return result;
	}
	
	private JSONObject prepareProbesForDisabling(Map<String, Boolean> probesMap) throws JSONException{
		JSONObject result = new JSONObject();
//		Log.v(TAG, probesMap.toString());
		for(Entry<String, Boolean> probe: probesMap.entrySet()){
			result.put(probe.getKey(), false);
		}
		return result;
	}
}