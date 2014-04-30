package edu.mit.mitmobile2.livinglabs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
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

import edu.mit.mitmobile2.objs.LivingLabContextItem;
import edu.mit.mitmobile2.objs.LivingLabItem;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
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
import android.widget.Toast;

 
public class LivingLabContextActivity extends Activity implements OnClickListener, OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener, OnTouchListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener  {
	private static final String TAG = "LLContextActivity";
    private JSONObject llciJson;
    
    private LivingLabsAccessControlDB mLivingLabAccessControlDB;
    
    private int selectedHourStart, selectedMinuteStart, selectedHourEnd, selectedMinuteEnd;

    private LocationClient mLocationClient;
    
    private boolean store_delete_flag = false; //false - store, true - delete
    private ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>(); 
    PolylineOptions polylineOptions; 
    private boolean checkClick = false;
    
    private int[] daysViews = {R.id.livinglabContextDurationDaySunday, R.id.livinglabContextDurationDayMonday, R.id.livinglabContextDurationDayTuesday,
    							R.id.livinglabContextDurationDayWednesday, R.id.livinglabContextDurationDayThursday, R.id.livinglabContextDurationDayFriday,
    								R.id.livinglabContextDurationDaySaturday};

    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLivingLabAccessControlDB = LivingLabsAccessControlDB.getInstance(this);   
        
        setContentView(R.layout.living_lab_context);
        Serializable  context_label_serializable = getIntent().getSerializableExtra("context_label");
        String context_label = null;
        
        
        if(context_label_serializable != null)
        	context_label = context_label_serializable.toString();
        
