package edu.mit.mitmobile2.livinglabs;

import java.util.Map;

import android.util.Log;
import edu.mit.media.funf.pipeline.Pipeline;
import edu.mit.media.openpds.client.funf.OpenPDSPipeline;

public class LivingLabOpenPDSPipeline extends OpenPDSPipeline{
	
	private static final String TAG = "LivingLabOpenPDSPipeline";
	
	@Override
	public void updatePipelines() {
		
		new Thread() {
			@Override
			public void run() {
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
		}.start();
	}

}
