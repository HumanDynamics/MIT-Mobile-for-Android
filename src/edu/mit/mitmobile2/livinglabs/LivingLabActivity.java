package edu.mit.mitmobile2.livinglabs;

import edu.mit.media.openpds.client.PersonalDataStore;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleFragmentActivity;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.objs.LivingLabItem;
import edu.mit.mitmobile2.objs.LivingLabVisualizationItem;
import edu.mit.mitmobile2.touchstone.TouchstonePrefsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class LivingLabActivity extends NewModuleFragmentActivity {
	private ViewPager mViewPager;
	private WebViewFragmentPagerAdapter mFragmentAdapter;	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.living_lab_container);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		
		if (mFragmentAdapter == null) {
			PersonalDataStore pds;
			try {
				pds = new PersonalDataStore(this);
			} catch (Exception e) {
				Intent intent = new Intent(this, TouchstonePrefsActivity.class);
				startActivity(intent);
				finish();
				return;
			}
			mFragmentAdapter = new WebViewFragmentPagerAdapter(getSupportFragmentManager());
			
			LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
						
			for (LivingLabVisualizationItem visualizationItem : labItem.getVisualizations()) {
				mFragmentAdapter.addItem(WebViewFragment.Create(pds.buildAbsoluteUrl("/visualization/" + visualizationItem.getKey()), visualizationItem.getTitle(), this, mViewPager));
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
