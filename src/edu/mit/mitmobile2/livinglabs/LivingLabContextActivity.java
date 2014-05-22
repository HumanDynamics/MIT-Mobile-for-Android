package edu.mit.mitmobile2.livinglabs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.PolylineOptions;

import edu.mit.media.openpds.client.PreferencesWrapper;
import edu.mit.mitmobile2.objs.LivingLabItem;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;


public class LivingLabContextActivity extends Activity implements OnClickListener, OnTouchListener  {
	private static final String TAG = "LLContextActivity";
	private JSONObject llciJson;

	private int selectedHourStart, selectedMinuteStart, selectedHourEnd, selectedMinuteEnd;
	
	PolylineOptions polylineOptions; 
	private LivingLabFunfPDS pds;
	private Connection connection;
	private boolean saveFlag = false, deleteFlag = false;

	private int[] daysViews = {R.id.livinglabContextDurationDaySunday, R.id.livinglabContextDurationDayMonday, R.id.livinglabContextDurationDayTuesday,
			R.id.livinglabContextDurationDayWednesday, R.id.livinglabContextDurationDayThursday, R.id.livinglabContextDurationDayFriday,
			R.id.livinglabContextDurationDaySaturday};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//If user is not logged in, redirect to touchstone
		try {
			pds = new LivingLabFunfPDS(this);
		} catch (Exception e) {
			Intent intent = new Intent(this, LivingLabsLoginActivity.class);
			startActivity(intent);
			finish();
			return;
		}   

		setContentView(R.layout.living_lab_context);
		Serializable  context_label_serializable = getIntent().getSerializableExtra("context_label");
		String context_label = null;


		if(context_label_serializable != null)
			context_label = context_label_serializable.toString();
		
