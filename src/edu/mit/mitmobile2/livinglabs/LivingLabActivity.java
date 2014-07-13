package edu.mit.mitmobile2.livinglabs;

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

public class LivingLabActivity extends NewModuleFragmentActivity {
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
			
			if (labItem.getVisualizations().size() > 1) {
				mViewPager.setVisibility(View.VISIBLE);
				mFragmentAdapter = new WebViewFragmentPagerAdapter(getSupportFragmentManager());			
							
				for (LivingLabVisualizationItem visualizationItem : labItem.getVisualizations()) {
					mFragmentAdapter.addItem(WebViewFragment.Create(pds.buildAbsoluteUrl("/visualization/" + visualizationItem.getKey()), visualizationItem.getTitle(), this, mViewPager));
					Log.v(TAG, "url: " + pds.buildAbsoluteUrl("/visualization/" + visualizationItem.getKey()));
				}
			} else if (labItem.getVisualizations().size() == 1) {
				LivingLabVisualizationItem visualizationItem = labItem.getVisualizations().get(0);
				WebViewFragment visualizationWebView = WebViewFragment.Create(pds.buildAbsoluteUrl("/visualization/" + visualizationItem.getKey()), visualizationItem.getTitle(), this, mViewPager);
				Log.v(TAG, "url: " + pds.buildAbsoluteUrl("/visualization/" + visualizationItem.getKey()));
				getSupportFragmentManager().beginTransaction().add(R.id.livingLabContainerLinearLayout, visualizationWebView, "visualizationWebView").commit();
			}
					
			mViewPager.setAdapter(mFragmentAdapter);
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