        boolean context_prefill_success = false;
        if(context_label != null){
            ArrayList<String> searchDataInput = new ArrayList<String>();
            searchDataInput.add(context_label);
            LivingLabContextItem llciFetched = null;
    		try {
    			llciFetched = mLivingLabAccessControlDB.retrieveLivingLabContextItem(searchDataInput);
    			
    			if(llciFetched != null){

    		        EditText contextLabelEditText = (EditText) findViewById(R.id.livinglabContextLabelEditText);
    		        contextLabelEditText.setText(llciFetched.getContextLabel());
    		        contextLabelEditText.setEnabled(false);
    		        
    		        TextView durationDaysError  = (TextView)findViewById(R.id.livinglabContextLabelError);
					durationDaysError.setText("(not editable)");
					durationDaysError.setTextColor(Color.parseColor("#FF0000"));
    		        
    		        
    		        EditText durationStartEditText = (EditText) findViewById(R.id.livinglabContextDurationStartEditText);
    		        durationStartEditText.setText(llciFetched.getContextDurationStart());
    		        durationStartEditText.setOnTouchListener(this);
    		        
    		        
    		        EditText durationEndEditText = (EditText) findViewById(R.id.livinglabContextDurationEndEditText);
    		        durationEndEditText.setText(llciFetched.getContextDurationEnd());
    		        durationEndEditText.setOnTouchListener(this);
    		        
    		        String days = llciFetched.getContextDurationDays();
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
    		        
    		        mLocationClient = new LocationClient(this, this, this);	
    		        String places = llciFetched.getContextPlaces();
    		        
    		        Pattern p = Pattern.compile("\\((.*?)\\)",Pattern.DOTALL);
    				Matcher matcher = p.matcher(places);
    				while(matcher.find()) {
    					String[] geo = matcher.group(1).split(",");
    					double lat = Double.parseDouble(geo[0]);
    					double lng = Double.parseDouble(geo[1]);
    					LatLng latlng = new LatLng(lat, lng);
    					
    					arrayPoints.add(latlng);
    					
    					//countPolygonPoints();
    					drawCircles(arrayPoints);
    					
    				}
    				
    		        Button delete = (Button) findViewById(R.id.livinglabContextDeleteButton);
    		        delete.setOnClickListener(this);

    				context_prefill_success = true;
    			}
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
        
        TextView durationDaysError  = (TextView)findViewById(R.id.livinglabContextLocationMessage);
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
					
					String places = arrayPoints.toString();
					Log.v(TAG, "places: " + places);
					
					try{
						llciJson.put("context_label", contextLabelString);
						llciJson.put("context_duration_start", durationStart);
						llciJson.put("context_duration_end", durationEnd);
						llciJson.put("context_duration_days", duration_days);
						llciJson.put("context_places", places);
					} catch(Exception e){
						e.printStackTrace();
					}
					
					LivingLabContextItem llci;
					try {
						llci = new LivingLabContextItem(llciJson);
						mLivingLabAccessControlDB.saveLivingLabContextItem(llci);
						
						store_delete_flag = false; //store
						
						Connection connection = new Connection(this);
						connection.execute(llciJson).get(1000, TimeUnit.MILLISECONDS);
						
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
					} else if (contextLabelString.isEmpty() || contextLabelString.equalsIgnoreCase("Create a new context") || contextLabelString.equalsIgnoreCase("NULL_CONTEXT")){
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
				LivingLabContextItem llci;
				llciJson = new JSONObject();
				
				try {
					llciJson.put("context_label", contextLabelString);
					llci = new LivingLabContextItem(llciJson);
					mLivingLabAccessControlDB.deleteContextItem(llci);
					
					store_delete_flag = true; //delete
					
					Intent activityIntent = new Intent(LivingLabContextActivity.this, LivingLabSettingsActivity.class);
		        	LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
		        	activityIntent.putExtra("lab", labItem);
					LivingLabContextActivity.this.startActivity(activityIntent);
					
				} catch (JSONException e) {
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
	            TimePickerDialog mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
	                @Override
	                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
	                    selectedHourEnd = selectedHour;
	                    selectedMinuteEnd = selectedMinute;
	    	            EditText endEditText = (EditText)findViewById(R.id.livinglabContextDurationEndEditText);
	    	            endEditText.setText( selectedHourEnd + " : " + selectedMinuteEnd);
	    	            
	    	            if(selectedHourStart < selectedHourEnd || selectedHourEnd == 0 || (selectedHourStart == selectedHourEnd && selectedMinuteStart < selectedMinuteEnd)){
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
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
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
		    	
			    	LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			  	   	CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
			  	   	map.animateCamera(cameraUpdate);
			  	   	map.getUiSettings().setZoomControlsEnabled(true);

			  	  map.setOnMapClickListener(this);
			  	  map.setOnMapLongClickListener(this);
			  	  map.setOnMarkerClickListener(this);
		        
		        
		    }
	     
	     
	   }
	   
	   public void Draw_Map(ArrayList<LatLng> latlngList) {
		    PolygonOptions rectOptions = new PolygonOptions();
		    rectOptions.addAll(latlngList);
		    rectOptions.strokeColor(Color.BLACK);
		    rectOptions.strokeWidth(7);
		    rectOptions.fillColor(Color.GRAY);
		    MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.livinglabContextMap);
		    GoogleMap map = mapFragment.getMap();
		    map.addPolygon(rectOptions);
		}

//	@Override
//	public void onMapClick(LatLng point) {
//		// TODO Auto-generated method stub
//		if (checkClick == false) { 
//			MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.livinglabContextMap);
//		    GoogleMap map = mapFragment.getMap();
//			map.addMarker(new MarkerOptions().position(point).icon( BitmapDescriptorFactory.fromResource(R.drawable.map_red_pin))); 
//			arrayPoints.add(point); 
//		}
//		
//	}
	
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
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
				if (arrayPoints.get(0).equals(marker.getPosition())) { 
					countPolygonPoints(); 
				} 
				return false; 
	}
	
	public void countPolygonPoints() { 
		if (arrayPoints.size() >= 3) { 
			checkClick = true; 
			PolygonOptions polygonOptions = new PolygonOptions(); 
			polygonOptions.addAll(arrayPoints); 
			polygonOptions.strokeColor(Color.BLUE); 
			polygonOptions.strokeWidth(7); polygonOptions.fillColor(Color.CYAN); 
			MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.livinglabContextMap);
		    GoogleMap map = mapFragment.getMap();
			map.addPolygon(polygonOptions); 
		} 
	}


	@Override
	public void onMapLongClick(LatLng arg0) {
		// TODO Auto-generated method stub
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.livinglabContextMap);
	    GoogleMap map = mapFragment.getMap();
		map.clear();
		  arrayPoints.clear();
		  checkClick = false;
		
		
	}

	
	private class Connection extends AsyncTask<JSONObject, Object, Object> {
		 
		private Context mContext;
		
        @Override
        protected Object doInBackground(JSONObject... object) {
        	try {
        		LivingLabFunfPDS llFunfPDS = new LivingLabFunfPDS(mContext);
        		
        		StringTokenizer st_uuid;
        		
        		if(!store_delete_flag) //false - store
        			st_uuid =  new StringTokenizer(llFunfPDS.getAccessControlStoreUrl(),"&");
        		else //true - delete
        			st_uuid = new StringTokenizer(llFunfPDS.getAccessControlDeleteUrl(),"&");
        		st_uuid.nextToken();
        		StringTokenizer st_uuidval = new StringTokenizer(st_uuid.nextToken(),"=");
        		st_uuidval.nextToken();
        		String uuid = st_uuidval.nextToken();
        		llciJson.put("datastore_owner", uuid); 
        		llciJson.put("context_setting_flag", 0); //0 - context
        		String result = llFunfPDS.uploadFunfData(llciJson);
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