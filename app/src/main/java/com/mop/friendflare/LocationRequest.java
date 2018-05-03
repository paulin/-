package com.mop.friendflare;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;

public class LocationRequest extends BaseModelObj {

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm aa");
	
	public static final String TAG = "LocationRequest";
	
	private static final long serialVersionUID = 1L;

	// Database column names
	public static final String LOCATION_REQ_FROM = "reqfrom";	
	public static final String LOCATION_REQ_FROM_NAME = "reqfromname";
	public static final String LOCATION_REQUEST_DATE = "locationreceiveddate";
	public static final String LOCATION_STATE = "locationstate";	
	public static final String LOCATION_REQ_NOTE = "locreqnote";
	
	static final String[] COLUMNS = new String[] { ROWID, LOCATION_REQ_FROM, LOCATION_REQ_FROM_NAME,
			LOCATION_REQUEST_DATE, LOCATION_STATE, LOCATION_REQ_NOTE};

	private Date locReqDate;
	private LocationRequestState locationState;
	private String reqfrom;
	private String whoRequested;
	private String reqNote;
	
	public LocationRequest() {
		super();
		locationState = LocationRequestState.NEW;
		reqNote = "";
		locReqDate = new Date();
		reqfrom = "";
		whoRequested = "";
	}

	
	@Override
	void buildFromCursor(Cursor cursor) {
		if (cursor == null) {
			return;
		}

		reqfrom = cursor.getString(cursor.getColumnIndexOrThrow(LOCATION_REQ_FROM));
		whoRequested = cursor.getString(cursor.getColumnIndexOrThrow(LOCATION_REQ_FROM_NAME));
		reqNote = cursor.getString(cursor.getColumnIndexOrThrow(LOCATION_REQ_NOTE));
		locReqDate = convertLongToDate(cursor.getLong(cursor.getColumnIndexOrThrow(LOCATION_REQUEST_DATE)));		
		locationState = LocationRequestState.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(LOCATION_STATE)));
		
		setLoaded(true);
	}

	@Override
	ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(LOCATION_REQ_FROM, reqfrom);		
		if(this.locReqDate != null)
			values.put(LOCATION_REQUEST_DATE, this.locReqDate.getTime());
		values.put(LOCATION_STATE, locationState.name());
		values.put(LOCATION_REQ_FROM_NAME, whoRequested);
		values.put(LOCATION_REQ_NOTE, reqNote);
		
		return values;
	}

	/**
	 * Moves the values from the provided game into this game, this should be used only when loading from the
	 * db. 
	 * 
	 * @param req
	 */
	public void merge(LocationRequest req) {
		//These don't change 
		this.setId(req.getId());
		this.setLocationState(req.getLocationState());
		this.setLocReqDate(req.getLocReqDate());
		this.setReqFrom(req.getReqFrom());
		this.setReqNote(req.getReqNote());
		this.setWhoRequested(req.getWhoRequested());
	}	
	
	/**
	 * Turns this history object into a string
	 * 
	 * @return
	 */
	public String toString() {
		return String.format("LocationRequest id[%d] state[%s] request date[%s] from[%s] who[%s] note[%s]", 
				this.getId(), this.getLocationState().name(), DATE_FORMAT.format(this.getLocReqDate()), this.getReqFrom(), this.getWhoRequested(), this.getReqNote());
	}



	public String getWhoRequested() {
		return whoRequested;
	}


	public void setWhoRequested(String whoRequested) {
		this.whoRequested = whoRequested;
		this.setChanged(true);
	}


	public Date getLocReqDate() {
		return locReqDate;
	}


	public void setLocReqDate(Date locReqDate) {
		this.locReqDate = locReqDate;
		this.setChanged(true);
	}


	public LocationRequestState getLocationState() {
		return locationState;
	}


	public void setLocationState(LocationRequestState locationState) {
		this.locationState = locationState;
		this.setChanged(true);
	}


	public String getReqFrom() {
		return reqfrom;
	}


	public void setReqFrom(String reqfrom) {
		this.reqfrom = reqfrom;
		this.setChanged(true);
	}


	public String getReqNote() {
		return reqNote;
	}


	public void setReqNote(String reqNote) {
		this.reqNote = reqNote;
		this.setChanged(true);
	}
}
