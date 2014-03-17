package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import edu.mit.mitmobile2.livinglabs.LivingLabAppListActivity;
import edu.mit.mitmobile2.MITMenuItem;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.livinglabs.R;

public class LivingLabsModule extends NewModule {

	@Override
	public String getLongName() {
		return "MIT Living Labs";
	}

	@Override
	public String getShortName() {
		return "Living Labs";
	}

	@Override
	public Class<? extends Activity> getModuleHomeActivity() {
		return LivingLabAppListActivity.class;
	}


	@Override
	public int getMenuIconResourceId() {
		return R.drawable.menu_directory;
	}

	@Override
	public int getHomeIconResourceId() {
		return R.drawable.home_people;
	}

	@Override
	public List<MITMenuItem> getPrimaryOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MITMenuItem> getSecondaryOptions() {
		// TODO Auto-generated method stub
		return new ArrayList<MITMenuItem>();
	}

	@Override
	public boolean onItemSelected(Activity activity, String id) {
		// TODO Auto-generated method stub
		return false;
	}
}
