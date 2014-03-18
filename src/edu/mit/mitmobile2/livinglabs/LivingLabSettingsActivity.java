package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.mitmobile2.objs.LivingLabItem;
import edu.mit.mitmobile2.objs.LivingLabSettingItem;
import edu.mit.mitmobile2.objs.LivingLabVisualizationItem;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

 
public class LivingLabSettingsActivity extends Activity implements OnClickListener {
	private static final String TAG = "LLSettingsActivity";
    private Map<CheckBox, ArrayList<ArrayList<CheckBox>>> checkBoxes = new HashMap<CheckBox, ArrayList<ArrayList<CheckBox>>>();
    private int[] buttonIds = new int[2];
    private JSONObject llsiJson;
    
    private LivingLabsSettingsDB mLivingLabSettingsDB;
    Map<String, ArrayList<ArrayList<String>>> checkBoxCollection = new HashMap<String, ArrayList<ArrayList<String>>>();
    
    String app_id, lab_id;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLivingLabSettingsDB = LivingLabsSettingsDB.getInstance(this);
        app_id = "Living Lab";       		
        LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
        
        ArrayList<LivingLabVisualizationItem> visualization = labItem.getVisualizations();
        lab_id = labItem.getName();
        
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 60, 0, 0);//60dp at top
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        
        TextView labText = new TextView(this);
        labText.setText(lab_id + " has access to");
        ll.addView(labText);
        
        RelativeLayout rl = new RelativeLayout(this);
        rl.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT));
        
        for(int i=0; i<visualization.size(); i++){
        	LivingLabVisualizationItem visualizationItem = visualization.get(i);
			String title = visualizationItem.getTitle();
			ArrayList<ArrayList<String>> probes = new ArrayList<ArrayList<String>>();
			try {
				JSONObject dataObject = visualizationItem.getData();
				JSONArray requiredProbesArray = dataObject.getJSONArray("required");
				JSONArray nonRequiredProbesArray = dataObject.getJSONArray("non-required");
				
				ArrayList<String> requiredProbes = new ArrayList<String>();
				ArrayList<String> nonRequiredProbes = new ArrayList<String>();
				for(int j=0; j<requiredProbesArray.length(); j++){
					String tempData = requiredProbesArray.getString(j);
					if(tempData.contains("Probe")){
						requiredProbes.add(tempData);
					}						
				}
				for(int k=0; k<nonRequiredProbesArray.length(); k++){
					String tempData = nonRequiredProbesArray.getString(k);
					if(tempData.contains("Probe")){
						nonRequiredProbes.add(tempData);
					}						
				}
				
				probes.add(requiredProbes);
				probes.add(nonRequiredProbes);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			checkBoxCollection.put(title, probes);
        }
        
        int checkBoxId = 0;
        
        for(Map.Entry<String, ArrayList<ArrayList<String>>> entry : checkBoxCollection.entrySet()) {
        	TableRow serviceRow =new TableRow(this);
            serviceRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
            CheckBox serviceCheckBox = new CheckBox(this);
            
            ArrayList<String> searchDataInput = new ArrayList<String>();
            searchDataInput.add(app_id);
            searchDataInput.add(lab_id);
            searchDataInput.add(entry.getKey());
            LivingLabSettingItem llsiFetched = mLivingLabSettingsDB.retrieveLivingLabSettingItem(searchDataInput);
            
            serviceCheckBox.setOnClickListener(this);
            serviceCheckBox.setId(checkBoxId);
            serviceCheckBox.setText(entry.getKey());
            
            boolean serviceChecked = false;
            if(llsiFetched != null)
            	serviceChecked = llsiFetched.getEnabled() == 1 ? true : false;
            serviceCheckBox.setChecked(serviceChecked);
            
            serviceRow.addView(serviceCheckBox);  
            ll.addView(serviceRow);
            
            checkBoxId++;
            
            ArrayList<ArrayList<String>> probesCollection = entry.getValue();
            ArrayList<ArrayList<CheckBox>> probeCheckBoxes = new ArrayList<ArrayList<CheckBox>>();
            
            for(int i=0; i<probesCollection.size(); i++){
            	ArrayList<String> specializedProbesCollection = probesCollection.get(i); //required and non-required
            	ArrayList<CheckBox> specializedProbeCheckBoxes = new ArrayList<CheckBox>();
            	for(int j=0; j<specializedProbesCollection.size(); j++){
            		TableRow probeRow = new TableRow(this);
                    probeRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
                    CheckBox probeCheckBox = new CheckBox(this);
                    probeCheckBox.setId(checkBoxId);
                    if(i == 0)
                    	probeCheckBox.setText(specializedProbesCollection.get(j) + " (Required)");
                    else
                    	probeCheckBox.setText(specializedProbesCollection.get(j));
                    probeCheckBox.setOnClickListener(this);
                    
                    boolean probeChecked = false;
                    
                    if(llsiFetched != null){
                    	int probeValue = 0;
                    	if(specializedProbesCollection.get(j).equalsIgnoreCase("Activity Probe"))
                    		probeValue = llsiFetched.getActivityProbe();
                    	else if(specializedProbesCollection.get(j).equalsIgnoreCase("Sms Probe"))
                    		probeValue = llsiFetched.getSMSProbe();
                    	else if(specializedProbesCollection.get(j).equalsIgnoreCase("Call Log Probe"))
                    		probeValue = llsiFetched.getCallLogProbe();
                    	else if(specializedProbesCollection.get(j).equalsIgnoreCase("Bluetooth Probe"))
                    		probeValue = llsiFetched.getBluetoothProbe();
                    	else if(specializedProbesCollection.get(j).equalsIgnoreCase("Wifi Probe"))
                    		probeValue = llsiFetched.getWifiProbe();
                    	else if(specializedProbesCollection.get(j).equalsIgnoreCase("Simple Location Probe"))
                    		probeValue = llsiFetched.getSimpleLocationProbe();
                    	else if(specializedProbesCollection.get(j).equalsIgnoreCase("Screen Probe"))
                    		probeValue = llsiFetched.getScreenProbe();
                    	else if(specializedProbesCollection.get(j).equalsIgnoreCase("Running Applications Probe"))
                    		probeValue = llsiFetched.getRunningApplicationsProbe();
                    	else if(specializedProbesCollection.get(j).equalsIgnoreCase("Hardware Info Probe"))
                    		probeValue = llsiFetched.getHardwareInfoProbe();
                    	else if(specializedProbesCollection.get(j).equalsIgnoreCase("App Usage Probe"))
                    		probeValue = llsiFetched.getAppUsageProbe();
                    	probeChecked = probeValue == 1 ? true : false;
                    }
                    
                    probeCheckBox.setChecked(probeChecked);
                    
                    specializedProbeCheckBoxes.add(probeCheckBox);
                    
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.leftMargin = 75;
                    
                    probeRow.addView(probeCheckBox);  
                    ll.addView(probeRow, params);
                    checkBoxId++;
            	}
            	
            	probeCheckBoxes.add(specializedProbeCheckBoxes);
            }
            checkBoxes.put(serviceCheckBox, probeCheckBoxes);
            
        }
        
        int saveButtonId = checkBoxId;
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

        
    }

	//3/14 7:15pm Service checkbox clicking is working! TODO: onClick::: required vs non-required checking
	@Override
	public void onClick(View v) {
		try{	
			boolean isServiceCheckBoxId = false;		
			CheckBox serviceCheckBox = null;
			for(Map.Entry<CheckBox, ArrayList<ArrayList<CheckBox>>> entry : checkBoxes.entrySet()){
				CheckBox tempServiceCheckBox = entry.getKey();
				if(v.getId() == tempServiceCheckBox.getId()){
					isServiceCheckBoxId = true;
					serviceCheckBox = tempServiceCheckBox;
				}
			}
			
			boolean isButton = false;
			if(v.getId() == buttonIds[0] || v.getId() == buttonIds[1])
				isButton = true;

			if(isServiceCheckBoxId){			
				ArrayList<ArrayList<CheckBox>> probeCheckBoxes = checkBoxes.get(serviceCheckBox);
				boolean checked = serviceCheckBox.isChecked();
		
				for(int i=0; i<probeCheckBoxes.size(); i++){
					ArrayList<CheckBox> specializedProbeCheckBoxes = probeCheckBoxes.get(i); //required and non-required
					
					for(int j=0; j<specializedProbeCheckBoxes.size(); j++){
						CheckBox probeCheckBox = specializedProbeCheckBoxes.get(j);
						if(checked){
							serviceCheckBox.setChecked(true);
							probeCheckBox.setChecked(true);
						}
						else{
							serviceCheckBox.setChecked(false);
							probeCheckBox.setChecked(false);
						}
					}
					
				}//forloop - probeCheckBoxes
			
			} else if(!isButton){
				
				int otherRequiredProbeCount = 0;
				CheckBox probeCheckBox = null;
				boolean requiredFlag = false;
				outer: for(Map.Entry<CheckBox, ArrayList<ArrayList<CheckBox>>> entry : checkBoxes.entrySet()){
					CheckBox tempServiceCheckBox = entry.getKey();
					ArrayList<ArrayList<CheckBox>> probeCheckBoxList = entry.getValue();
					for(int i=0; i<probeCheckBoxList.size(); i++){
						ArrayList<CheckBox> specializedProbeCheckBoxList = probeCheckBoxList.get(i);
						for(int j=0; j<specializedProbeCheckBoxList.size(); j++){
							if(v.getId() == specializedProbeCheckBoxList.get(j).getId()){
								serviceCheckBox = tempServiceCheckBox;
								otherRequiredProbeCount = checkBoxes.get(tempServiceCheckBox).get(0).size();
								probeCheckBox = specializedProbeCheckBoxList.get(j);
								if(probeCheckBox.getText().toString().toLowerCase().contains("required")){
									requiredFlag = true;
								}
								
								probeCheckBox.setChecked(probeCheckBox.isChecked());
								checkBoxes.get(entry.getKey()).get(i).get(j).setChecked(probeCheckBox.isChecked());
								break outer;
							}
						}
						
					}//forloop - probeCheckBoxList
				}
				
				if(requiredFlag){
					boolean allOtherRequiredProbesChecked = checkAllOtherRequiredProbes(serviceCheckBox, v.getId(), probeCheckBox.isChecked());
					if((probeCheckBox.isChecked() != serviceCheckBox.isChecked()) && (allOtherRequiredProbesChecked || otherRequiredProbeCount == 1)){
						serviceCheckBox.setChecked(!serviceCheckBox.isChecked());
					} 
				}

			} else { //save,cancel buttons
				if(v.getId() == buttonIds[0]){
					String service_id = "";
					LivingLabSettingItem llsi = null;
					
					
					for(Entry<CheckBox, ArrayList<ArrayList<CheckBox>>> entry : checkBoxes.entrySet()) {
						llsiJson = new JSONObject();
						service_id = (String) entry.getKey().getText();
					
						llsiJson.put("app_id", app_id);
						llsiJson.put("lab_id", lab_id);
						llsiJson.put("service_id", service_id);
					
						int enabled = entry.getKey().isChecked() ? 1 : 0;
						llsiJson.put("enabled", enabled);
					
						ArrayList<ArrayList<CheckBox>> tempProbeCheckBoxes = entry.getValue();
						String probe_id_raw = "", probe_id = "";
						for(int i=0; i<tempProbeCheckBoxes.size(); i++){
							
							ArrayList<CheckBox> specializedTempProbeCheckBoxes = tempProbeCheckBoxes.get(i);
							for(int j=0; j<specializedTempProbeCheckBoxes.size(); j++){
								probe_id_raw = (String) specializedTempProbeCheckBoxes.get(j).getText();
								probe_id = "";
								String[] tokens = probe_id_raw.split(" ");
							
								for(int k = 0; k<tokens.length; k++){
									if(tokens[k].contains("Required"))
										continue;
									char firstLetter = Character.toLowerCase(tokens[k].charAt(0));
									probe_id += firstLetter + tokens[k].substring(1, tokens[k].length()) + "_";
								}
								probe_id = probe_id.substring(0, probe_id.length()-1);
								
							
								int checked = specializedTempProbeCheckBoxes.get(j).isChecked() ? 1 : 0;
								llsiJson.put(probe_id, checked);
							}
							
						}
				
						llsi = new LivingLabSettingItem(llsiJson);
						mLivingLabSettingsDB.saveLivingLabSettingItem(llsi);
						Log.v(TAG, "llsi json: " + llsiJson.toString());
						Connection connection = new Connection(this);
						connection.execute(llsiJson).get(1000, TimeUnit.MILLISECONDS);
					}
				} else if(v.getId() == buttonIds[1]){
					finish();
				}
			
			} //if/elseif/else - service/probe chekboxes and buttons
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean checkAllOtherRequiredProbes(CheckBox serviceCheckBox, int viewId, boolean checked){
		boolean result = true;
		ArrayList<ArrayList<CheckBox>> probeCheckBoxes = checkBoxes.get(serviceCheckBox);
		for(int i=0; i<probeCheckBoxes.size(); i++){
			
			ArrayList<CheckBox> specializedProbeCheckBoxes = probeCheckBoxes.get(i);
			for(int j=0; j<specializedProbeCheckBoxes.size(); j++){
				CheckBox otherProbeCheckBox = specializedProbeCheckBoxes.get(j);
				
				if(otherProbeCheckBox.getText().toString().toLowerCase().contains("required")){
					if(otherProbeCheckBox.getId() != viewId){
						if(!otherProbeCheckBox.isChecked()){
							result = false;
							break;
						}
					}
				}
				
			}//inner for loop
			
		}
		return result;
	}
	
	private class Connection extends AsyncTask<JSONObject, Object, Object> {
		 
		private Context mContext;
		
        @Override
        protected Object doInBackground(JSONObject... object) {
        	try {
        		LivingLabFunfPDS llFunfPDS = new LivingLabFunfPDS(mContext);
        		
        		StringTokenizer st_uuid =  new StringTokenizer(llFunfPDS.getFunfUploadUrl(),"&");
        		st_uuid.nextToken();
        		StringTokenizer st_uuidval = new StringTokenizer(st_uuid.nextToken(),"=");
        		st_uuidval.nextToken();
        		String uuid = st_uuidval.nextToken();
        		llsiJson.put("datastore_owner", uuid); 
        		String result = llFunfPDS.uploadFunfData(llsiJson);
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

	
}