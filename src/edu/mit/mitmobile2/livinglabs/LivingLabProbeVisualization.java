package edu.mit.mitmobile2.livinglabs;

import org.json.JSONArray;
import org.json.JSONException;

import edu.mit.media.openpds.client.PersonalDataStore;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleFragmentActivity;
import edu.mit.mitmobile2.livinglabs.gfsa.R;
import edu.mit.mitmobile2.objs.LivingLabItem;
import edu.mit.mitmobile2.objs.LivingLabVisualizationItem;
import edu.mit.mitmobile2.touchstone.TouchstonePrefsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

public class LivingLabProbeVisualization extends NewModuleFragmentActivity {
	private static final String TAG = "LivingLabActivity";
	
	private ViewPager mViewPager;
	private WebViewFragmentPagerAdapter mFragmentAdapter;	
	private WebViewFragment mWebViewFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.living_lab_container);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		
		
		if (mFragmentAdapter == null && mWebViewFragment == null) {
			PersonalDataStore pds;
			try {
				pds = new PersonalDataStore(this);
			} catch (Exception e) {
				Intent intent = new Intent(this, LivingLabsLoginActivity.class);
				startActivity(intent);
				finish();
				return;
			}
			LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
			String labName = getIntent().getSerializableExtra("lab").toString();
			
			JSONArray labProbes;
			try {
				Log.v(TAG, getIntent().getSerializableExtra("probes").toString());
				labProbes = new JSONArray(getIntent().getSerializableExtra("probes").toString());
				if (labProbes.length() > 1) {
					mViewPager.setVisibility(View.VISIBLE);
					mFragmentAdapter = new WebViewFragmentPagerAdapter(getSupportFragmentManager());			
								
					for (int i=0; i<labProbes.length(); i++) {
						try {
							String url = pds.buildAbsoluteUrl("/probevisualization/");
							url += "&probe=" + labProbes.getString(i)+"&lab=" + labName;
							mFragmentAdapter.addItem(WebViewFragment.Create(url, labName, this, mViewPager));
							Log.v(TAG, "url: " + url);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				} else if (labProbes.length() == 1) {
					//LivingLabVisualizationItem visualizationItem = labItem.getVisualizations().get(0);
					WebViewFragment visualizationWebView = null;
					try {
						String url = pds.buildAbsoluteUrl("/probevisualization/");
						url += "&probe=" + labProbes.getString(0)+"&lab=" + labName;
						visualizationWebView = WebViewFragment.Create(url, labName, this, mViewPager);
						Log.v(TAG, "url: " + url);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					getSupportFragmentManager().beginTransaction().add(R.id.livingLabContainerLinearLayout, visualizationWebView, "visualizationWebView").commit();
				}
						
				mViewPager.setAdapter(mFragmentAdapter);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			

		}
		
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
		return false;
	}

}
