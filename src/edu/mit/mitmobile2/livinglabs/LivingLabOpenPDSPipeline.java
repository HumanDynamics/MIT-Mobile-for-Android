package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.pipeline.Pipeline;
import edu.mit.media.openpds.client.funf.OpenPDSPipeline;
import edu.mit.mitmobile2.objs.LivingLabSettingItem;

public class LivingLabOpenPDSPipeline extends OpenPDSPipeline {

	private static final String TAG = "LivingLabOpenPDSPipeline";
	
	@Override
	public void onCreate(FunfManager manager) {
		this.manager = manager;
		this.gson = manager.getGsonBuilder().create();
		
		// Clean up the data requests based 
		data = filterDataRequests(data,  getEnabledProbes());
		
		super.onCreate(manager);
	}

	@Override
	public void updatePipelines() {
		try{
			LivingLabFunfPDS pds = new LivingLabFunfPDS(manager);	
			Map<String, Pipeline> pipelines = pds.getPipelines();

			for (String name : pipelines.keySet()) {
				manager.registerPipeline(name, pipelines.get(name));
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private HashSet<String> getEnabledProbes() {
		LivingLabsAccessControlDB livingLabSettingsDB = LivingLabsAccessControlDB.getInstance(manager);
		HashSet<String> probesToEnable = new HashSet<String>();
		try {
			JSONObject probesObject = livingLabSettingsDB.retrieveLivingLabProbeItem();
	        Iterator<String> keys = probesObject.keys();

	        while(keys.hasNext()){
	        	String probeName = keys.next().toString();
	        	if (LivingLabsAccessControlDB.PROBE_MAPPING.containsKey(probeName)) {
	        		probesToEnable.add(LivingLabsAccessControlDB.PROBE_MAPPING.get(probeName).getCanonicalName());
	        	}
	        }
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return probesToEnable;	
	}
	
	private static List<JsonElement> filterDataRequests(List<JsonElement> data, HashSet<String> enabledProbes) {
		ArrayList<JsonElement> filteredData = new ArrayList<JsonElement>();
		for (JsonElement dataRequest : data) {
			JsonObject dataRequestJsonObject = dataRequest.getAsJsonObject();
			if (enabledProbes.contains(dataRequestJsonObject.get("@type").getAsString())) {
				filteredData.add(dataRequest);
			}
		}
		
		return filteredData;
	}
}