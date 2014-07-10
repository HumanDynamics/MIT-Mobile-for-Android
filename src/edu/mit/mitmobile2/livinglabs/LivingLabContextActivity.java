package edu.mit.mitmobile2.livinglabs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.google.android.gms.location.LocationClient;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import edu.mit.media.openpds.client.PreferencesWrapper;
import edu.mit.mitmobile2.objs.LivingLabItem;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


public class LivingLabContextActivity extends Activity implements OnClickListener, OnTouchListener, OnCheckedChangeListener, OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
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
	
	private boolean weekdayFlag = false, weekendFlag = false;
	private CheckBox weekdayCheckBox, weekendCheckBox;

	private LocationClient mLocationClient;
	private ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>(); 
	private boolean checkClick = false;

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
		
		weekdayCheckBox = (CheckBox)findViewById(R.id.livinglabContextDurationDayWeekday);
		weekendCheckBox = (CheckBox)findViewById(R.id.livinglabContextDurationDayWeekend);

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

				if(days_array[1] == 1 && days_array[2] == 1 && days_array[3] == 1 && days_array[4] == 1 && days_array[5] == 1)
					weekdayCheckBox.setChecked(true);
				if(days_array[0] == 1 && days_array[6] == 1)
					weekendCheckBox.setChecked(true);
				
				
				mLocationClient = new LocationClient(this, this, this);	
		        String places = contextFromServer.getString("context_places");
		        
		        Pattern p = Pattern.compile("\\((.*?)\\)",Pattern.DOTALL);
				Matcher matcher = p.matcher(places);
				while(matcher.find()) {
					String[] geo = matcher.group(1).split(",");
					double lat = Double.parseDouble(geo[0]);
					double lng = Double.parseDouble(geo[1]);
					LatLng latlng = new LatLng(lat, lng);
					
					arrayPoints.add(latlng);
					drawCircles(arrayPoints);
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
			
			// Create the LocationRequest object
	        mLocationClient = new LocationClient(this, this, this);	
		}

		//weekday/weekend processing
		weekdayCheckBox.setOnCheckedChangeListener(this);
		weekendCheckBox.setOnCheckedChangeListener(this);
		
		TextView durationDaysError  = (TextView)findViewById(R.id.livinglabContextDurationDayError);
		durationDaysError.setTextColor(Color.parseColor("#0000FF"));

		Button save = (Button) findViewById(R.id.livinglabContextSaveButton);
		save.setOnClickListener(this);
		Button cancel = (Button) findViewById(R.id.livinglabContextCancelButton);
		cancel.setOnClickListener(this);

		TextView locationMessage = (TextView)findViewById(R.id.livinglabContextLocationMessage);
		locationMessage.setTextColor(Color.parseColor("#0000FF"));

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
				String places = arrayPoints.toString();

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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Log.v(TAG, Integer.toString(buttonView.getId()));
		switch(buttonView.getId()){
		case R.id.livinglabContextDurationDayWeekday:
			for(int i=1; i<6; i++){
				CheckBox weekdayCheckBox = (CheckBox)findViewById(daysViews[i]);
				weekdayCheckBox.setChecked(!weekdayCheckBox.isChecked());
			}
			break;
		case R.id.livinglabContextDurationDayWeekend:
			CheckBox weekendCheckBox1 = (CheckBox)findViewById(daysViews[0]);
			weekendCheckBox1.setChecked(!weekendCheckBox1.isChecked());
			CheckBox weekendCheckBox2 = (CheckBox)findViewById(daysViews[6]);
			weekendCheckBox2.setChecked(!weekendCheckBox2.isChecked());			
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
					llciJson.put("datastore_owner", uuid); 
					llciJson.put("context_setting_flag", 0); //0 - context
					
//					String result = pds.saveAccessControlData(llciJson);
					pds.accessControlData(llciJson, "store");
				} else if(deleteFlag){
					PreferencesWrapper prefs = new PreferencesWrapper(mContext);
	
					String uuid = prefs.getUUID();
					llciJson.put("datastore_owner", uuid); 
					llciJson.put("context_setting_flag", 0); //0 - context
					
//					String result = pds.deleteAccessControlData(llciJson);
					pds.accessControlData(llciJson, "delete");
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
	
	@Override
	protected void onStart() {
		super.onStart();
	    // Connect the client.
	    mLocationClient.connect();
	}
	
	@Override
	protected void onStop() {
		// Disconnect the client.
	    mLocationClient.disconnect();
	    super.onStop();
	}


   @Override
   public void onDisconnected() {
      // Display the connection status
      Toast.makeText(this, "Disconnected. Please re-connect.",
      Toast.LENGTH_SHORT).show();
   }
   @Override
   public void onConnectionFailed(ConnectionResult connectionResult) {
      // Display the error code on failure
      Toast.makeText(this, "Connection Failure : " + 
      connectionResult.getErrorCode(),
      Toast.LENGTH_SHORT).show();
   }


   @Override
	public void onConnected(Bundle bundle) {
      // Get the current location's latitude & longitude
      Location currentLocation = mLocationClient.getLastLocation();

      MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.livinglabContextMap);
	    GoogleMap map = mapFragment.getMap();
	    if (map != null) {
	        // The GoogleMap object is ready to go.
	    	map.setMapType(1);

		    	//LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()); 
	    		LatLng latLng;
	    		if(currentLocation == null){
	    			latLng = new LatLng(42.359957, -71.093539); //centering the map on 77 Mass Ave.
	    		} else {
	    			latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()); //obtained location through the device
	    		}

		  	   	CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
		  	   	map.animateCamera(cameraUpdate);
		  	   	map.getUiSettings().setZoomControlsEnabled(true);

		  	  map.setOnMapClickListener(this);
		  	  map.setOnMapLongClickListener(this);
		  	  map.setOnMarkerClickListener(this);


	    }


   }


	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void onMapLongClick(LatLng arg0) {
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.livinglabContextMap);
	    GoogleMap map = mapFragment.getMap();
		map.clear();
		arrayPoints.clear();
		checkClick = false;
	}


	@Override
	public void onMapClick(LatLng point) {
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.livinglabContextMap);
		GoogleMap map = mapFragment.getMap();
		map.addMarker(new MarkerOptions().position(point).icon( BitmapDescriptorFactory.fromResource(R.drawable.map_red_pin))); 
	
		// Instantiates a new CircleOptions object and defines the center and radius
		CircleOptions circleOptions = new CircleOptions()
		    .center(point)
		    .radius(300); // In meters
	
		// Get back the mutable Circle
		Circle circle = map.addCircle(circleOptions);
	
	
		arrayPoints.add(point); 
		
	}
	
	public void drawCircles(ArrayList<LatLng> points){
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.livinglabContextMap);
		GoogleMap map = mapFragment.getMap();


		for(int i=0; i<points.size(); i++){
			map.addMarker(new MarkerOptions().position(points.get(i)).icon( BitmapDescriptorFactory.fromResource(R.drawable.map_red_pin))); 
			// Instantiates a new CircleOptions object and defines the center and radius
			CircleOptions circleOptions = new CircleOptions()
			    .center(points.get(i))
			    .radius(300); // In meters

			// Get back the mutable Circle
			Circle circle = map.addCircle(circleOptions);
		}

	}


	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}


}