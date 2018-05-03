package com.mop.friendflare;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.whereuat.free.R;
import com.pugetworks.utils.GeneralUtils;
import com.pugetworks.utils.GoogleAnalytics;
import com.pugetworks.utils.LogConstants;

public class InviteFriendsActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = "InviteFriendsActivity";
	public static final String SMS_URI_STRING = "content://mms-sms/conversations/";
    public static final String[] PROJECTION = new String[]{"*"};
    public static final String INVITE_MESSAGE = "";
    public static final String GROUP_CHAT_TEXT = "GROUP CHAT";
    public static final String NOT_A_CONTACT = "NOT A CONTACT";
	private LayoutInflater mInflater;
	private ListView list;
	SimpleCursorAdapter mAdapter;
	Cursor cursor;
	ArrayList<String> selectedContacts = new ArrayList<String>();
	boolean [] isSelected; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_friends);
		
		Button dismissButton = (Button) findViewById(R.id.dismiss_dialog_button);
		dismissButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		        finish();
		    }
		});
		
		Button inviteButton = (Button) findViewById(R.id.invite_friends_button);
		inviteButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
		    	progressBar.setVisibility(View.VISIBLE);
		        sendTextMessages();
		       
		     //  progressBar.setVisibility(View.GONE);
		        finish();
		    }
		});
		
		
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		list = (ListView) findViewById(R.id.contacts_list);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			
		String[] fromColumns = {"address"};		
		int[] toViews = {R.id.check_box};


//		mAdapter = new SimpleCursorAdapter(this, R.layout.contact_row, null,
//				fromColumns, toViews, 0);  Why is this here twice?
		
		
		mAdapter = new SimpleCursorAdapter(this, R.layout.contact_row, null,
				fromColumns, toViews, 0)
		{			
		 	public void setViewText(TextView v, String text) {
				super.setViewText(v, getContactName(text));
		 	}   
		};

		mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
					final String address = cursor.getString(cursor.getColumnIndex("address"));
					final int row = cursor.getPosition();
					view.setTag(cursor.getPosition());
					
					if (view instanceof CheckBox) {
						CheckBox  cb = (CheckBox) view;
						//int tag =  ((Integer)view.getTag()).intValue();
						//((TextView)view).setText(getContactName(address));
						//((TextView)view).setText("HELLO!");
						//cb.setChecked(isSelected[tag]);
						
						cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								int tag =  ((Integer)buttonView.getTag()).intValue();
								isSelected[tag] = isChecked;
							//	Log.v("XENIA", "Checkbox at " + buttonView.getTag() +  " - CHECKED CHANGED to " + isChecked);
								if(isChecked){
									String contactName = getContactName(address);
									if(!contactName.equals(GROUP_CHAT_TEXT) && !contactName.equals(NOT_A_CONTACT)) {
										selectedContacts.add(address);											
										Log.v("XENIAH", getContactName(address) + "Added to the list");
									}
									//Toast.makeText(InviteFriendsActivity.this, msg, Toast.LENGTH_LONG).show();
								}			
							}
						}
						
					);
					
				   cb.setChecked(isSelected[row]);
				}
				return false;
			}
		});
      
		list.setAdapter(mAdapter);
		this.getSupportLoaderManager().initLoader(0, null, this);
		list.setClickable(true);
	}

	// Called when a new Loader needs to be created
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final Uri uri = Uri.parse(SMS_URI_STRING);
		return new CursorLoader(this, uri, PROJECTION, null, null, "normalized_date desc limit 40");  
	}

	// Called when a previously created loader has finished loading
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mAdapter.swapCursor(data);
		//Cursor cur = this.getContentResolver().query(Uri.parse( "content://mms-sms/conversations"), new String[]{"*"}, null, null, "normalized_date desc limit 10" );
		//this.cursor = cur;
		//mAdapter.swapCursor(cur);
		
		for(int i =0; i < mAdapter.getCount(); i++) {
			String temp = mAdapter.getItem(i).toString();
			Log.i(LogConstants.MATT_TAG, "Adapter = " + temp);
		}
		
		this.cursor = data;
		this.isSelected = new boolean[mAdapter.getCount()];
	}

	// Called when a previously created loader is reset, making the data
	// unavailable
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
		this.cursor = null;
		this.isSelected = null;
	}
   
	public String getContactName(String address)
	{
		if(address == null || address.trim().length() == 0) {
			return GROUP_CHAT_TEXT;
		} else {
			String contact=address;
	        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));  
	        Cursor cursor = this.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME},PhoneLookup.NUMBER+"='"+address+"'",null,null);
	
	        String temp = "";
	        if(cursor.getCount() > 0)
	        {
	        	cursor.moveToFirst();
	        	contact =cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));

	        } else {
        		contact = NOT_A_CONTACT;	        	
	        }
	        Log.i(LogConstants.MATT_TAG, "CONTACT = " + contact + "TEMP = ["+ temp + "] ADDRESS = " +  address);
	        cursor.close();
	        return contact;
		}
	}
	
	
	public void sendTextMessages()
	{
		if(this.selectedContacts != null)
		{
			for(String contactAddress: this.selectedContacts)
			{
				String msg = this.getResources().getString(R.string.invite_message);
				//String msg = "If you ever want to know my location, all you have to do is text me with the phrase '@?', and you will get a text reply with a map link to my current position. Read more at http://www.whereu-at.com.";
	    		SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(contactAddress, null, msg, null, null);
				GeneralUtils.insertSMS(InviteFriendsActivity.this, contactAddress, msg);
				Log.v(TAG, "SMS info message sent to " + getContactName(contactAddress));	
				Toast.makeText(InviteFriendsActivity.this, msg, Toast.LENGTH_LONG).show();				
			}
			if(this.selectedContacts.size() > 0) {
				EasyTracker.getTracker().sendEvent(TAG, GoogleAnalytics.INVITE_MESSAGE_SENT, "invite", new Long(this.selectedContacts.size()));				
			}
		}
	}
	
	
	

}
