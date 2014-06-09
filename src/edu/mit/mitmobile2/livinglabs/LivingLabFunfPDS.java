package edu.mit.mitmobile2.livinglabs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
	private LivingLabsAccessControlDB mLivingLabSettingsDB = LivingLabsAccessControlDB.getInstance(getContext());
	private static Map<String, String> PROBE_MAPPING;
	static {
		PROBE_MAPPING = new HashMap<String, String>();
		PROBE_MAPPING.put("activity_probe", "ActivityProbe");
		PROBE_MAPPING.put("sms_probe", "SmsProbe");
		PROBE_MAPPING.put("call_log_probe", "CallLogProbe");
		PROBE_MAPPING.put("bluetooth_probe", "BluetoothProbe");
		PROBE_MAPPING.put("wifi_probe", "WifiProbe");
		PROBE_MAPPING.put("simple_location_probe", "SimpleLocationProbe");
		PROBE_MAPPING.put("screen_probe", "ScreenProbe");
		PROBE_MAPPING.put("running_applications_probe", "RunningApplicationsProbe");
		PROBE_MAPPING.put("hardware_info_probe", "HardwareInfoProbe");
	}

	public LivingLabFunfPDS(Context context) throws Exception {
		super(context);
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

	@Override
	protected JsonArray getPipelinesJsonArray() {
		// Overriding this here to assure that we always have a pipeline without having to hit the server
		JsonParser jsonParser = new JsonParser();
		JsonObject mainPipelineConfig = new JsonObject();
		mainPipelineConfig.addProperty("name", "MainPipeline");
		mainPipelineConfig.add("config",jsonParser.parse(getContext().getString(R.string.main_pipeline_config)).getAsJsonObject());
		JsonArray pipelinesJsonArray = new JsonArray();
		pipelinesJsonArray.add(mainPipelineConfig);
		return pipelinesJsonArray;
	}
	
	@Override
	public Map<String, Pipeline> getPipelines() { 
		Map<String, Pipeline> pipelines = new HashMap<String, Pipeline>();
		JsonArray pipelinesJsonArray = getPipelinesJsonArray();		

		HashSet<String> probesToEnable = new HashSet<String>();
		ArrayList<LivingLabSettingItem> llsiArray;
		JSONObject llpObject;
		try {
//			llsiArray = mLivingLabSettingsDB.retrieveLivingLabSettingItem();
//			
//			for (LivingLabSettingItem llsi : llsiArray) {
//				probesToEnable.addAll(llsi.getEnabledProbes());
//			}
			llpObject = mLivingLabSettingsDB.retrieveLivingLabProbeItem();
			Iterator<String> keysIterator = llpObject.keys();
			
			while (keysIterator.hasNext()) {
				String key = keysIterator.next();
				
				if (PROBE_MAPPING.containsKey(key) && llpObject.optInt(key) == 1) {
					probesToEnable.add(PROBE_MAPPING.get(key));
				}
			}
			
			Log.v(TAG, "probesToEnable: " + probesToEnable.toString());
			
			if (pipelinesJsonArray != null) {
				Gson gson = FunfManager.getGsonBuilder(getContext()).create();
				for (JsonElement pipelineJsonElement : getPipelinesJsonArray()) {
					try {
						JsonObject pipelineJsonObject = filterPipelineData(pipelineJsonElement.getAsJsonObject(), probesToEnable);
						
						if (pipelineJsonObject != null) {
							Pipeline pipeline = gson.fromJson(pipelineJsonObject.get("config"), OpenPDSPipeline.class);
							pipelines.put(pipelineJsonObject.get("name").getAsString(), pipeline);
						}
					} catch (Exception e) {
						Log.w(LogUtil.TAG, "Error creating pipelines from PDS configs", e);
					}
				}
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return pipelines;
	}

	public String getAccessControlStoreUrl() {
		return buildAbsoluteApiUrl("/accesscontrol/store/");
	}

	public String getAccessControlDeleteUrl() {
		return buildAbsoluteApiUrl("/accesscontrol/delete/");
	}
	
	public String getAccessControlLoadUrl() {
		return buildAbsoluteApiUrl("/accesscontrol/load/");
	}
	
	public String saveAccessControlData(JSONObject object) throws ClientProtocolException, IOException {          
		HttpPost httppost = new HttpPost(getAccessControlStoreUrl());

		StringEntity entity = new StringEntity(object.toString(), "UTF-8");
		entity.setContentType("application/json;charset=UTF-8");//text/plain;charset=UTF-8
		entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
		httppost.setEntity(entity); 
		DefaultHttpClient httpClient = new DefaultHttpClient();

		ResponseHandler<String> responseHandler=new BasicResponseHandler();
		String response = httpClient.execute(httppost, responseHandler); 

		return response;

	}
	
	public String loadAccessControlData(JSONObject object) throws ClientProtocolException, IOException, JSONException { 
		
		HttpPost httppost = new HttpPost(getAccessControlLoadUrl());

		StringEntity entity = new StringEntity(object.toString(), "UTF-8");
		entity.setContentType("application/json;charset=UTF-8");//text/plain;charset=UTF-8
		entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
		httppost.setEntity(entity); 
		DefaultHttpClient httpClient = new DefaultHttpClient();

		ResponseHandler<String> responseHandler=new BasicResponseHandler();
		String response = httpClient.execute(httppost, responseHandler); 
		
		return response;
	}
	
	public String deleteAccessControlData(JSONObject object) throws ClientProtocolException, IOException {          
		HttpPost httppost = new HttpPost(getAccessControlDeleteUrl());

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
