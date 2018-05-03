package com.mop.friendflare;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;


public class MessageReceiver extends BroadcastReceiver {
	private static final String TAG = "MessageReceiver";
	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	
	public static final String MAGIC_WORD = "@?";
	public static final String NO_NAME = "???";
	
	public void onReceive(Context context, Intent intent) {
		Log.i(LogConstants.MATT_TAG, "Text Received");
		if (SMS_RECEIVED.equals(intent.getAction())) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

			Bundle bundle = (Bundle) intent.getExtras().clone();
			Object messages[] = (Object[]) bundle.get("pdus");

			// context.startService(respond_intent);

			SmsMessage smsMessage[] = new SmsMessage[messages.length];
			for (int n = 0; n < messages.length; n++) {
				smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
			}
			String msgBody = smsMessage[0].getMessageBody();
			
			//Look for the magic word
			if(msgBody.contains(MAGIC_WORD) && (msgBody.length() == MAGIC_WORD.length())) {
				//Showtime
				String fromNumber = smsMessage[0].getDisplayOriginatingAddress();
				
				fromNumber = getOnlyNumerics(fromNumber);
				// create a new intent and have the user do something?

				if (fromNumber.length() < 7) {
					Log.v(TAG, "From shortcode: " + fromNumber);
					// short circuit in case for shortcodes
					return;
				}
				
				//Make sure the this number is in the address book
				String name = getContactDisplayNameByNumber(fromNumber, context);
				if(!name.equals(NO_NAME)) {
					//Create a new location request
					LocationRequest request = new LocationRequest();
					request.setLocReqDate(new Date());
					request.setReqFrom(fromNumber);
					request.setWhoRequested(name);
					request.setLocationState(LocationRequestState.NEW);
					request.setChanged(true);
					
					//Add it to the database
					DbAdapter mDbHelper =  new DbAdapter(context);

					mDbHelper.open();
					mDbHelper.saveLocationRequest(request);
					mDbHelper.close();
					
					//Fire up the service
					Intent i = new Intent();
//					i.putExtra(WhereMain.FROM_NUMBER, fromNumber);  Doesn't seem to work for later versions
					i.setClass(context, GPSService.class);
					context.startService(i);
					
//					initService(context);					
				}				
			}
		}
	}
	
	public String getContactDisplayNameByNumber(String number, Context context ) {
	    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	    String name = NO_NAME;

	    ContentResolver contentResolver = context.getContentResolver();
	    Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
	            ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

	    try {
	        if (contactLookup != null && contactLookup.getCount() > 0) {
	            contactLookup.moveToNext();
	            name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
	            //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
	        }
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }

	    return name;
	}
	
	public static String getOnlyNumerics(String str) {
	    
	    if (str == null) {
	        return null;
	    }

	    StringBuffer strBuff = new StringBuffer();
	    char c;
	    
	    for (int i = 0; i < str.length() ; i++) {
	        c = str.charAt(i);
	        
	        if (Character.isDigit(c)) {
	            strBuff.append(c);
	        }
	    }
	    return strBuff.toString();
	}	
}
