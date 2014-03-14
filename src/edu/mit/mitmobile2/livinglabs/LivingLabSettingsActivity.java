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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

 
public class LivingLabSettingsActivity extends Activity implements OnClickListener {
	private static final String TAG = "LLSettingsActivity";
    private Map<CheckBox, ArrayList<CheckBox>> checkBoxes = new HashMap<CheckBox, ArrayList<CheckBox>>();
    private int[] buttonIds = new int[2];
    private JSONObject llsiJson;
    
    private LivingLabsSettingsDB mLivingLabSettingsDB;
    Map<String, ArrayList<String>> checkBoxCollection = new HashMap<String, ArrayList<String>>();
    
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
			ArrayList<String> probes = new ArrayList<String>();
			try {
				JSONArray dataArr = new JSONArray(visualizationItem.getData());
				for(int j=0; j<dataArr.length(); j++){
					String tempData = dataArr.getString(j);
					if(tempData.contains("Probe")){
						probes.add(tempData);
					}						
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			checkBoxCollection.put(title, probes);
        }
        
        int checkBoxId = 0;

        for(Map.Entry<String, ArrayList<String>> entry : checkBoxCollection.entrySet()) {
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
            
            ArrayList<String> probesCollection = entry.getValue();
            ArrayList<CheckBox> probeCheckBoxes = new ArrayList<CheckBox>();
            for(int i=0; i<probesCollection.size(); i++){
                TableRow probeRow = new TableRow(this);
                probeRow.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
                CheckBox probeCheckBox = new CheckBox(this);
                probeCheckBox.setId(checkBoxId);
                probeCheckBox.setText(probesCollection.get(i));
                probeCheckBox.setOnClickListener(this);
                
                boolean probeChecked = false;
                
                if(llsiFetched != null){
                	int probeValue = 0;
                	if(probesCollection.get(i).equalsIgnoreCase("Activity Probe"))
                		probeValue = llsiFetched.getActivityProbe();
                	else if(probesCollection.get(i).equalsIgnoreCase("Sms Probe"))
                		probeValue = llsiFetched.getSMSProbe();
                	else if(probesCollection.get(i).equalsIgnoreCase("Call Log Probe"))
                		probeValue = llsiFetched.getCallLogProbe();
                	else if(probesCollection.get(i).equalsIgnoreCase("Bluetooth Probe"))
                		probeValue = llsiFetched.getBluetoothProbe();
                	else if(probesCollection.get(i).equalsIgnoreCase("Wifi Probe"))
                		probeValue = llsiFetched.getWifiProbe();
                	else if(probesCollection.get(i).equalsIgnoreCase("Simple Location Probe"))
                		probeValue = llsiFetched.getSimpleLocationProbe();
                	else if(probesCollection.get(i).equalsIgnoreCase("Screen Probe"))
                		probeValue = llsiFetched.getScreenProbe();
                	else if(probesCollection.get(i).equalsIgnoreCase("Running Applications Probe"))
                		probeValue = llsiFetched.getRunningApplicationsProbe();
                	else if(probesCollection.get(i).equalsIgnoreCase("Hardware Info Probe"))
                		probeValue = llsiFetched.getHardwareInfoProbe();
                	else if(probesCollection.get(i).equalsIgnoreCase("App Usage Probe"))
                		probeValue = llsiFetched.getAppUsageProbe();
                	probeChecked = probeValue == 1 ? true : false;
                }
                
                probeCheckBox.setChecked(probeChecked);
                
                probeCheckBoxes.add(probeCheckBox);
                
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 75;
                
                probeRow.addView(probeCheckBox);  
                ll.addView(probeRow, params);
                checkBoxId++;
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
        
        setContentView(ll);        
        
    }

	@Override
	public void onClick(View v) {
		try{	
			boolean isServiceCheckBoxId = false;		
			CheckBox serviceCheckBox = null;
			for(Map.Entry<CheckBox, ArrayList<CheckBox>> entry : checkBoxes.entrySet()){
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
				ArrayList<CheckBox> probeCheckBoxes = checkBoxes.get(serviceCheckBox);
				boolean checked = serviceCheckBox.isChecked();
		
				for(int i=0; i<probeCheckBoxes.size(); i++){
					CheckBox probeCheckBox = probeCheckBoxes.get(i);
					if(checked){
						serviceCheckBox.setChecked(true);
						probeCheckBox.setChecked(true);
					}
					else{
						serviceCheckBox.setChecked(false);
						probeCheckBox.setChecked(false);
					}
				}
			
			} else if(!isButton){
				
				int otherProbeCount = 0;
				CheckBox probeCheckBox = null;
				outer: for(Map.Entry<CheckBox, ArrayList<CheckBox>> entry : checkBoxes.entrySet()){
					CheckBox tempServiceCheckBox = entry.getKey();
					ArrayList<CheckBox> probeCheckBoxList = entry.getValue();
					for(int i=0; i<probeCheckBoxList.size(); i++){
						if(v.getId() == probeCheckBoxList.get(i).getId()){
							serviceCheckBox = tempServiceCheckBox;
							otherProbeCount = checkBoxes.get(tempServiceCheckBox).size();
							probeCheckBox = probeCheckBoxList.get(i);
							probeCheckBox.setChecked(probeCheckBox.isChecked());
							checkBoxes.get(entry.getKey()).get(i).setChecked(probeCheckBox.isChecked());
							break outer;
						}
					}
				}
			
				boolean allOtherProbesChecked = checkAllOtherProbes(serviceCheckBox, v.getId(), probeCheckBox.isChecked());
				if((probeCheckBox.isChecked() != serviceCheckBox.isChecked()) && (allOtherProbesChecked || otherProbeCount == 1)){
					serviceCheckBox.setChecked(!serviceCheckBox.isChecked());
				}

			} else { //save,cancel buttons
				if(v.getId() == buttonIds[0]){
					String service_id = "";
					LivingLabSettingItem llsi = null;
					
					
					for(Entry<CheckBox, ArrayList<CheckBox>> entry : checkBoxes.entrySet()) {
						llsiJson = new JSONObject();
						service_id = (String) entry.getKey().getText();
					
						llsiJson.put("app_id", app_id);
						llsiJson.put("lab_id", lab_id);
						llsiJson.put("service_id", service_id);
					
						int enabled = entry.getKey().isChecked() ? 1 : 0;
						llsiJson.put("enabled", enabled);
					
						ArrayList<CheckBox> tempProbeCheckBoxes = entry.getValue();
						String probe_id_raw = "", probe_id = "";
						for(int i=0; i<tempProbeCheckBoxes.size(); i++){
							probe_id_raw = (String) tempProbeCheckBoxes.get(i).getText();
							probe_id = "";
							String[] tokens = probe_id_raw.split(" ");
						
							for(int j = 0; j<tokens.length; j++){
								char firstLetter = Character.toLowerCase(tokens[j].charAt(0));
								probe_id += firstLetter + tokens[j].substring(1, tokens[j].length()) + "_";
							}
							probe_id = probe_id.substring(0, probe_id.length()-1);
						
							int checked = tempProbeCheckBoxes.get(i).isChecked() ? 1 : 0;
							llsiJson.put(probe_id, checked);
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
			
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean checkAllOtherProbes(CheckBox serviceCheckBox, int viewId, boolean checked){
		boolean result = checked;
		ArrayList<CheckBox> probeCheckBoxes = checkBoxes.get(serviceCheckBox);
		for(int i=0; i<probeCheckBoxes.size(); i++){
			CheckBox otherProbeCheckBox = probeCheckBoxes.get(i);
			if(otherProbeCheckBox.getId() != viewId){
				if(checked){
					if(!otherProbeCheckBox.isChecked()){
						result = false;
						break;
					}
				} else {
					if(otherProbeCheckBox.isChecked()){
						result = true;
						break;
					}
				}
			}
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