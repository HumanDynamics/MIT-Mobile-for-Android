package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.google.android.gms.maps.model.PolylineOptions;

import edu.mit.media.openpds.client.PreferencesWrapper;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleActivity;
import edu.mit.mitmobile2.objs.LivingLabItem;
import edu.mit.mitmobile2.objs.LivingLabVisualizationItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class LivingLabContextSpatialActivity extends NewModuleActivity implements OnClickListener, OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	private static final String TAG = "LLContextSpatialActivity";
	private JSONObject llciJson, llsiJson;
	
	String app_id, lab_id;
	PolylineOptions polylineOptions; 
	private LivingLabFunfPDS pds;
	private Connection connection;
	private boolean saveFlag = false, deleteFlag = false;

	private LocationClient mLocationClient;
	private ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>(); 
	private boolean checkClick = false;
	
	private HashMap<String, Boolean> probeSettings;
	private String contextsFromServer;
	private String context_label = null;
	
	private JSONObject accesscontrolObject = new JSONObject();
	private LivingLabItem labItem = null;
	
	private String places = null;
	
	private int textId = 0;

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
		
		ArrayList<LivingLabVisualizationItem> visualization = labItem.getVisualizations();
        lab_id = labItem.getName();
		try {
			llsiJson = new JSONObject(getIntent().getSerializableExtra("llsiJson").toString());
			llciJson = new JSONObject(getIntent().getSerializableExtra("llciJson").toString());
			if(llsiJson.has("settings_context_label"))
				context_label = llsiJson.getString("settings_context_label");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		setContentView(R.layout.living_lab_context_spatial);
		
		TextView labelText = (TextView) findViewById(R.id.livinglabContextHeaderTextView);
		labelText.setText(Html.fromHtml("<h4>" + lab_id + ": Define Location</h4> Tap the map to indicate " + 
				"where MIT-FIT can use your data. " + 
				"You can specify one or more locations. To delete the locations, " + 
				"press and hold the map."));
				//"Note: the locations you specify here will not be shared with " + lab_id + "."));
		labelText.setId(textId);
		
//		TextView locationMessageText = (TextView) findViewById(R.id.livinglabContextLocationMessage);
//		locationMessageText.setText(Html.fromHtml("Tap the map to indicate where MIT-FIT can use your data. " + 
//				"You can specify one or more locations. To delete the locations, " + 
//				"press and hold the map." + 
//				"Note: the locations you specify here will not be shared with " + lab_id + "."));
//		locationMessageText.setId(textId);
		
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
		
		try{
			mLocationClient = new LocationClient(this, this, this);	
		} catch(Exception e){
			e.printStackTrace();
		}

		if(contextFromServer != null){
			ArrayList<String> searchDataInput = new ArrayList<String>();
			searchDataInput.add(context_label);
			try {
				if(contextFromServer.has("context_places")){
			        places = contextFromServer.getString("context_places");

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
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Button finishButton = (Button) findViewById(R.id.livinglabContextFinishSpatialButton);
		finishButton.setEnabled(!arrayPoints.isEmpty()); //if arrayPoints is empty, disable
		finishButton.setOnClickListener(this);
//		TextView locationMessage = (TextView)findViewById(R.id.livinglabContextLocationMessage);
//		locationMessage.setTextColor(Color.parseColor("#0000FF"));
		
	    View textIdView = findViewById(textId);
	    View rootView = textIdView.getRootView();
	    rootView.setBackgroundColor(getResources().getColor(android.R.color.white));
	}


	@Override
	public void onClick(View v) {

		switch(v.getId()){
		case R.id.livinglabContextFinishSpatialButton:
			places = arrayPoints.toString();
			if(!arrayPoints.isEmpty()){
				try{
					llciJson.put("context_places", places);
					
					accesscontrolObject.put("setting_object", llsiJson);
					accesscontrolObject.put("context_object", llciJson);
					connection = new Connection(this);
					saveFlag = true;
					connection.execute(accesscontrolObject).get(1000, TimeUnit.MILLISECONDS);
					saveFlag = false;
	
				} catch (Exception e) {
					e.printStackTrace();
				}
				Intent labIntent = new Intent(LivingLabContextSpatialActivity.this, LivingLabActivity.class);
				labIntent.putExtra("lab", labItem);
				startActivity(labIntent);
			} else {
				TextView errorTextView = (TextView)findViewById(R.id.livinglabContextLocationError);
				errorTextView.setText("Please select a location.");
				errorTextView.setTextColor(Color.RED);
			}
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

//					pds.saveAccessControlData(accesscontrolObject);
					pds.accessControlData(accesscontrolObject, "store");
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
	
	@Override
	protected void onStart() {
		super.onStart();
	    mLocationClient.connect();
	}
	
	@Override
	protected void onStop() {
	    mLocationClient.disconnect();
	    super.onStop();
	}


   @Override
   public void onDisconnected() {
      Toast.makeText(this, "Disconnected. Please re-connect.",
      Toast.LENGTH_SHORT).show();
   }
   @Override
   public void onConnectionFailed(ConnectionResult connectionResult) {
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
 
	    		LatLng latLng;
	    		if(currentLocation == null){
	    			latLng = new LatLng(42.359957, -71.093539); //if no location available, center the map on 77 Mass Ave.
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
		return false;
	}


	@Override
	public void onMapLongClick(LatLng arg0) {
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.livinglabContextMap);
	    GoogleMap map = mapFragment.getMap();
		map.clear();
		arrayPoints.clear();
		checkClick = false;
		
		Button finishButton = (Button) findViewById(R.id.livinglabContextFinishSpatialButton);
		finishButton.setEnabled(false);
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
		
		
		Button finishButton = (Button) findViewById(R.id.livinglabContextFinishSpatialButton);
		if(!finishButton.isEnabled())
			finishButton.setEnabled(true); //enable button after the map is clicked.
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
	
	}

//	@Override
//	public void onBackPressed() {
//	}
	
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
	
	public void livingLabSettings(){
		Log.v(TAG, "LivingLabSettings");
	}

}