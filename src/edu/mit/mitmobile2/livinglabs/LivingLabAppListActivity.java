package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleActivity;
import edu.mit.mitmobile2.livinglabs.R;
import edu.mit.mitmobile2.objs.LivingLabItem;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

public class LivingLabAppListActivity extends NewModuleActivity {
	private static final String TAG = "LivingLabAppListActivity";
	private ListView mLivingLabsListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.living_labs_list);
		List<LivingLabItem> labs = new ArrayList<LivingLabItem>();
		try {
			String labsString = getString(R.string.living_labs_application_list);
			JSONArray labsJson = new JSONArray(labsString);
			for (int i = 0; i < labsJson.length(); i++) {
				labs.add(new LivingLabItem(labsJson.optJSONObject(i)));
			}
		} catch (JSONException e) {
			Log.e("LivingLabAppListActivity", "Error Constructing Labs List", e);
			return;
		}
		
		LinearLayout layout = new LinearLayout(this);
		
		mLivingLabsListView = (ListView) findViewById(R.id.livingLabsListView);
		mLivingLabsListView.setAdapter(new LivingLabArrayAdapter(this, R.layout.living_lab_row, R.id.livingLabRowTitle, labs));		
		mLivingLabsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				LivingLabItem labItem = (LivingLabItem) listView.getItemAtPosition(position);
				
				Log.v(TAG,"position is: " + position);
				
				if(labItem.getName().equalsIgnoreCase("settings")){
					labItem = (LivingLabItem) listView.getItemAtPosition(0);
					Intent labIntent = new Intent(LivingLabAppListActivity.this, LivingLabSettingsActivity.class);
					labIntent.putExtra("lab", labItem);
					startActivity(labIntent);
				} else {

				
					Intent labIntent = new Intent(LivingLabAppListActivity.this, LivingLabActivity.class);
					labIntent.putExtra("lab", labItem);
					startActivity(labIntent);
				
				}
			}
		});

	}
	
	@Override
	public boolean isModuleHomeActivity() {
		return true;
	}
	
	@Override
	protected NewModule getNewModule() {
		// TODO Auto-generated method stub
		return new LivingLabsModule();
	}
	@Override
	protected boolean isScrollable() {
		return false;
	}
	@Override
	protected void onOptionSelected(String optionId) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.layout.living_lab_menu, menu);
		return true;
	}

	public void livingLabSettings(){
		Log.v(TAG, "LivingLabSettings");
	}

}
