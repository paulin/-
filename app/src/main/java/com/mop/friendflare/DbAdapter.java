package com.mop.friendflare;


import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class DbAdapter {
	
		
	/**
	 * This must be incremented every time the system is deployed and the database is to be changed. 
	 * WARNING... BY CHANGING THIS NUMBER ALL PRIOR DATA WILL BE DUMPED 
	 * upgradeDB will be called;
	 */
    public static final int DATABASE_VERSION = 1;
		
    private static final String TAG = "DbAdapter";
    private static DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    //This will be the name of the db file at /data/data/com.sporcle/databases

	
	private static final String TABLE_LOC_REQUEST = "locationrequest";
    	    
    private static final String DB_SETUP = "PRAGMA foreign_keys = ON;";
    private static final String DB_DROP1 = "DROP TABLE IF EXISTS " + TABLE_LOC_REQUEST + ";";
       
    private static final String DB_LOC_REQUEST = "create table " + TABLE_LOC_REQUEST + "(" + 
	"   _id integer primary key autoincrement," + 
	" 	locationreceiveddate integer not null default CURRENT_TIMESTAMP," +
	"   locationstate text not null," + 
	"   reqfrom text not null," + 
	"   reqfromname text not null," + 
	"   locreqnote text not null" +
	");";


	
    private final Context mCtx;

    /**
     * Special listener that is used for upgrading the database
     * @author paulin
     *
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

    	public static final String TAG = "DatabaseHelper";
    	
        DatabaseHelper(Context context) {
            super(context, ResourceInstaller.DATABASE_NAME, null, DbAdapter.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, "Creating Database in thread [" + Thread.currentThread().getName() + "]");
        	createDatabase(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            
        	upgradeDatabase(db);
        }
        
        /**
         * Called when the database is first created, by the Database Helper
         */
        public void createDatabase(SQLiteDatabase db) {
        	Log.i(TAG, "Creating Database");
            db.execSQL(DbAdapter.DB_SETUP);
                //I don't believe we need the following lines, we are only creating.
            //mDb.execSQL(DB_DROP1);       
            db.execSQL(DB_LOC_REQUEST);                               		
        }

        /**
         * Called when the database is to be upgraded by the Database Helper
         */
        public void upgradeDatabase(SQLiteDatabase db) {    	
        	Log.i(TAG, "Upgrading Database");
        	db.execSQL(DB_DROP1);
        	createDatabase(db);    	
        }
    }
    
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;            
    }
    
    public void close() {
        mDbHelper.close();
    }    
    

    
    /**
     * Will drop and rebuild the db.  Starts over
     */
    public void rebuildDB(SQLiteDatabase db) {
        db.execSQL(DB_SETUP);
        db.execSQL(DB_DROP1);            
        db.execSQL(DB_LOC_REQUEST);                  
    }
              

    /**
     * Returns a list of games most recently played, the max number will be the limit.
     * 
     * @return
     */
    public ArrayList<LocationRequest> fetchAllLocationRequests() {
    	ArrayList<LocationRequest> historyArray = new ArrayList<LocationRequest>();
    	Cursor cursor =  mDb.query(
    			TABLE_LOC_REQUEST,
    			new String[] {}, 
    			null, 
    			new String[] {}, 
    			null, 
    			null, 
    			LocationRequest.LOCATION_REQUEST_DATE + " desc", 
    			null);
		LocationRequest history = null;
		int count = cursor.getCount();
		cursor.moveToFirst();
		Log.v(TAG, "Count Location Request" + count );
		for(int i = 0; i < count; i++) {
			cursor.moveToPosition(i);
			history = new LocationRequest();
			history.loadFromCursor(cursor);
			historyArray.add(history);
		}
        cursor.close();
    	
    	return historyArray;
    	
    }

    /**
     * Returns an arraylist of history in the new state
     * 
     * @param	state	The category you are interested in
     */
    public ArrayList<LocationRequest> fetchLocationRequest(LocationRequestState state) {
    	ArrayList<LocationRequest> historyArray = new ArrayList<LocationRequest>();
    	Log.v(LogConstants.MATT_TAG, "Fetching Location for State [" + state.name() + "]");
		Cursor cursor = mDb.query(
	    			TABLE_LOC_REQUEST,
	    			new String[] {}, 
	    			LocationRequest.LOCATION_STATE+ " = ?" , 
	    			new String[] {"" + state.name()}, 
	    			null, 
	    			null, 
	    			LocationRequest.LOCATION_REQUEST_DATE + " desc", 
	    			null);
			
		
		LocationRequest history = null;
		int count = cursor.getCount();
		cursor.moveToFirst();
		Log.v(TAG, "Count Location Request" + count );
		for(int i = 0; i < count; i++) {
			cursor.moveToPosition(i);
			history = new LocationRequest();
			history.loadFromCursor(cursor);
			historyArray.add(history);
		}
        cursor.close();
    	
    	return historyArray;
    }

   
    public void saveLocationRequest(LocationRequest request){
    	ContentValues vals = request.getContentValues();
    	Log.v(TAG, "LocationRequest going in " + request.toString());
    	if(request.isChanged()){
    		if(request.isPersistant()){
    			if(mDb.update(TABLE_LOC_REQUEST, vals, LocationRequest.ROWID + "=" + request.getId(), null) > 0){
    				request.setChanged(false);
    			}else{
    				Log.e("DbAdapter", "Could not save the location request");
    			}
    		}else{
    			Long result = mDb.insert(TABLE_LOC_REQUEST, null, vals);
    			if(result > 0){
    				LocationRequest resultLocationRequest = fetchLocationRequest(result);
    		    	//Log.v(TAG, "HISTORY merging to history " + history.toString());
    				request.merge(resultLocationRequest);
    				request.setChanged(false);
    			}else{
    				Log.e(TAG, "Could not fetch the locationRequest");
    			}
    		}
    	}else{
			Log.w(TAG, "LocationRequest not saved, there were no changes");
    	}
    	//Log.v(TAG, "HISTORY coming out " + history.toString());
    }     
    
    /**
     * Return the game note
     * @param rowId
     * @return
     * @throws SQLException
     */
    public LocationRequest fetchLocationRequest(long rowId) throws SQLException {

        Cursor mCursor = mDb.query(true, TABLE_LOC_REQUEST, LocationRequest.COLUMNS,
        		LocationRequest.ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            LocationRequest result = new LocationRequest();
            result.loadFromCursor(mCursor);
            
            mCursor.close();
            return result;
        }

        return null;
    }    
    
    public void deleteAllLocationRequests() throws SQLException {
    	mDb.execSQL("delete from " + TABLE_LOC_REQUEST);
    }
}
