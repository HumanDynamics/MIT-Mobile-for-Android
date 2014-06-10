package edu.mit.mitmobile2.livinglabs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.media.openpds.client.PreferencesWrapper;
import edu.mit.mitmobile2.objs.LivingLabItem;
import edu.mit.mitmobile2.objs.LivingLabVisualizationItem;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;


public class LivingLabContextTemporalActivity extends Activity implements OnClickListener, OnTouchListener, OnCheckedChangeListener {
	private static final String TAG = "LLContextTemporalActivity";
	private JSONObject llciJson, llsiJson;

	private int selectedHourStart, selectedMinuteStart, selectedHourEnd, selectedMinuteEnd;
	
	String app_id, lab_id; 
	private LivingLabFunfPDS pds;
	private Connection connection;
	private boolean saveFlag = false, deleteFlag = false;

	private int[] daysViews = {R.id.livinglabContextDurationDaySunday, R.id.livinglabContextDurationDayMonday, R.id.livinglabContextDurationDayTuesday,
			R.id.livinglabContextDurationDayWednesday, R.id.livinglabContextDurationDayThursday, R.id.livinglabContextDurationDayFriday,
			R.id.livinglabContextDurationDaySaturday};
	
	private boolean weekdayFlag = false, weekendFlag = false;
	private CheckBox weekdayCheckBox, weekendCheckBox;
	private ToggleButton sundayButton, mondayButton, tuesdayButton, wednesdayButton, thursdayButton, fridayButton, saturdayButton;
	private CheckBox allowAllTimesCheckBox;
	
	
	private View contextMapView, locationTitleView, locationMessageView;
	
	private HashMap<String, Boolean> probeSettings;
	private String contextsFromServer;
	private String context_label = null;
	
	private JSONObject accesscontrolObject = new JSONObject();
	
	private String contextLabelString = "";
	private boolean duration_days_value_flag = false;
	
	private LivingLabItem labItem = null;

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
		
		app_id = "Living Lab";    
		labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
		try {
			llsiJson = new JSONObject(getIntent().getSerializableExtra("llsiJson").toString());
			if(llsiJson.has("settings_context_label"))
				context_label = llsiJson.getString("settings_context_label");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		
		ArrayList<LivingLabVisualizationItem> visualization = labItem.getVisualizations();
        lab_id = labItem.getName();

		setContentView(R.layout.living_lab_context_temporal);
		
		TextView progressText = (TextView) findViewById(R.id.livinglabContextHeaderTextView);
		if(context_label != null)
			progressText.setText(Html.fromHtml("<b>Edit the context</b>"));
		else{
			progressText.setText(Html.fromHtml("<b>Define a context</b>"));
		}
		
		
		TextView labelText = (TextView) findViewById(R.id.livinglabContextLabelTextView);
		labelText.setText(Html.fromHtml("Label"));
		
		TextView durationText = (TextView) findViewById(R.id.livinglabContextDurationTextView);
		durationText.setText(Html.fromHtml("Limit to a specified time range or allow collection all the time."));
		
		TextView fromText = (TextView) findViewById(R.id.livinglabContextDurationFromTextView);
		fromText.setText(Html.fromHtml("From"));
		
		TextView toText = (TextView) findViewById(R.id.livinglabContextDurationToTextView);
		toText.setText(Html.fromHtml("To"));
		
		probeSettings = (HashMap<String, Boolean>) getIntent().getSerializableExtra("probeSettings");
		
		contextsFromServer = getIntent().getStringExtra("contextsFromServer");
		JSONObject contextFromServer = new JSONObject();
		if(contextsFromServer != null){
			Log.v(TAG, contextsFromServer);
			try {
	            JSONArray contextsArray = new JSONArray(contextsFromServer);
	            for(int i=0; i<contextsArray.length(); i++){
	            	if(context_label.equalsIgnoreCase(contextsArray.getJSONObject(i).getString("context_label"))){
	            		contextFromServer = contextsArray.getJSONObject(i);
	            	}
	            }
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
		}
		
		allowAllTimesCheckBox = (CheckBox)findViewById(R.id.livinglabContextAllowAllTimes);
		
		weekdayCheckBox = (CheckBox)findViewById(R.id.livinglabContextDurationDayWeekday);
		weekendCheckBox = (CheckBox)findViewById(R.id.livinglabContextDurationDayWeekend);
		
		sundayButton = (ToggleButton)findViewById(R.id.livinglabContextDurationDaySunday);
		mondayButton = (ToggleButton)findViewById(R.id.livinglabContextDurationDayMonday);
		tuesdayButton = (ToggleButton)findViewById(R.id.livinglabContextDurationDayTuesday);
		wednesdayButton = (ToggleButton)findViewById(R.id.livinglabContextDurationDayWednesday);
		thursdayButton = (ToggleButton)findViewById(R.id.livinglabContextDurationDayThursday);
		fridayButton = (ToggleButton)findViewById(R.id.livinglabContextDurationDayFriday);
		saturdayButton = (ToggleButton)findViewById(R.id.livinglabContextDurationDaySaturday);

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


				String context_duration_start = contextFromServer.getString("context_duration_start");
				String context_duration_end = contextFromServer.getString("context_duration_end");
				EditText durationStartEditText = (EditText) findViewById(R.id.livinglabContextDurationStartEditText);
				durationStartEditText.setText(context_duration_start);
				durationStartEditText.setOnTouchListener(this);


				EditText durationEndEditText = (EditText) findViewById(R.id.livinglabContextDurationEndEditText);
				durationEndEditText.setText(context_duration_end);
				durationEndEditText.setOnTouchListener(this);
				
				if(context_duration_start.trim().equalsIgnoreCase("0 : 0") && context_duration_end.trim().equalsIgnoreCase("23 : 59"))
					allowAllTimesCheckBox.setChecked(true);

				String days = contextFromServer.getString("context_duration_days");
				Log.v(TAG, days);
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
						//CheckBox dayCheckBox = (CheckBox)findViewById(daysViews[i]);
						ToggleButton dayToggleButton = (ToggleButton)findViewById(daysViews[i]);
						dayToggleButton.setChecked(true);
					}
				}

