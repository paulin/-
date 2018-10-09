package com.mop.atat


import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.telephony.SmsMessage
import android.util.Log

/**
 * This class does the actual intercepting of the inbound message, it then starts the GPSService
 * in a separate thread which will deal with the request in the background.
 */
class MessageReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = "MessageReceiver"
        val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"

        val MAGIC_WORD = "@@?"
        val NO_NAME = "???"

        fun getOnlyNumerics(str: String?): String? {

            if (str == null) {
                return null
            }

            val strBuff = StringBuffer()
            var c: Char

            for (i in 0 until str.length) {
                c = str[i]

                if (Character.isDigit(c)) {
                    strBuff.append(c)
                }
            }
            return strBuff.toString()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {

        if (SMS_RECEIVED == intent.action) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            val bundle = intent.extras.clone() as Bundle
            val messages = bundle.get("pdus") as Array<Any>

            // context.startService(respond_intent);

            val smsMessage = arrayOfNulls<SmsMessage>(messages.size)
            for (n in messages.indices) {
                smsMessage[n] = SmsMessage.createFromPdu(messages[n] as ByteArray)
            }
            val msgBody = smsMessage[0]?.getMessageBody()

            //Look for the magic word
            if (msgBody!!.contains(MAGIC_WORD) && msgBody.length == MAGIC_WORD.length) {
                //Showtime
                var fromNumber: String? = smsMessage[0]?.getDisplayOriginatingAddress()

                fromNumber = getOnlyNumerics(fromNumber)
                // create a new intent and have the user do something?

                if (fromNumber!!.length < 7) {
                    Log.v(TAG, "From shortcode: $fromNumber")
                    // short circuit in case for shortcodes
                    return
                }

                //Make sure the this number is in the address book
                val name = getContactDisplayNameByNumber(fromNumber, context)
                if (name != NO_NAME) {
                    //Create a new location request
                    var values = ContentValues()
                    val tempState = LocationRequestState.NEW.type
                    val tempDate = System.currentTimeMillis()
                    values.put(LocationRequestDbManager.COL_STATE, tempState)
                    values.put(LocationRequestDbManager.COL_NUMBER, fromNumber)
                    values.put(LocationRequestDbManager.COL_REQUESTER, name)
                    values.put(LocationRequestDbManager.COL_NOTE, name)
                    values.put(LocationRequestDbManager.COL_REQUEST_DATE, tempDate)

                    //Add it to the database
                    var dbManager = LocationRequestDbManager(context)
                    val mID = dbManager.insert(values)


                    //Dies here, I'm not entirely sure this is how I want this to work anyway.
                    //Fire up the service
                    val i = Intent()
                    //					i.putExtra(WhereMain.FROM_NUMBER, fromNumber);  Doesn't seem to work for later versions
                    i.setClass(context, GPSService::class.java)
                    context.startService(i)

                    //					initService(context);
                }
            }
        }
    }

    fun getContactDisplayNameByNumber(number: String, context: Context): String {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        var name = NO_NAME

        val contentResolver = context.contentResolver
        val contactLookup = contentResolver.query(uri, arrayOf(BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)

        try {
            if (contactLookup != null && contactLookup.count > 0) {
                contactLookup.moveToNext()
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            contactLookup?.close()
        }

        return name
    }
}
