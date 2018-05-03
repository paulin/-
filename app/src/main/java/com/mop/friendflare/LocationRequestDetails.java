package com.mop.friendflare;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import com.whereuat.free.R;
import com.pugetworks.database.DbAdapter;
import com.pugetworks.database.LocationRequest;
import com.pugetworks.utils.LogConstants;
import com.pugetworks.utils.PWBaseActivity;

/**
 * Displays a list of comments
 * @author paulin
 *
 */
public class LocationRequestDetails extends PWBaseActivity {	
	public static final String TAG = "LocationRequestDetails";
    private Long locationRequestID;
    
    private TextView fromNumberTextView;
    private TextView fromDateTextView;    
    private TextView fromResponse;
    
    private WebView messageResponseView;
    
	private DbAdapter mDbHelper;	
    
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_request_details);
        
        this.locationRequestID = savedInstanceState != null ? savedInstanceState
				.getLong(FriendMain.LOC_REQUEST_ID) : null;
		if (this.locationRequestID == null) {
			Bundle extras = getIntent().getExtras();
			this.locationRequestID = extras != null ? extras.getLong(FriendMain.LOC_REQUEST_ID)
					: null;
		}		
				
		mDbHelper = new DbAdapter(this);
		
		fromNumberTextView = (TextView)this.findViewById(R.id.location_request_from_number);
		fromDateTextView = (TextView)this.findViewById(R.id.location_request_from_date);
		fromResponse = (TextView)this.findViewById(R.id.location_request_response);
		messageResponseView = (WebView)this.findViewById(R.id.location_request_message);		
    }
    
    
   

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(WhereMain.LOC_REQUEST_ID, this.locationRequestID);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		this.locationRequestID = state != null ? state.getLong(WhereMain.LOC_REQUEST_ID) : null;
	}

		
	@Override
	protected void onPause() {
		super.onPause();
		//Log.v(TAG, "MATT onPause Called");
	}

	@Override
	protected void onStart() {
		super.onStart();
		//Log.v(TAG, "MATT onStart Called");
	}

	
	@Override
	protected void onStop() {
		super.onStop();
		//Log.v(TAG, "MATT onStop Called");
		//holder.clear();
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(LogConstants.MATT_TAG, "onResume Called, locationRequestId [" + locationRequestID + "]");
        fillData();
	}    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	return super.onContextItemSelected(item);
	}

	/**
	 * Loads the list data from the database
	 */
	private void fillData(){
		//Log.d(TAG, "MATT: fillData start at index [" + startIndex + "] current count [" + holder.getCount() + "] from origin : " + origin);
		if(locationRequestID != null) {
			mDbHelper.open();
			LocationRequest req = mDbHelper.fetchLocationRequest(locationRequestID);
			if(req != null) {
				this.fromNumberTextView.setText("From: " + req.getReqFrom());
				this.fromDateTextView.setText("Date: " + LocationRequest.DATE_FORMAT.format(req.getLocReqDate()));
				this.fromResponse.setText(req.getReqNote());
				this.messageResponseView.loadData(req.getReqNote(), "text/html; charset=UTF-8", null);
			} else {
				Log.e(LogConstants.MATT_TAG, "No Location Request Found with ID [" + locationRequestID + "]");				
			}
			mDbHelper.close();

			
		} else {
			Log.e(LogConstants.MATT_TAG, "Null Request Location ID Provided");
		}		
	}

	@Override
	public boolean usesGPS() {
		// TODO Auto-generated method stub
		return false;
	}	
}