				Log.v(TAG, "days_array length: " + days_array.length);
				if(days_array[1] == 1 && days_array[2] == 1 && days_array[3] == 1 && days_array[4] == 1 && days_array[5] == 1)
					weekdayCheckBox.setChecked(true);
				if(days_array[0] == 1 && days_array[6] == 1)
					weekendCheckBox.setChecked(true);
					
				Button delete = (Button) findViewById(R.id.livinglabContextDeleteButton);
				if(!contextFromServer.getString("context_label").equalsIgnoreCase("MIT") && !contextFromServer.getString("context_label").equalsIgnoreCase("Alltime-Everywhere")){
					delete.setOnClickListener(this);
				} else {
					TableRow tempTableRow = (TableRow) findViewById(R.id.livinglabContextTemporalRow5);
					tempTableRow.setWeightSum(2);
					delete.setVisibility(View.GONE);
				}

				context_prefill_success = true;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else { //new context!
			Button delete = (Button) findViewById(R.id.livinglabContextDeleteButton);
			TableRow tempTableRow = (TableRow) findViewById(R.id.livinglabContextTemporalRow5);
			tempTableRow.setWeightSum(2);
			delete.setVisibility(View.GONE);
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
		
		
		allowAllTimesCheckBox.setOnCheckedChangeListener(this);
		//weekday/weekend processing
		weekdayCheckBox.setOnCheckedChangeListener(this);
		weekendCheckBox.setOnCheckedChangeListener(this);
		sundayButton.setOnCheckedChangeListener(this);
		mondayButton.setOnCheckedChangeListener(this);
		tuesdayButton.setOnCheckedChangeListener(this);
		wednesdayButton.setOnCheckedChangeListener(this);
		thursdayButton.setOnCheckedChangeListener(this);
		fridayButton.setOnCheckedChangeListener(this);
		saturdayButton.setOnCheckedChangeListener(this);
		
		TextView durationDaysError  = (TextView)findViewById(R.id.livinglabContextDurationDayError);
		durationDaysError.setTextColor(Color.parseColor("#0000FF"));

		Button setLocationButton = (Button) findViewById(R.id.livinglabContextSetLocationButton);
		setLocationButton.setOnClickListener(this);
		Button finishButton = (Button) findViewById(R.id.livinglabContextFinishTemporalButton);
		finishButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		EditText contextLabel = (EditText)findViewById(R.id.livinglabContextLabelEditText);
		contextLabelString = contextLabel.getText().toString();
		
		llciJson = new JSONObject();

		switch(v.getId()){
		case R.id.livinglabContextFinishTemporalButton:
			formLlciJson();

			if(duration_days_value_flag && !contextLabelString.isEmpty() && !contextLabelString.equalsIgnoreCase("Create a new context") && !contextLabelString.equalsIgnoreCase("NULL_CONTEXT")){				
				try{
					
					connection = new Connection(this);
					saveFlag = true;
					connection.execute(accesscontrolObject).get(1000, TimeUnit.MILLISECONDS);
					saveFlag = false;

				} catch (Exception e) {
					e.printStackTrace();
				}
				Intent labIntent = new Intent(LivingLabContextTemporalActivity.this, LivingLabActivity.class);
				labIntent.putExtra("lab", labItem);
				startActivity(labIntent);
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
		case R.id.livinglabContextDeleteButton:
			try {
				llciJson.put("context_label", contextLabelString);
				
				accesscontrolObject.put("setting_object", null);
				accesscontrolObject.put("context_object", llciJson);
				
				connection = new Connection(this);
				deleteFlag = true;
				connection.execute(accesscontrolObject).get(1000, TimeUnit.MILLISECONDS);
				deleteFlag = false;

				JSONArray updatedContextsArray = new JSONArray();
				try {
		            JSONArray contextsArray = new JSONArray(contextsFromServer);
		            for(int i=0; i<contextsArray.length(); i++){
		            	if(contextLabelString.equalsIgnoreCase(contextsArray.getJSONObject(i).getString("context_label"))){
		            		context_label = null;
		            	} else {
		            		updatedContextsArray.put(contextsArray.getJSONObject(i));
		            	}
		            }
		        } catch (JSONException e) {
		            e.printStackTrace();
		        }
	            
				Intent labIntent = new Intent(LivingLabContextTemporalActivity.this, LivingLabSettingsContextActivity.class);
				labIntent.putExtra("lab", labItem);
				labIntent.putExtra("probeSettings", probeSettings);
				labIntent.putExtra("context_label", context_label);
				labIntent.putExtra("contextsFromServer", updatedContextsArray.toString());
				startActivity(labIntent);

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.livinglabContextSetLocationButton:	
			
			formLlciJson();
			if(duration_days_value_flag && !contextLabelString.isEmpty() && !contextLabelString.equalsIgnoreCase("Create a new context") && !contextLabelString.equalsIgnoreCase("NULL_CONTEXT")){					
				Intent intent = new Intent(this, LivingLabContextSpatialActivity.class);
				LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
				intent.putExtra("lab", labItem);
				intent.putExtra("llsiJson", llsiJson.toString());
				intent.putExtra("llciJson", llciJson.toString());
				intent.putExtra("contextsFromServer", contextsFromServer);
				intent.putExtra("context_label", contextLabelString);
				
				startActivity(intent);
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
		default:
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent me) {
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Log.v(TAG, Integer.toString(buttonView.getId()));
		CheckBox weekdayCheckBox, weekendCheckBox;
		ToggleButton sundayButton, mondayButton, tuesdayButton, wednesdayButton, thursdayButton, fridayButton, saturdayButton;
		boolean weekdaysChecked, weekendsChecked;
		switch(buttonView.getId()){
		case R.id.livinglabContextDurationDayWeekday:			
			mondayButton = (ToggleButton)findViewById(daysViews[1]);
			tuesdayButton = (ToggleButton)findViewById(daysViews[2]);
			wednesdayButton = (ToggleButton)findViewById(daysViews[3]);
			thursdayButton = (ToggleButton)findViewById(daysViews[4]);
			fridayButton = (ToggleButton)findViewById(daysViews[5]);
			weekdaysChecked = mondayButton.isChecked() && tuesdayButton.isChecked() && wednesdayButton.isChecked() && thursdayButton.isChecked() && fridayButton.isChecked();
			if(weekdaysChecked != isChecked){
				mondayButton.setChecked(isChecked);
				tuesdayButton.setChecked(isChecked);
				wednesdayButton.setChecked(isChecked);
				thursdayButton.setChecked(isChecked);
				fridayButton.setChecked(isChecked);
			}
			break;
		case R.id.livinglabContextDurationDayWeekend:
			sundayButton = (ToggleButton)findViewById(daysViews[0]);
			saturdayButton = (ToggleButton)findViewById(daysViews[6]);
			weekendsChecked = sundayButton.isChecked() && saturdayButton.isChecked();
			if(weekendsChecked != isChecked){
				sundayButton.setChecked(isChecked);
				saturdayButton.setChecked(isChecked);	
			}
			break;
		case R.id.livinglabContextDurationDaySunday:
		case R.id.livinglabContextDurationDaySaturday:	
			sundayButton = (ToggleButton)findViewById(daysViews[0]);
			saturdayButton = (ToggleButton)findViewById(daysViews[6]);
			weekendCheckBox = (CheckBox)findViewById(R.id.livinglabContextDurationDayWeekend);
			
			weekendsChecked = sundayButton.isChecked() && saturdayButton.isChecked();
			if(weekendsChecked != weekendCheckBox.isChecked())
				weekendCheckBox.setChecked(!weekendCheckBox.isChecked());
			break;
		case R.id.livinglabContextDurationDayMonday:
		case R.id.livinglabContextDurationDayTuesday:
		case R.id.livinglabContextDurationDayWednesday:
		case R.id.livinglabContextDurationDayThursday:
		case R.id.livinglabContextDurationDayFriday:
			mondayButton = (ToggleButton)findViewById(daysViews[1]);
			tuesdayButton = (ToggleButton)findViewById(daysViews[2]);
			wednesdayButton = (ToggleButton)findViewById(daysViews[3]);
			thursdayButton = (ToggleButton)findViewById(daysViews[4]);
			fridayButton = (ToggleButton)findViewById(daysViews[5]);			
			weekdayCheckBox = (CheckBox)findViewById(R.id.livinglabContextDurationDayWeekday);
		
			boolean daysChecked = mondayButton.isChecked() && tuesdayButton.isChecked() && wednesdayButton.isChecked() && thursdayButton.isChecked() && fridayButton.isChecked();
			if(daysChecked != weekdayCheckBox.isChecked()){
				weekdayCheckBox.setChecked(!weekdayCheckBox.isChecked());
				Log.v(TAG, "days checked: " + daysChecked);
				Log.v(TAG, "weekday checked: " + weekdayCheckBox.isChecked());
			}
			break;
		case R.id.livinglabContextAllowAllTimes:
			EditText startEditText = (EditText)findViewById(R.id.livinglabContextDurationStartEditText);
			startEditText.setText("0 : 0");
			EditText endEditText = (EditText)findViewById(R.id.livinglabContextDurationEndEditText);
			endEditText.setText("23 : 59");
			
			break;
		}
		
	}

	private class Connection extends AsyncTask<JSONObject, Object, Object> {		 
		private Context mContext;

		@Override
		protected Object doInBackground(JSONObject... object) {
			try {
				
				if(saveFlag){
					PreferencesWrapper prefs = new PreferencesWrapper(mContext);
					String uuid = prefs.getUUID();
					accesscontrolObject.put("datastore_owner", uuid); 
					pds.saveAccessControlData(accesscontrolObject);
				} else if(deleteFlag){
					PreferencesWrapper prefs = new PreferencesWrapper(mContext);
					String uuid = prefs.getUUID();
					
					accesscontrolObject.put("datastore_owner", uuid); 
					pds.deleteAccessControlData(accesscontrolObject);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		public Connection(Context context) {
			this.mContext = context;
		}

	}
	
	public void formLlciJson(){
		EditText durationStartText = (EditText) findViewById(R.id.livinglabContextDurationStartEditText);
		String durationStart = durationStartText.getText().toString();
		EditText durationEndText = (EditText) findViewById(R.id.livinglabContextDurationEndEditText);
		String durationEnd = durationEndText.getText().toString();

		int[] duration_days_value = new int[7];
		duration_days_value[0] = ((ToggleButton)findViewById(R.id.livinglabContextDurationDaySunday)).isChecked()? 1 : 0;
		duration_days_value[1] = ((ToggleButton)findViewById(R.id.livinglabContextDurationDayMonday)).isChecked()? 1 : 0;
		duration_days_value[2] = ((ToggleButton)findViewById(R.id.livinglabContextDurationDayTuesday)).isChecked()? 1 : 0;
		duration_days_value[3] = ((ToggleButton)findViewById(R.id.livinglabContextDurationDayWednesday)).isChecked()? 1 : 0;
		duration_days_value[4] = ((ToggleButton)findViewById(R.id.livinglabContextDurationDayThursday)).isChecked()? 1 : 0;
		duration_days_value[5] = ((ToggleButton)findViewById(R.id.livinglabContextDurationDayFriday)).isChecked()? 1 : 0;
		duration_days_value[6] = ((ToggleButton)findViewById(R.id.livinglabContextDurationDaySaturday)).isChecked()? 1 : 0;

		for(int i=0; i<duration_days_value.length; i++){
			if(duration_days_value[i] == 1)
				duration_days_value_flag = true;
		}
		
		String duration_days = (Arrays.toString(duration_days_value));
		//String places = arrayPoints.toString();

		try{
			llsiJson.put("settings_context_label", contextLabelString);
			
			llciJson.put("context_label", contextLabelString);
			llciJson.put("context_duration_start", durationStart);
			llciJson.put("context_duration_end", durationEnd);
			llciJson.put("context_duration_days", duration_days);
			llciJson.put("context_places", "");
			
			Log.v(TAG, llciJson.toString());

			accesscontrolObject.put("setting_object", llsiJson);
			accesscontrolObject.put("context_object", llciJson);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onBackPressed() {
	}

}