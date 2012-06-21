package fr.redmoon.tictac.bus;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

public class CalendarAccess {
	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] {
	    "_id",//Calendars._ID,                           // 0
	    "account_name",//Calendars.ACCOUNT_NAME,                  // 1
	    "calendar_displayName",//Calendars.CALENDAR_DISPLAY_NAME,         // 2
	    "ownerAccount",//Calendars.OWNER_ACCOUNT                  // 3
	};
	  
	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
//	private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
	private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;//2;
//	private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

	private long mCalID;
	private Activity mActivity; 
	
	private static CalendarAccess INSTANCE; 
	
	private CalendarAccess(final Activity activity) {
		mActivity = activity;
		initAccess();
	}
	
	public static CalendarAccess getInstance(final Activity activity) {
		if (INSTANCE == null) {
			INSTANCE = new CalendarAccess(activity); 
		}
		return INSTANCE;
	}

	private void initAccess() {
		// Run query
		Cursor cur = null;
		ContentResolver cr = mActivity.getContentResolver();
//		Uri uri = Uri.parse("content://com.android.calendar/calendars");//Calendars.CONTENT_URI;   
//		String selection = "((" + "account_name"/*Calendars.ACCOUNT_NAME*/ + " = ?) AND (" 
//		                        + "account_type"/*Calendars.ACCOUNT_TYPE*/ + " = ?) AND ("
//		                        + "ownerAccount"/*Calendars.OWNER_ACCOUNT*/ + " = ?))";
//		String[] selectionArgs = new String[] {"marekh.ebony@gmail.com", "com.google", "marekh.ebony@gmail.com"}; 
//		// Submit the query and get a Cursor object back. 
//		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		
		if (Integer.parseInt(Build.VERSION.SDK) >= 8 ) {
	        cur = cr.query(Uri.parse("content://com.android.calendar/calendars"), new String[]{ "_id", "displayname" }, null, null, null);
		} else {
	        cur = cr.query(Uri.parse("content://calendar/calendars"), new String[]{ "_id", "displayname" }, null, null, null);
		}
		
		// Use the cursor to step through the returned records
		while (cur.moveToNext()) {		    
		    // Get the field values
		    long calID = cur.getLong(PROJECTION_ID_INDEX);
		    String displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
//		    String accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
//		    String ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
		              
		    // Sauvegarde de l'ID du calendrier
		    if ("Welltime".equals(displayName)) {
		    	mCalID = calID;
		    	break;
		    }
		}
	}

	public void addWorkingEvent(final long date, final int inTime, final int outTime) {
		// Extraction des infos de la date
		final int year = DateUtils.extractYear(date);
		final int month = DateUtils.extractYear(date) - 1;
		final int dayOfMonth = DateUtils.extractYear(date);
		
		// Extraction des heures et minutes des pointages
		final int inHour = TimeUtils.extractHour(inTime);
		final int inMinutes = TimeUtils.extractMinutes(inTime);
		final int outHour = TimeUtils.extractHour(outTime);
		final int outMinutes = TimeUtils.extractMinutes(outTime);
		
		// Création des heures de l'évènement en millisecondes
		Calendar beginTime = Calendar.getInstance();
		beginTime.set(year, month, dayOfMonth, inHour, inMinutes);
		final long startMillis = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(year, month, dayOfMonth, outHour, outMinutes);
		final long endMillis = endTime.getTimeInMillis();

		// Création de l'évènement dans le calendrier
		ContentResolver cr = mActivity.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("dtstart"/*Events.DTSTART*/, startMillis);
		values.put("dtend"/*Events.DTEND*/, endMillis);
		values.put("title"/*Events.TITLE*/, "Travail");
		//values.put("description"/*Events.DESCRIPTION*/, "Group workout");
		values.put("calendar_id"/*Events.CALENDAR_ID*/, mCalID);
		Uri uri = cr.insert(Uri.parse("content://com.android.calendar/events")/*Events.CONTENT_URI*/, values);
		if (uri == null) {
			Toast.makeText(mActivity, "Erreur lors de l'insertion de l'évènement dans le calendrier.", Toast.LENGTH_SHORT).show();
		}
		// get the event ID that is the last element in the Uri
		//long eventID = Long.parseLong(uri.getLastPathSegment());
		// 
		// ... do something with event ID
		//
		//
	}

	public void createWorkingEvents(final long date, final List<Integer> checkings) {
		// Suppression de tous les évènements de ce type de la journée
		// TODO Pour être propre il faudrait enregistrer l'ID des évènements
		// créés pour tel et tel pointage, pour mettre à jour cet évènement
		// en cas de modification des pointages associés.
		// TODO
		
		// Ajout des pointages par deux dans un évènement
		Integer in = null;
		for (Integer checking : checkings) {
			if (in == null) {
				in = checking;
			} else {
				addWorkingEvent(date, in, checking);
				in = null;
			}
		}
	}
}
