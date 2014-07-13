package edu.mit.mitmobile2;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import edu.mit.mitmobile2.livinglabs.gfsa.R;

public class PrefsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
	
}