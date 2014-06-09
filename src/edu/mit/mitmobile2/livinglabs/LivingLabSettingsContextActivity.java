package edu.mit.mitmobile2.livinglabs;

import java.io.Serializable;
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
import edu.mit.mitmobile2.objs.LivingLabItem;
import edu.mit.mitmobile2.objs.LivingLabVisualizationItem;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

 
public class LivingLabSettingsContextActivity extends Activity implements OnClickListener, OnCheckedChangeListener {
	private static final String TAG = "LLSettingsContextActivity";
    String settings_context_label = null;
    private JSONObject llsiJson;
    
    private LivingLabsAccessControlDB mLivingLabAccessControlDB;
    
    String app_id, lab_id;
    
    Set<Integer> requiredIdsList = new HashSet<Integer>();
    Map<String, Set<String>> purposes = new HashMap<String, Set<String>>();
    
	private LivingLabFunfPDS pds;
	private Connection connection;
	
	private JSONArray contextsFromServer;
	
	private boolean saveFlag = false;
	
	private String context_label = "";
	
	private int finishButtonId = 1;
	private int newContextId = 2;
	
	private int contextId = 3;
	private int selectedContextId = 0;
	private int editContextButtonId = 0;
	private int contextErrorId = 0;
	
	private Map<Integer, String> contextButtonIdsNames = new HashMap<Integer, String>();
	
	private HashMap<String, Boolean> probeSettings;
	private LivingLabItem labItem = null;
	
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
        
		mLivingLabAccessControlDB = LivingLabsAccessControlDB.getInstance(this);
		
