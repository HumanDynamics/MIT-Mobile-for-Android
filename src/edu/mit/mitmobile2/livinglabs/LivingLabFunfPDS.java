package edu.mit.mitmobile2.livinglabs;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import android.content.Context;
import android.util.Log;
import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.pipeline.Pipeline;
import edu.mit.media.funf.util.LogUtil;
import edu.mit.media.openpds.client.funf.FunfPDS;
import edu.mit.media.openpds.client.funf.OpenPDSPipeline;
import edu.mit.mitmobile2.objs.LivingLabSettingItem;

public class LivingLabFunfPDS extends FunfPDS {
	
	
	private static final String TAG = "LivingLabFunfPDS";
	private LivingLabsSettingsDB mLivingLabSettingsDB = LivingLabsSettingsDB.getInstance(getContext());

	public LivingLabFunfPDS(Context context) throws Exception {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Map<String, Pipeline> getPipelines() { 
		Map<String, Pipeline> pipelines = new HashMap<String, Pipeline>();
		JsonArray pipelinesJsonArray = getPipelinesJsonArray();		
		
		ArrayList<String> probesToEnable = new ArrayList<String>();
		ArrayList<LivingLabSettingItem> llsiArray = mLivingLabSettingsDB.retrieveLivingLabSettingItem();
		for(int i=0; i<llsiArray.size(); i++){
			LivingLabSettingItem llsi = llsiArray.get(i);
			
			if (llsi.getActivityProbe() == 1) probesToEnable.add("ActivityProbe");
			if (llsi.getSMSProbe() == 1) probesToEnable.add("SmsProbe");
			if (llsi.getCallLogProbe() == 1) probesToEnable.add("CallLogProbe");
			if (llsi.getBluetoothProbe() == 1) probesToEnable.add("BluetoothProbe");
			if (llsi.getWifiProbe() == 1) probesToEnable.add("WifiProbe");
			if (llsi.getSimpleLocationProbe() == 1) probesToEnable.add("SimpleLocationProbe");
			if (llsi.getScreenProbe() == 1) probesToEnable.add("ScreenProbe");
			if (llsi.getRunningApplicationsProbe() == 1) probesToEnable.add("RunningApplicationsProbe");
			if (llsi.getHardwareInfoProbe() == 1) probesToEnable.add("HardwareInfoProbe");
			if (llsi.getAppUsageProbe() == 1) probesToEnable.add("AppUsageProbe");
		}
		
		if (pipelinesJsonArray != null) {
			Gson gson = FunfManager.getGsonBuilder(getContext()).create();
			for (JsonElement pipelineJsonElement : getPipelinesJsonArray()) {
				try {
					JsonObject pipelineJsonObject = pipelineJsonElement.getAsJsonObject();
					if (pipelineJsonObject.has("name") && pipelineJsonObject.has("config")) {
						
						JsonObject pipelineConfig = pipelineJsonObject.get("config").getAsJsonObject();
						JsonObject newPipelineConfig = new JsonObject();
						
				    	JsonArray finalProbesArray = new JsonArray();
						for (Map.Entry<String,JsonElement> entry : pipelineConfig.entrySet()) {
						    if(!entry.getKey().equalsIgnoreCase("data")){
						    	newPipelineConfig.add(entry.getKey(), entry.getValue());
						    } else {
						    	JsonArray probesArray = entry.getValue().getAsJsonArray();
						    	for(int i=0; i<probesArray.size(); i++){
						    		JsonObject tempProbe = probesArray.get(i).getAsJsonObject();
						    		
						    		boolean probePresent = false;
						    		for(int j=0; j<probesToEnable.size(); j++){
						    			if(tempProbe.get("@type").toString().contains(probesToEnable.get(j).toString())){
						    				probePresent = true;
						    			}
						    		}
						    		if(probePresent){
						    			finalProbesArray.add(tempProbe);
						    		}
						    	}
						    	newPipelineConfig.add(entry.getKey(), finalProbesArray);
						    }
						}
						
						
						Pipeline pipeline = gson.fromJson(pipelineJsonObject.get("config"), OpenPDSPipeline.class);
						pipelines.put(pipelineJsonObject.get("name").getAsString(), pipeline);
					}
				} catch (Exception e) {
					Log.w(LogUtil.TAG, "Error creating pipelines from PDS configs", e);
				}
			}
		}
		
		return pipelines;
	}
	
	@Override
	public String getFunfUploadUrl() {
		return buildAbsoluteApiUrl("/accesscontrol/store/");
	}
	
	public String uploadFunfData(JSONObject object) throws ClientProtocolException, IOException {          
	    HttpPost httppost = new HttpPost(getFunfUploadUrl());

		StringEntity entity = new StringEntity(object.toString(), "UTF-8");
		entity.setContentType("application/json;charset=UTF-8");//text/plain;charset=UTF-8
		entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
		httppost.setEntity(entity); 
		DefaultHttpClient httpClient = new DefaultHttpClient();

		ResponseHandler<String> responseHandler=new BasicResponseHandler();
		String response = httpClient.execute(httppost, responseHandler); 
		
		return response;
	
	}

}
