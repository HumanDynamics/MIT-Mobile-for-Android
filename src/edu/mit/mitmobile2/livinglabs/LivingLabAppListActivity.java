package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleActivity;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.objs.LivingLabItem;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

public class LivingLabAppListActivity extends NewModuleActivity {
	private ListView mLivingLabsListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.living_labs_list);
		List<LivingLabItem> labs = new ArrayList<LivingLabItem>();
		try {
			String labsString = getString(R.string.living_labs_application_list);
			//StringReader stringReader = new StringReader(labsString);
			//stringReader.
			JSONArray labsJson = new JSONArray(labsString);
			for (int i = 0; i < labsJson.length(); i++) {
				labs.add(new LivingLabItem(labsJson.optJSONObject(i)));
			}
		} catch (JSONException e) {
			Log.e("LivingLabAppListActivity", "Error Constructing Labs List", e);
			return;
		}
		
		mLivingLabsListView = (ListView) findViewById(R.id.livingLabsListView);
		mLivingLabsListView.setAdapter(new LivingLabArrayAdapter(this, 0, labs));		
		mLivingLabsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				LivingLabItem labItem = (LivingLabItem) listView.getItemAtPosition(position);

				Intent labIntent = new Intent(LivingLabAppListActivity.this, LivingLabActivity.class);
				labIntent.putExtra("lab", labItem);
				startActivity(labIntent);
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


}
