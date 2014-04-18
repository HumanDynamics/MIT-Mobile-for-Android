package edu.mit.mitmobile2.livinglabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.mitmobile2.objs.LivingLabContextItem;
import edu.mit.mitmobile2.objs.LivingLabItem;
import edu.mit.mitmobile2.objs.LivingLabSettingItem;
import edu.mit.mitmobile2.objs.LivingLabVisualizationItem;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

 
public class LivingLabAccessControlActivity extends Activity implements OnClickListener {
	private static final String TAG = "LLAccessControlActivity";
    
    private LivingLabsAccessControlDB mLivingLabAccessControlDB;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLivingLabAccessControlDB = LivingLabsAccessControlDB.getInstance(this);
        ArrayList<LivingLabContextItem> llciArray = mLivingLabAccessControlDB.retrieveLivingLabContextItem();
        Intent activityIntent;
        if(llciArray == null){
        	activityIntent = new Intent(LivingLabAccessControlActivity.this, LivingLabContextActivity.class);
        } else{
        	activityIntent = new Intent(LivingLabAccessControlActivity.this, LivingLabSettingsActivity.class);
        }
    	LivingLabItem labItem = (LivingLabItem) getIntent().getSerializableExtra("lab");
    	activityIntent.putExtra("lab", labItem);
    	LivingLabAccessControlActivity.this.startActivity(activityIntent);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
	
}