package edu.mit.mitmobile2.livinglabs;

import edu.mit.mitmobile2.objs.LivingLabItem;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

 
public class LivingLabAccessControlActivity extends Activity implements OnClickListener {
	private static final String TAG = "LLAccessControlActivity";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent activityIntent = new Intent(LivingLabAccessControlActivity.this, LivingLabSettingsActivity.class);
    	LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
    	activityIntent.putExtra("lab", labItem);
    	LivingLabAccessControlActivity.this.startActivity(activityIntent);
	}

	@Override
	public void onClick(View arg0) {

	}
	
}