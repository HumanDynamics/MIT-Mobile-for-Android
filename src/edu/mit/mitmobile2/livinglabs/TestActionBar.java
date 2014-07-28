package edu.mit.mitmobile2.livinglabs;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleActivity;

public class TestActionBar extends NewModuleActivity {
	private static final String TAG = "TestActionBar";
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textView = new TextView(this);
        textView.setText("Hello World!");
        setContentView(textView);
	}
	
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
		return true;
	}
	
	public void livingLabSettings(){
		Log.v(TAG, "LivingLabSettings");
	}

}
