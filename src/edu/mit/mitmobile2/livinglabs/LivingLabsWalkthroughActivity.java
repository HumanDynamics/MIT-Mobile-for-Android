package edu.mit.mitmobile2.livinglabs;

import edu.mit.mitmobile2.objs.LivingLabItem;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class LivingLabsWalkthroughActivity extends Activity implements OnClickListener{
	private static final String TAG = "WalkthroughActivity";
	private LivingLabFunfPDS pds;
	private int nextButtonId = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "In Walkthrough Activity");
		
		LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
		
		//If user is not logged in, redirect to touchstone
		try {
			pds = new LivingLabFunfPDS(this);
		} catch (Exception e) {
			Intent intent = new Intent(this, LivingLabsLoginActivity.class);
			startActivity(intent);
			finish();
			return;
		} 		
		LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 60, 0, 0);//60dp at top
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        
        TextView labText = new TextView(this);
        labText.setText("Welcome to MIT Living Lab! Below is a screeshot of how the " + labItem.getName() + " lab will eventually look like.");
        labText.setTextSize(14);
        ll.addView(labText);
        
        
        ImageView labScreenshot = new ImageView(this);
        labScreenshot.setImageDrawable(getResources().getDrawable(R.drawable.livinglab_activity_probe)); //placeholder image for now.
        labScreenshot.setVisibility(View.VISIBLE);
        ll.addView(labScreenshot);
        
        Button nextButton = new Button(this);
	    nextButton.setText( "Next");
	    nextButton.setId(nextButtonId);
	    nextButton.setOnClickListener(this);
	    
	    ll.addView(nextButton);
	    
        ScrollView sv = new ScrollView(this);
	    sv.addView(ll);
        setContentView(sv);
	}

	@Override
	public void onClick(View arg0) {
		if(arg0.getId() == nextButtonId){
			Intent intent = new Intent(this, LivingLabSettingsProbesActivity.class);
			LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
			intent.putExtra("lab", labItem);
			startActivity(intent);
			finish(); //use noHistory??
		}
	}
	
	@Override
	public void onBackPressed() {
	}

}
