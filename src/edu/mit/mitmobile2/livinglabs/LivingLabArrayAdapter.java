package edu.mit.mitmobile2.livinglabs;

import java.util.List;

import edu.mit.mitmobile2.TwoLineActionRow;
import edu.mit.mitmobile2.livinglabs.gfsa.R;
import edu.mit.mitmobile2.objs.LivingLabItem;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class LivingLabArrayAdapter extends ArrayAdapter<LivingLabItem> {

	private LayoutInflater mLayoutInflater;
	SharedPreferences isFirstVisitOfLab;
	private Context context;
	
	public LivingLabArrayAdapter(Context context, int resource, int textViewResourceId, List<LivingLabItem> objects) {
		super(context, resource, textViewResourceId, objects);
		this.context = context;
		mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final LivingLabItem labItem = getItem(position);
				
		if (convertView == null) {
			// Create new View
			convertView = mLayoutInflater.inflate(R.layout.living_lab_row, parent, false);
		}
		
		TextView titleTextView = (TextView) convertView.findViewById(R.id.livingLabRowTitle);
		ImageButton settingsButton = (ImageButton) convertView.findViewById(R.id.livingLabRowButton);
		
		// Populate pre-existing view with content;
		titleTextView.setText(labItem.getName());
		settingsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isFirstVisitOfLab = PreferenceManager.getDefaultSharedPreferences(context);
				boolean firstVisitOfLabFlag = isFirstVisitOfLab.getBoolean(labItem.getName(), false);
				Intent labIntent = null;
				
				if(firstVisitOfLabFlag){
					SharedPreferences.Editor editor = isFirstVisitOfLab.edit();
				    editor.putBoolean(labItem.getName(), false);
				    editor.commit();
					labIntent = new Intent(getContext(), LivingLabsWalkthroughActivity.class);
				} else {
					labIntent = new Intent(getContext(), LivingLabSettingsProbesActivity.class);
				}
				labIntent.putExtra("lab", labItem);
				getContext().startActivity(labIntent);
			}
		});
		
		settingsButton.setVisibility(View.GONE);
		
		return convertView;
	}

}
