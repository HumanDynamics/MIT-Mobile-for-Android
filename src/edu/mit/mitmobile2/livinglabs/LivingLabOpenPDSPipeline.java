package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONException;

import com.google.android.gms.drive.DriveFolder.OnCreateFolderCallback;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import android.util.Log;
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
		
		JsonObject filteredPipelineJsonObject = filterPipelineData(gson.toJsonTree(this).getAsJsonObject(), getEnabledProbes());
		LivingLabOpenPDSPipeline filteredPipeline = (LivingLabOpenPDSPipeline) gson.fromJson(filteredPipelineJsonObject, LivingLabOpenPDSPipeline.class);
		
		for (JsonElement dataRequest : filteredPipeline.data) {
			manager.requestData(this, dataRequest);
		}
		
		for (String action : schedules.keySet()) {
			manager.registerPipelineAction(this, action, schedules.get(action));
		}
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
		ArrayList<LivingLabSettingItem> llsiArray;
		try {
			llsiArray = livingLabSettingsDB.retrieveLivingLabSettingItem();
			
			for (LivingLabSettingItem llsi : llsiArray) {
				probesToEnable.addAll(llsi.getEnabledProbes());
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return probesToEnable;	
	}
	
	private JsonObject filterPipelineData(JsonObject pipelineJsonObject, HashSet<String> enabledProbes) {
		if (pipelineJsonObject != null && pipelineJsonObject.has("name") && pipelineJsonObject.has("config")) {
			JsonObject pipelineConfig = pipelineJsonObject.getAsJsonObject("config");
			JsonObject newPipelineConfig = new JsonObject();

			JsonArray finalProbesArray = new JsonArray();
			for (Map.Entry<String,JsonElement> entry : pipelineConfig.entrySet()) {
				if(!entry.getKey().equalsIgnoreCase("data")){
					newPipelineConfig.add(entry.getKey(), entry.getValue());
				} else {
					JsonArray probesArray = entry.getValue().getAsJsonArray();
					for(int i=0; i<probesArray.size(); i++){
						JsonObject tempProbe = probesArray.get(i).getAsJsonObject();

						for (String probe : enabledProbes) {
							if (tempProbe.get("@type").toString().contains(probe)) {
								finalProbesArray.add(tempProbe);
							}
						}
					}
					newPipelineConfig.add(entry.getKey(), finalProbesArray);
				}
			}
			
			return newPipelineConfig;
		}
		return null;
	}
}
