package fr.redmoon.tictac.bus.export;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.activities.TicTacActivity.OnDayDeletionListener;

public class CalendarAccess implements OnDayDeletionListener {
	public static final Uri EVENTS_CONTENT_URI = Uri.parse("content://com.android.calendar/events");
	public static final String WORKEVENT_TITLE = "Travail";
	public static final String CALENDAR_NAME = "Welltime";
	
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
	
	private CalendarAccess() {
		// Demande aux autres vues de nous notifier en cas de suppression de jour
		TicTacActivity.registerDayDeletionListener(this);
	}
	
	public static CalendarAccess getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CalendarAccess(); 
		}
		return INSTANCE;
	}

	public void initAccess(final Activity activity) {
		mCalID = -1;
		if (!PreferencesBean.instance.syncCalendar || activity == null) {
			return;
		}
		mActivity = activity;
		
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
		    if (CALENDAR_NAME.equals(displayName)) {
		    	mCalID = calID;
		    	break;
		    }
		}
	}

	private void addWorkEvent(final int year, final int month, final int dayOfMonth, final int inTime, final int outTime) {
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
		values.put("title"/*Events.TITLE*/, WORKEVENT_TITLE);
		//values.put("description"/*Events.DESCRIPTION*/, "Group workout");
		values.put("calendar_id"/*Events.CALENDAR_ID*/, mCalID);
		Uri uri = cr.insert(EVENTS_CONTENT_URI/*Events.CONTENT_URI*/, values);
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
	
	public void createWorkEvents(final long date, final List<Integer> checkings) {
		if (!PreferencesBean.instance.syncCalendar || mActivity == null || mCalID == -1) {
			return;
		}
		
		// Extraction des infos de la date
		final int year = DateUtils.extractYear(date);
		final int month = DateUtils.extractMonth(date);
		final int dayOfMonth = DateUtils.extractDayOfMonth(date);
		
		// Suppression de tous les évènements de ce type de la journée
		// TODO Pour être propre il faudrait enregistrer l'ID des évènements
		// créés pour tel et tel pointage, pour mettre à jour cet évènement
		// en cas de modification des pointages associés.
		deleteWorkEvents(year, month, dayOfMonth);
		
		// Ajout des pointages par deux dans un évènement
		Integer in = null;
		for (Integer checking : checkings) {
			if (in == null) {
				in = checking;
			} else {
				addWorkEvent(year, month, dayOfMonth, in, checking);
				in = null;
			}
		}
	}
	
	public void createDayTypeEvent(final long date, final int typeMorning, final int typeAfternoon) {
		if (!PreferencesBean.instance.syncCalendar || mActivity == null || mCalID == -1) {
			return;
		}
		
		// Extraction des infos de la date
		final int year = DateUtils.extractYear(date);
		final int month = DateUtils.extractMonth(date);
		final int dayOfMonth = DateUtils.extractDayOfMonth(date);
		
		// Suppression de tous les évènements de ce type de la journée
		// TODO Pour être propre il faudrait enregistrer l'ID des évènements
		// créés pour tel et tel pointage, pour mettre à jour cet évènement
		// en cas de modification des pointages associés.
		deleteDayTypeEvents(year, month, dayOfMonth);
		
		// Ajout d'un évènement durant toute la journée
		final String[] dayTypes = mActivity.getResources().getStringArray(R.array.dayTypesEntries);
		final StringBuilder type = new StringBuilder(dayTypes[typeMorning]);
		if (typeMorning != typeAfternoon) {
			type.append(" / ").append(dayTypes[typeAfternoon]);
		}
		addDayTypeEvent(year, month, dayOfMonth, type.toString());
	}
	
	private void addDayTypeEvent(final int year, final int month, final int dayOfMonth, final String type) {
		// Création des heures de l'évènement en millisecondes
		Calendar beginTime = Calendar.getInstance();
		beginTime.set(year, month, dayOfMonth, 0, 1);
		final long startMillis = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(year, month, dayOfMonth, 0, 2);
		final long endMillis = endTime.getTimeInMillis();

		// Création de l'évènement dans le calendrier
		ContentResolver cr = mActivity.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("dtstart"/*Events.DTSTART*/, startMillis);
		values.put("dtend"/*Events.DTEND*/, endMillis);
		values.put("title"/*Events.TITLE*/, type);
		values.put("allDay"/*Events.ALL_DAY*/, "1");
		//values.put("eventTimezone"/*Events.TIMEZONE_UTC*/, "UTC");
		//values.put("description"/*Events.DESCRIPTION*/, "Group workout");
		values.put("calendar_id"/*Events.CALENDAR_ID*/, mCalID);
		Uri uri = cr.insert(EVENTS_CONTENT_URI/*Events.CONTENT_URI*/, values);
		if (uri == null) {
			Toast.makeText(mActivity, "Erreur lors de l'insertion de l'évènement dans le calendrier.", Toast.LENGTH_SHORT).show();
		}
	}

	public void deleteWorkEvents(final int year, final int month, final int dayOfMonth) {
		if (mActivity == null || mCalID == -1) {
			return;
		}
		
		// Création des heures de l'évènement en millisecondes
		Calendar beginTime = Calendar.getInstance();
		beginTime.set(year, month, dayOfMonth, 0, 0);
		final long dayStart = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(year, month, dayOfMonth, 23, 59);
		final long dayEnd = endTime.getTimeInMillis();
		
		mActivity.getContentResolver().delete(
			EVENTS_CONTENT_URI,
			"dtstart >= ? AND dtend <= ? AND title = ?",
			new String[]{ String.valueOf(dayStart), String.valueOf(dayEnd), WORKEVENT_TITLE });
		
		// Run query
//		Cursor cur = null;
//		ContentResolver cr = mActivity.getContentResolver();
//		Uri uri = Uri.parse("content://com.android.calendar/calendars");//Calendars.CONTENT_URI;   
//		String selection = "((" + "account_name"/*Calendars.ACCOUNT_NAME*/ + " = ?) AND (" 
//		                        + "account_type"/*Calendars.ACCOUNT_TYPE*/ + " = ?) AND ("
//		                        + "ownerAccount"/*Calendars.OWNER_ACCOUNT*/ + " = ?))";
//		String[] selectionArgs = new String[] {"marekh.ebony@gmail.com", "com.google", "marekh.ebony@gmail.com"}; 
//		// Submit the query and get a Cursor object back. 
//		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
//
//        cur = cr.query(
//        	EVENTS_CONTENT_URI,
//        	new String[]{ "_id" },
//        	"dtstart >= ? AND dtend <= ? AND title = ?",
//        	new String[]{ String.valueOf(dayStart), String.valueOf(dayEnd), WORKEVENT_TITLE },
//        	null);
//		
//		while (cur.moveToNext()) {		    
//		    // Récupération de l'identifiant de l'évènement
//		    long eventID = cur.getLong(0);
//		              
//		    // Suppression de l'évènement
//		    Uri deleteUri = ContentUris.withAppendedId(EVENTS_CONTENT_URI, eventID);
//		    cr.delete(deleteUri, null, null);
//		}
	}
	
	public void deleteDayTypeEvents(final int year, final int month, final int dayOfMonth) {
		if (mActivity == null || mCalID == -1) {
			return;
		}
		
		// Création des heures de l'évènement en millisecondes
		Calendar beginTime = Calendar.getInstance();
		beginTime.set(year, month, dayOfMonth, 0, 0);
		final long dayStart = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(year, month, dayOfMonth, 23, 59);
		final long dayEnd = endTime.getTimeInMillis();
		
		mActivity.getContentResolver().delete(
			EVENTS_CONTENT_URI,
			"dtstart >= ? AND dtend <= ? AND allDay = ?",
			new String[]{ String.valueOf(dayStart), String.valueOf(dayEnd), "1" });
	}
	
	public void deleteEvents(final int year, final int month, final int dayOfMonth) {
		if (mActivity == null || mCalID == -1) {
			return;
		}
		
		// Création des heures de l'évènement en millisecondes
		Calendar beginTime = Calendar.getInstance();
		beginTime.set(year, month, dayOfMonth, 0, 0);
		final long dayStart = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(year, month, dayOfMonth, 23, 59);
		final long dayEnd = endTime.getTimeInMillis();
		
		mActivity.getContentResolver().delete(
			EVENTS_CONTENT_URI,
			"dtstart = ? AND dtend = ?",
			new String[]{ String.valueOf(dayStart), String.valueOf(dayEnd) });
	}
	
	@Override
	public void onDeleteDay(long date) {
		if (!PreferencesBean.instance.syncCalendar || mActivity == null || mCalID == -1) {
			return;
		}
		
		// Extraction des infos de la date
		final int year = DateUtils.extractYear(date);
		final int month = DateUtils.extractMonth(date);
		final int dayOfMonth = DateUtils.extractDayOfMonth(date);
				
		// Suppression des évènements de ce jour
		deleteEvents(year, month, dayOfMonth);
	}
}
