package edu.mit.mitmobile2.livinglabs;

import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleActivity;
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

public class LivingLabsCreditsActivity extends NewModuleActivity implements OnClickListener{
	private static final String TAG = "WalkthroughActivity";
	private LivingLabFunfPDS pds;
	private int textId = 0;
	private int nextButtonId = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "In Walkthrough Activity");
		
		setContentView(R.layout.living_lab_scrollview);
	    LinearLayout ll = (LinearLayout)findViewById(R.id.livinglabLinearLayout);
		
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
//		LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 60, 0, 0);//60dp at top
//        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        ll.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.FILL_PARENT, ScrollView.LayoutParams.FILL_PARENT));
        
        TextView labText = new TextView(this);
//        labText.setText(Html.fromHtml("<h3>Welcome to MIT Living Lab!</h3><br/><br/><b>" + labItem.getName() + "</b>" + 
//        		" is going to collect data from your phone and display visualizations based on the data. <br/><br/>" +
//        		"In this walkthrough, you will be able to specify what data can be collected."));
        
        labText.setText(Html.fromHtml("<b> " + labItem.getName() + "</b> has been developed by: " + labItem.getCredits()));
        labText.setTextSize(14);
        labText.setId(textId);
        ll.addView(labText);
        
//        Button nextButton = new Button(this);
//	    nextButton.setText( "Next");
//	    nextButton.setId(nextButtonId);
//	    nextButton.setOnClickListener(this);
//	    
//	    ll.addView(nextButton);
	    
//        ScrollView sv = new ScrollView(this);
//	    sv.addView(ll);
//        setContentView(sv);
        
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
	
	@Override
	protected NewModule getNewModule() {
		return new LivingLabsModule();
	}

	@Override
	protected boolean isScrollable() {
		return false;
	}

	@Override
	protected void onOptionSelected(String optionId) {
		
	}

	@Override
	protected boolean isModuleHomeActivity() {
		return false;
	}
	
	public void livingLabSettings(){
		Log.v(TAG, "LivingLabSettings");
	}

}
