package com.mop.friendflare;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class GeneralUtils {
	
	public static String CONTENT_URI_SENT_MESSAGES = "content://sms/sent";
	
	public static void insertSMS(Context context, String address, String body)
	{
		ContentResolver resolver = null;
		resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("address", address);
		values.put("body", body);
		resolver.insert(Uri.parse(CONTENT_URI_SENT_MESSAGES), values);
	}
}