        app_id = "Living Lab"; 

        
        try {
	        labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
	        //Log.v(TAG, getIntent().getSerializableExtra("probeSettings").toString());
	        probeSettings = (HashMap<String, Boolean>) getIntent().getSerializableExtra("probeSettings");
	        
	        Serializable temp_context_label = getIntent().getSerializableExtra("context_label");
	        if(temp_context_label != null)
	        	settings_context_label = temp_context_label.toString();
	        contextsFromServer = new JSONArray(getIntent().getSerializableExtra("contextsFromServer").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        ArrayList<LivingLabVisualizationItem> visualization = labItem.getVisualizations();
        lab_id = labItem.getName();
        
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 60, 0, 0);//60dp at top
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        
        TextView labText = new TextView(this);
        labText.setText("Choose context to share selected data with " + lab_id + ". Alternatively, create new context.");
        labText.setTextSize(14);
        ll.addView(labText);
        
        RelativeLayout rl = new RelativeLayout(this);
        rl.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));

		Button newContextButton = new Button(this);
		newContextButton.setId(newContextId);
	    Spannable buttonLabel = new SpannableString(" Create New Context");
	    buttonLabel.setSpan(new ImageSpan(getApplicationContext(), R.drawable.menu_add_bookmark,ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    newContextButton.setText(buttonLabel);
	    newContextButton.setOnClickListener(this);
	    
		ll.addView(newContextButton);
		
		RadioButton[] contextRadioButtons = new RadioButton[contextsFromServer.length()];
		RadioGroup contextRadioButtonsGroup = new RadioGroup(this); 
		contextRadioButtonsGroup.setOrientation(RadioGroup.VERTICAL);

		int numContexts = contextsFromServer.length();
		editContextButtonId = contextId + numContexts;
		for(int i=0; i<numContexts; i++){
			String contextLabel = "";
			try {
				contextRadioButtons[i]  = new RadioButton(this);
				contextRadioButtonsGroup.addView(contextRadioButtons[i]); 
				contextLabel = contextsFromServer.getJSONObject(i).getString("context_label");
				Log.v(TAG, "contextLabel: " + contextLabel);
				
				if(contextLabel.equalsIgnoreCase(settings_context_label)){
					selectedContextId = contextId;
				}
				contextRadioButtons[i].setText(contextLabel);
				contextRadioButtons[i].setId(contextId);
				contextId++;
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			ImageButton editContextButton = new ImageButton(this);
			editContextButton.setImageDrawable(getResources().getDrawable(R.drawable.menu_settings));
			editContextButton.setId(editContextButtonId);
			contextButtonIdsNames.put(editContextButtonId, contextLabel);
			editContextButtonId++;
			editContextButton.setOnClickListener(this);
			contextRadioButtonsGroup.addView(editContextButton);
		}
		contextRadioButtonsGroup.check(selectedContextId);
		contextRadioButtonsGroup.setOnCheckedChangeListener(this);
		ll.addView(contextRadioButtonsGroup);
		
		contextErrorId = editContextButtonId;
        TextView contextErrorText = new TextView(this);
        contextErrorText.setText("Please select a context or create a new one.");
        contextErrorText.setTextSize(12);
        contextErrorText.setId(contextErrorId);
        contextErrorText.setTextColor(Color.RED);
        contextErrorText.setVisibility(View.GONE);
        ll.addView(contextErrorText);        
        

	    TableLayout buttonsLayout = new TableLayout(this);
        TableRow buttonsRow = new TableRow(this);
	    buttonsRow.setPadding(20,50,40,0);
	    
	    Button finishButton = new Button(this);
	    finishButton.setText("Finish");
	    finishButton.setId(finishButtonId);
	    finishButton.setOnClickListener(this);
	    
	    buttonsRow.addView(finishButton);
	    buttonsRow.setGravity(Gravity.CENTER);

	    buttonsLayout.addView(buttonsRow);
	    ll.addView(buttonsLayout);
        
	    ScrollView sv = new ScrollView(this);
	    sv.addView(ll);
	    setContentView(sv);
    }
	
	@Override
	public void onClick(View v) {
		try {
			formLlsiJson();
			
			if(v.getId() == newContextId){
				llsiJson.put("settings_context_label", null);
				
				Intent intent = new Intent(this, LivingLabContextTemporalActivity.class);
				LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
				intent.putExtra("lab", labItem);
				intent.putExtra("llsiJson", llsiJson.toString());
				intent.putExtra("probeSettings", probeSettings);
//				intent.putExtra("context_label", context_label);
				startActivity(intent);
			} else if(v.getId() == finishButtonId){
				
				if(settings_context_label == null){
					TextView contextError = (TextView) findViewById(contextErrorId);
					contextError.setVisibility(View.VISIBLE);
				} else {
					llsiJson.put("settings_context_label", settings_context_label);
					
					connection = new Connection(this);
					saveFlag = true;
					connection.execute(llsiJson).get(1000, TimeUnit.MILLISECONDS);
					saveFlag = false;
	
					//finish();
					Intent labIntent = new Intent(LivingLabSettingsContextActivity.this, LivingLabActivity.class);
					labIntent.putExtra("lab", labItem);
					startActivity(labIntent);
				}
			} else if(contextButtonIdsNames.containsKey(v.getId())){
				llsiJson.put("settings_context_label", contextButtonIdsNames.get(v.getId()));
				
				Intent intent = new Intent(this, LivingLabContextTemporalActivity.class);
				LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
				intent.putExtra("lab", labItem);
				intent.putExtra("llsiJson", llsiJson.toString());
				intent.putExtra("contextsFromServer", contextsFromServer.toString());
				intent.putExtra("probeSettings", probeSettings);
//				
				//intent.putExtra("context_label", contextButtonIdsNames.get(v.getId()));
				startActivity(intent);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void formLlsiJson() throws JSONException{
		String probe_id_raw = "";
		String probe_id = "";
		llsiJson = new JSONObject();
		
		llsiJson.put("app_id", app_id);
		llsiJson.put("lab_id", lab_id);
		for(Map.Entry<String, Boolean> probeSetting : probeSettings.entrySet()){
			probe_id_raw = probeSetting.getKey();
			Log.v(TAG, "probe: " + probe_id_raw);
			probe_id = "";
			
			String[] tokens = probe_id_raw.split(" ");
		
			for(int k = 0; k<tokens.length; k++){
				char firstLetter = Character.toLowerCase(tokens[k].charAt(0));
				probe_id += firstLetter + tokens[k].substring(1, tokens[k].length()) + "_";
			}
			probe_id = probe_id.substring(0, probe_id.length()-1);
			
		
			int checked = probeSetting.getValue() ? 1 : 0;
			llsiJson.put(probe_id, checked);
		}
	}
	
	private class Connection extends AsyncTask<JSONObject, Object, Object> {
		 
		private Context mContext;
		
        @Override
        protected Object doInBackground(JSONObject... object) {
        	try {
        		if(saveFlag){
	        		PreferencesWrapper prefs = new PreferencesWrapper(mContext);
	        		
	        		String uuid = prefs.getUUID();
	        		//llsiJson.put("datastore_owner", uuid); 
	        		//llsiJson.put("context_setting_flag", 1); //1 - setting
	        		
	        		JSONObject accesscontrolObject = new JSONObject();
	        		accesscontrolObject.put("datastore_owner", uuid); 
	        		accesscontrolObject.put("setting_object", llsiJson); 
	        		accesscontrolObject.put("context_object", null); 
	        		
	        		//String result = pds.saveAccessControlData(llsiJson);
	        		pds.saveAccessControlData(accesscontrolObject);
	        		Log.v(TAG, "data: " + llsiJson.toString());
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
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		settings_context_label = ((RadioButton) findViewById(arg1)).getText().toString();
	}
	
	@Override
	public void onBackPressed() {
	}
	
}