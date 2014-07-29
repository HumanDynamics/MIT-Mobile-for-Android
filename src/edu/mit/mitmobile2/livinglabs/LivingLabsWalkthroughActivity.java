package edu.mit.mitmobile2.livinglabs;

import edu.mit.mitmobile2.objs.LivingLabItem;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class LivingLabsWalkthroughActivity extends Activity implements OnClickListener{
	private static final String TAG = "WalkthroughActivity";
	private LivingLabFunfPDS pds;
	private int textId = 0;
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
        String labName = labItem.getName();
        labText.setText(Html.fromHtml("<h4>Welcome to " + labName + "!</h4>" +  labName + 
        		" uses data from your phone to create visualizations. <br/><br/>" +
        		"To create the visualiztions, you need to allow " + labName + " to collect this data. " +
        		"Then you define a time and location where the collected data can be used. " +
        		"<br/> <br/> Let's get started!"));
        labText.setTextSize(14);
        labText.setId(textId);
        ll.addView(labText);
        
        Button nextButton = new Button(this);
	    nextButton.setText( "Continue");
	    nextButton.setId(nextButtonId);
	    nextButton.setOnClickListener(this);
	    
	    ll.addView(nextButton);
	    
        ScrollView sv = new ScrollView(this);
	    sv.addView(ll);
        setContentView(sv);
        
	    View textIdView = findViewById(textId);
	    View rootView = textIdView.getRootView();
	    rootView.setBackgroundColor(getResources().getColor(android.R.color.white));
	}

	@Override
	public void onClick(View arg0) {
		if(arg0.getId() == nextButtonId){
			Intent intent = new Intent(this, LivingLabSettingsProbesActivity.class);
			LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
			intent.putExtra("lab", labItem);
			startActivity(intent);
			//finish(); //use noHistory??
		}
	}
	
//	@Override
//	public void onBackPressed() {
//	}

}
