package edu.mit.mitmobile2.livinglabs;

import java.util.List;

import edu.mit.mitmobile2.TwoLineActionRow;
import edu.mit.mitmobile2.objs.LivingLabItem;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class LivingLabArrayAdapter extends ArrayAdapter<LivingLabItem> {

	public LivingLabArrayAdapter(Context context, int resource, List<LivingLabItem> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TwoLineActionRow view = (TwoLineActionRow) convertView;
		
		if (view == null) {
			// Create new View
			view = new TwoLineActionRow(getContext());
		}
		
		final LivingLabItem labItem = (LivingLabItem) getItem(position);
		// Populate pre-existing view with content;
		view.setTitle(labItem.getName());

		return view;
	}

}
