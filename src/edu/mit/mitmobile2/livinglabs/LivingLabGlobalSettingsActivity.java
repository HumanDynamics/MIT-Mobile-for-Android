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
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
	private static final String TAG = "LLGlobalSettingsActivity";
    String settings_context_label = null;
    private JSONObject llsiJson, loadParams;
    
    String app_id, lab_id;
    
    Set<Integer> requiredIdsList = new HashSet<Integer>();
    Map<String, Set<String>> purposes = new HashMap<String, Set<String>>();
    
	private LivingLabFunfPDS pds;
	private Connection connection;
	
	private JSONArray settingsFromServer, contextsFromServer;
	private JSONObject probesFromServer; //returns union of the probes (and their statuses) on the server.
	
	private boolean saveFlag = false;
	
	private int textId = 0;
	private int deselectAllButtonId = 1;
	private int selectAllRequiredButtonId = 2;
	private int selectAllButtonId = 3;

	private JSONObject probesMap = new JSONObject();
	private ArrayList<Integer> probesIds = new ArrayList<Integer>();

	private HashMap<String, Boolean> probeSettings = new HashMap<String, Boolean>();
	
	private LivingLabsAccessControlDB mLivingLabAccessControlDB;
	
	private JSONObject mllpiValues = new JSONObject();
	
	
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
				e1.printStackTrace();
			}
            
            
			tableRow = new TableRow(this);
			
	        tableRow.setLayoutParams(layoutRow);
	        
	        Switch probeButton = new Switch(this);
			int probeId = 0;

			try {
				probeId = (Integer) ((JSONObject) probesMap.get(probe)).get("id");
				probesIds.add(probeId); //accumulate the ids
			} catch (JSONException e) {
				e.printStackTrace();
			}

			probeButton.setChecked(probeChecked);
			probeButton.setId(probeId);	
			probeButton.setClickable(false);
			
			probeSettings.put(probe, probeChecked);
			
			tableRow.addView(probeButton, 400, 200);
			
			TableRow.LayoutParams layoutPurpose = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			TextView probePurpose = new TextView(this);
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
        buttonLayoutParams.gravity = Gravity.LEFT;
	    
        
        Button deselectAllButton = new Button(this);
        deselectAllButton.setText("De-select all data");
        deselectAllButton.setId(deselectAllButtonId);
        deselectAllButton.setOnClickListener(this);
        deselectAllButton.setLayoutParams(buttonLayoutParams);
        
        Button selectAllRequiredButton = new Button(this);
        selectAllRequiredButton.setText("Select required data");
        selectAllRequiredButton.setId(selectAllRequiredButtonId);
        selectAllRequiredButton.setOnClickListener(this);
        selectAllRequiredButton.setLayoutParams(buttonLayoutParams);
        
        Button selectAllButton = new Button(this);
        selectAllButton.setText("Select all data");
        selectAllButton.setId(selectAllButtonId);
        selectAllButton.setOnClickListener(this);
        selectAllButton.setLayoutParams(buttonLayoutParams);
	    
	    buttonsRow.setWeightSum(3);
	    buttonsRow.addView(deselectAllButton);
	    buttonsRow.addView(selectAllRequiredButton);
	    buttonsRow.addView(selectAllButton);
	    buttonsRow.setGravity(Gravity.CENTER);

	    buttonsLayout.addView(buttonsRow);
	    ll.addView(buttonsLayout);
        
	    ScrollView sv = new ScrollView(this);
	    sv.addView(ll);
	    setContentView(sv);
	    
	    View textIdView = findViewById(textId);
	    View rootView = textIdView.getRootView();
	    rootView.setBackgroundColor(getResources().getColor(android.R.color.white));
  
    }
	
	@Override
	public void onClick(View v) {
		if(v.getId() == deselectAllButtonId){
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("De-Selecting Global Settings");

			alertDialogBuilder
			    .setMessage("You will not be collecting any data for the app. Continue or Cancel?")
			    .setCancelable(false)
			    .setPositiveButton("Continue",new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
						try{
							Iterator<String> keys = probesMap.keys();
					        while(keys.hasNext()){
					            JSONObject probeDetails = probesMap.getJSONObject(keys.next());
					            
					            int probeIdValue = probeDetails.getInt("id");
					            if(probesIds.contains(probeIdValue))
					            	selectProbe(probeIdValue, false);
					        }
					        mllpiValues = LivingLabsAccessControlDB.saveLivingLabProbeItem("false");
					        Log.v(TAG, mllpiValues.toString());
					        
					        connection = new Connection(LivingLabGlobalSettingsActivity.this);
							saveFlag = true;
							connection.execute(llsiJson).get(1000, TimeUnit.MILLISECONDS);
							saveFlag = false;
				        } catch(Exception e){
				        	e.printStackTrace();
				        }
					}
			    })
				.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});
	 
				AlertDialog alertDialog = alertDialogBuilder.create();
	 
				alertDialog.show();
					
			
		} else if(v.getId() == selectAllRequiredButtonId){
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Selecting all required Global Settings");

			alertDialogBuilder
			    .setMessage("You will be collecting only data required by the labs installed on your app. Continue or Cancel?")
			    .setCancelable(false)
			    .setPositiveButton("Continue",new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
						try{
							Iterator<String> keys = probesMap.keys();
					        while(keys.hasNext()){
					            JSONObject probeDetails = probesMap.getJSONObject(keys.next());
					            
					            int probeIdValue = probeDetails.getInt("id");
					            //need to select only the required probes
					            if(probesIds.contains(probeIdValue))
					            	selectProbe(probeIdValue, false);
					        }
					        mllpiValues = LivingLabsAccessControlDB.saveLivingLabProbeItem(null); //akin to settings
					        
					        connection = new Connection(LivingLabGlobalSettingsActivity.this);
							saveFlag = true;
							connection.execute(llsiJson).get(1000, TimeUnit.MILLISECONDS);
							saveFlag = false;
				        } catch(Exception e){
				        	e.printStackTrace();
				        }
					}
			    })
				.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});
	 
				AlertDialog alertDialog = alertDialogBuilder.create();
	 
				alertDialog.show();
					
			
		} else if(v.getId() == selectAllButtonId){
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Selecting All Global Settings");

			alertDialogBuilder
			    .setMessage("You will be collecting all data (even if not required by any installed lab). Continue or cancel?")
			    .setCancelable(false)
			    .setPositiveButton("Continue",new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
						try{
							Iterator<String> keys = probesMap.keys();
					        while(keys.hasNext()){
					            JSONObject probeDetails = probesMap.getJSONObject(keys.next());
					           
					            int probeIdValue = probeDetails.getInt("id");
					            if(probesIds.contains(probeIdValue))
					            	selectProbe(probeIdValue, true);
					        }
//					        JSONObject probesToSave = prepareProbes(probeSettings, "true");
					        mllpiValues = LivingLabsAccessControlDB.saveLivingLabProbeItem("true");
					        
					        connection = new Connection(LivingLabGlobalSettingsActivity.this);
							saveFlag = true;
							connection.execute(llsiJson).get(1000, TimeUnit.MILLISECONDS);
							saveFlag = false;
				        } catch(Exception e){
				        	e.printStackTrace();
				        }
					}
			    })
				.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				});
	 
				AlertDialog alertDialog = alertDialogBuilder.create();
	 
				alertDialog.show();

		}
	}
	
	public void selectProbe(int probeId, boolean flag){
		Switch probeButton = (Switch) findViewById(probeId);
		
		if(flag)
			probeButton.setChecked(true);
		else
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
	        		accesscontrolObject.put("probes", mllpiValues);

	        		pds.accessControlData(accesscontrolObject, "global");
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
}