		String contextsString = getIntent().getStringExtra("contexts");
		JSONObject contextFromServer = new JSONObject();
		if(contextsString != null){
			Log.v(TAG, contextsString);
			try {
	            JSONArray contextsArray = new JSONArray(contextsString);
	            for(int i=0; i<contextsArray.length(); i++){
	            	if(context_label.equalsIgnoreCase(contextsArray.getJSONObject(i).getString("context_label"))){
	            		contextFromServer = contextsArray.getJSONObject(i);
	            	}
	            }
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
		}

		boolean context_prefill_success = false;
		if(context_label != null){
			ArrayList<String> searchDataInput = new ArrayList<String>();
			searchDataInput.add(context_label);
			try {

				EditText contextLabelEditText = (EditText) findViewById(R.id.livinglabContextLabelEditText);
				contextLabelEditText.setText(contextFromServer.getString("context_label"));
				contextLabelEditText.setEnabled(false);

				TextView durationDaysError  = (TextView)findViewById(R.id.livinglabContextLabelError);
				durationDaysError.setText("(not editable)");
				durationDaysError.setTextColor(Color.parseColor("#FF0000"));


				EditText durationStartEditText = (EditText) findViewById(R.id.livinglabContextDurationStartEditText);
				durationStartEditText.setText(contextFromServer.getString("context_duration_start"));
				durationStartEditText.setOnTouchListener(this);


				EditText durationEndEditText = (EditText) findViewById(R.id.livinglabContextDurationEndEditText);
				durationEndEditText.setText(contextFromServer.getString("context_duration_end"));
				durationEndEditText.setOnTouchListener(this);

				String days = contextFromServer.getString("context_duration_days");
				String[] days_items = days.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
				int[] days_array = new int[days_items.length];

				for (int i=0; i < days_items.length; i++) {
					try {
						days_array[i] = Integer.parseInt(days_items[i].trim());
					} catch (NumberFormatException nfe) {};
				}

				for(int i=0; i<days_array.length; i++){
					int day_value = days_array[i];
					if(day_value == 1){
						CheckBox dayCheckBox = (CheckBox)findViewById(daysViews[i]);
						dayCheckBox.setChecked(true);
					}
				}

				Button delete = (Button) findViewById(R.id.livinglabContextDeleteButton);
				delete.setOnClickListener(this);

				context_prefill_success = true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if(!context_prefill_success){
			TimePicker timePicker = new TimePicker(this);
			selectedHourStart = timePicker.getCurrentHour();
			selectedMinuteStart = timePicker.getCurrentMinute();
			EditText durationStartEditText = (EditText) findViewById(R.id.livinglabContextDurationStartEditText);
			durationStartEditText.setText("" + selectedHourStart + " : " + selectedMinuteStart);
			durationStartEditText.setOnTouchListener(this);

			if(selectedHourStart == 24)
				selectedHourEnd = 0;
			else
				selectedHourEnd = selectedHourStart + 1;
			selectedMinuteEnd = selectedMinuteStart;
			EditText durationEndEditText = (EditText) findViewById(R.id.livinglabContextDurationEndEditText);
			durationEndEditText.setText("" + selectedHourEnd + " : " + selectedMinuteEnd);
			durationEndEditText.setOnTouchListener(this);
		}

		TextView durationDaysError  = (TextView)findViewById(R.id.livinglabContextDurationDayError);
		durationDaysError.setTextColor(Color.parseColor("#0000FF"));

		Button save = (Button) findViewById(R.id.livinglabContextSaveButton);
		save.setOnClickListener(this);
		Button cancel = (Button) findViewById(R.id.livinglabContextCancelButton);
		cancel.setOnClickListener(this);



	}

	@Override
	public void onClick(View v) {
		EditText contextLabel = (EditText)findViewById(R.id.livinglabContextLabelEditText);
		String contextLabelString = contextLabel.getText().toString();

		switch(v.getId()){
		case R.id.livinglabContextSaveButton:
			llciJson = new JSONObject();


			EditText durationStartText = (EditText) findViewById(R.id.livinglabContextDurationStartEditText);
			String durationStart = durationStartText.getText().toString();
			EditText durationEndText = (EditText) findViewById(R.id.livinglabContextDurationEndEditText);
			String durationEnd = durationEndText.getText().toString();

			int[] duration_days_value = new int[7];
			duration_days_value[0] = ((CheckBox)findViewById(R.id.livinglabContextDurationDaySunday)).isChecked()? 1 : 0;
			duration_days_value[1] = ((CheckBox)findViewById(R.id.livinglabContextDurationDayMonday)).isChecked()? 1 : 0;
			duration_days_value[2] = ((CheckBox)findViewById(R.id.livinglabContextDurationDayTuesday)).isChecked()? 1 : 0;
			duration_days_value[3] = ((CheckBox)findViewById(R.id.livinglabContextDurationDayWednesday)).isChecked()? 1 : 0;
			duration_days_value[4] = ((CheckBox)findViewById(R.id.livinglabContextDurationDayThursday)).isChecked()? 1 : 0;
			duration_days_value[5] = ((CheckBox)findViewById(R.id.livinglabContextDurationDayFriday)).isChecked()? 1 : 0;
			duration_days_value[6] = ((CheckBox)findViewById(R.id.livinglabContextDurationDaySaturday)).isChecked()? 1 : 0;

			boolean duration_days_value_flag = false;
			for(int i=0; i<duration_days_value.length; i++){
				if(duration_days_value[i] == 1)
					duration_days_value_flag = true;
			}

			if(duration_days_value_flag && !contextLabelString.isEmpty() && !contextLabelString.equalsIgnoreCase("Create a new context") && !contextLabelString.equalsIgnoreCase("NULL_CONTEXT")){
				String duration_days = (Arrays.toString(duration_days_value));
				String places = "";

				try{
					llciJson.put("context_label", contextLabelString);
					llciJson.put("context_duration_start", durationStart);
					llciJson.put("context_duration_end", durationEnd);
					llciJson.put("context_duration_days", duration_days);
					llciJson.put("context_places", places);
				} catch(Exception e){
					e.printStackTrace();
				}


				try {
					connection = new Connection(this);
					saveFlag = true;
					connection.execute(llciJson).get(1000, TimeUnit.MILLISECONDS);
					saveFlag = false;

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				Intent activityIntent = new Intent(LivingLabContextActivity.this, LivingLabSettingsActivity.class);
				LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
				activityIntent.putExtra("lab", labItem);
				LivingLabContextActivity.this.startActivity(activityIntent);

				finish();
			} else{

				if(!duration_days_value_flag){
					TextView durationDaysError  = (TextView)findViewById(R.id.livinglabContextDurationDayError);
					durationDaysError.setText("Select at least one day.");
					durationDaysError.setTextColor(Color.parseColor("#FF0000"));
				} else if (contextLabelString.isEmpty() || contextLabelString.equalsIgnoreCase("Create a new context") || contextLabelString.equalsIgnoreCase("MIT") || contextLabelString.equalsIgnoreCase("NULL_CONTEXT")){
					TextView durationDaysError  = (TextView)findViewById(R.id.livinglabContextLabelError);
					durationDaysError.setText("Provide a valid label.");
					durationDaysError.setTextColor(Color.parseColor("#FF0000"));
				}
			}
			break;
		case R.id.livinglabContextCancelButton:
			finish();
			break;
		case R.id.livinglabContextDeleteButton:
			llciJson = new JSONObject();

			try {
				llciJson.put("context_label", contextLabelString);
				connection = new Connection(this);
				deleteFlag = true;
				connection.execute(llciJson).get(1000, TimeUnit.MILLISECONDS);
				deleteFlag = false;

				Intent activityIntent = new Intent(LivingLabContextActivity.this, LivingLabSettingsActivity.class);
				LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
				activityIntent.putExtra("lab", labItem);
				LivingLabContextActivity.this.startActivity(activityIntent);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;
		}


	}

	@Override
	public boolean onTouch(View v, MotionEvent me) {
		// TODO Auto-generated method stub
		if ((me.getAction() == MotionEvent.ACTION_DOWN) && (v.getId() == R.id.livinglabContextDurationStartEditText || v.getId() == R.id.livinglabContextDurationEndEditText)){
			if(v.getId() == R.id.livinglabContextDurationStartEditText){
				EditText selectedTimeStartEdit = (EditText)findViewById(R.id.livinglabContextDurationStartEditText);
				String selectedTimeStart = selectedTimeStartEdit.getText().toString();
				StringTokenizer timeSt = new StringTokenizer(selectedTimeStart, " : ");
				
				selectedHourStart = Integer.parseInt(timeSt.nextToken());
				selectedMinuteStart = Integer.parseInt(timeSt.nextToken());
				
				TimePickerDialog mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
						selectedHourStart = selectedHour;
						selectedMinuteStart = selectedMinute;
						EditText startEditText = (EditText)findViewById(R.id.livinglabContextDurationStartEditText);
						startEditText.setText( selectedHourStart + " : " + selectedMinuteStart);

						if(selectedHourStart == 24)
							selectedHourEnd = 0;
						else
							selectedHourEnd = selectedHourStart + 1;
						selectedMinuteEnd = selectedMinuteStart;
						EditText endEditText = (EditText)findViewById(R.id.livinglabContextDurationEndEditText);
						endEditText.setText( selectedHourEnd + " : " + selectedMinuteEnd);
					}
				}, selectedHourStart, selectedMinuteStart, true);//Yes 24 hour time
				mTimePicker.setTitle("Select Start");
				mTimePicker.show();
			} else if(v.getId() == R.id.livinglabContextDurationEndEditText){
				EditText selectedTimeEndEdit = (EditText)findViewById(R.id.livinglabContextDurationEndEditText);
				String selectedTimeEnd = selectedTimeEndEdit.getText().toString();
				StringTokenizer timeSt = new StringTokenizer(selectedTimeEnd, " : ");
				
				selectedHourEnd = Integer.parseInt(timeSt.nextToken());
				selectedMinuteEnd = Integer.parseInt(timeSt.nextToken());
				
				TimePickerDialog mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
						selectedHourEnd = selectedHour;
						selectedMinuteEnd = selectedMinute;
						EditText endEditText = (EditText)findViewById(R.id.livinglabContextDurationEndEditText);
						endEditText.setText( selectedHourEnd + " : " + selectedMinuteEnd);

						if(selectedHourStart > selectedHourEnd || selectedHourEnd == 0 || (selectedHourStart == selectedHourEnd && selectedMinuteStart > selectedMinuteEnd)){
							if(selectedHourEnd == 0)
								selectedHourStart = 23;
							else
								selectedHourStart = selectedHourEnd - 1;
							selectedMinuteStart = selectedMinuteEnd;

							EditText startEditText = (EditText)findViewById(R.id.livinglabContextDurationStartEditText);
							startEditText.setText( selectedHourStart + " : " + selectedMinuteStart);
						}
					}
				}, selectedHourEnd, selectedMinuteEnd, true);//Yes 24 hour time
				mTimePicker.setTitle("Select End");
				mTimePicker.show();
			}
		} 
		return false;
	}


	private class Connection extends AsyncTask<JSONObject, Object, Object> {		 
		private Context mContext;

		@Override
		protected Object doInBackground(JSONObject... object) {
			try {
				
				if(saveFlag){
					PreferencesWrapper prefs = new PreferencesWrapper(mContext);

					String uuid = prefs.getUUID();
					llciJson.put("datastore_owner", uuid); 
					llciJson.put("context_setting_flag", 0); //0 - context
					
					String result = pds.saveAccessControlData(llciJson);
				} else if(deleteFlag){
					PreferencesWrapper prefs = new PreferencesWrapper(mContext);
	
					String uuid = prefs.getUUID();
					llciJson.put("datastore_owner", uuid); 
					llciJson.put("context_setting_flag", 0); //0 - context
					
					String result = pds.deleteAccessControlData(llciJson);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		public Connection(Context context) {
			this.mContext = context;
		}

	}


}