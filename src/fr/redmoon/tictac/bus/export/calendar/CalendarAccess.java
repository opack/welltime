package fr.redmoon.tictac.bus.export.calendar;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import fr.redmoon.tictac.R;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.gui.activities.TicTacActivity;
import fr.redmoon.tictac.gui.activities.TicTacActivity.OnDayDeletionListener;

public class CalendarAccess implements OnDayDeletionListener {
	public static final String WORKEVENT_TITLE = "Travail";
	public static final String CALENDAR_NAME = "Welltime";
	
	public static final ICalendarAPIInfos calendarInfos;
	static {
		final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion >= 14 ) {
			calendarInfos = new CalendarAPIInfosSDK14();
		} else if (sdkVersion >= 8 ) {
			calendarInfos = new CalendarAPIInfosSDK8();
		} else {
			calendarInfos = new CalendarAPIInfosSDKlt8();;
		}
	}
	
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
        cur = cr.query(calendarInfos.getCalendarUri(), new String[]{ calendarInfos.getColumnNameID(), calendarInfos.getColumnNameCalendarDisplayName() }, null, null, null);
		
		// Use the cursor to step through the returned records
		if (cur != null) {
			while (cur.moveToNext()) {		    
			    // Get the field values
			    long calID = cur.getLong(0);
			    String displayName = cur.getString(1);
			              
			    // Sauvegarde de l'ID du calendrier
			    if (CALENDAR_NAME.equalsIgnoreCase(displayName)) {
			    	mCalID = calID;
			    	break;
			    }
			}
		}
	}
	
	private boolean isCalendarAvailable() {
		if (!PreferencesBean.instance.syncCalendar || mActivity == null) {
			return false;
		}
		// Si l'id du calendrier n'a pas encore été récupéré (si par exemple il a été créé entre temps)
		// alors on tente de le récupérer
		if (mCalID == -1) {
			initAccess(mActivity);
			if (mCalID == -1) {
				// Si malgré tout le calendrier n'existe pas, on ne fait rien.
				return false;
			}
		}
		return true;
	}

	public void createEvents(final DayBean day) {
		if (!isCalendarAvailable()) {
			return;
		}
		
		// Extraction des infos de la date
		final int year = DateUtils.extractYear(day.date);
		final int month = DateUtils.extractMonth(day.date);
		final int dayOfMonth = DateUtils.extractDayOfMonth(day.date);
		
		// Création des plages de travail
		createWorkEvents(year, month, dayOfMonth, day.checkings);
		
		// Création du type de jour
		createDayTypeEvent(year, month, dayOfMonth, day.typeMorning, day.typeAfternoon);
	}
	
	public void createWorkEvents(final long date, final List<Integer> checkings) {
		if (!isCalendarAvailable()) {
			return;
		}
		
		// Extraction des infos de la date
		final int year = DateUtils.extractYear(date);
		final int month = DateUtils.extractMonth(date);
		final int dayOfMonth = DateUtils.extractDayOfMonth(date);
		
		createWorkEvents(year, month, dayOfMonth, checkings);
	}
	
	private void createWorkEvents(final int year, final int month, final int dayOfMonth, final List<Integer> checkings) {
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
		values.put("eventTimezone"/*Events.TIMEZONE_UTC*/, "UTC");
		Uri uri = cr.insert(calendarInfos.getEventsContentUri()/*Events.CONTENT_URI*/, values);
		if (uri == null) {
			Toast.makeText(mActivity, "Erreur lors de l'insertion de l'évènement dans le calendrier.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void createDayTypeEvent(final long date, final int typeMorning, final int typeAfternoon) {
		if (!isCalendarAvailable()) {
			return;
		}
		
		// Extraction des infos de la date
		final int year = DateUtils.extractYear(date);
		final int month = DateUtils.extractMonth(date);
		final int dayOfMonth = DateUtils.extractDayOfMonth(date);
		
		createDayTypeEvent(year, month, dayOfMonth, typeMorning, typeAfternoon);
	}
	
	private void createDayTypeEvent(final int year, final int month, final int dayOfMonth, final int typeMorning, final int typeAfternoon) {
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
		// Pour une raison obscure et inconnue l'ajout d'un évènement allDay=1
		// décale de -1 jour. On ajoute donc un jour ici.
		beginTime.add(Calendar.DAY_OF_YEAR, 1);
		final long startMillis = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(year, month, dayOfMonth, 0, 2);
		endTime.add(Calendar.DAY_OF_YEAR, 1);
		final long endMillis = endTime.getTimeInMillis();

		// Création de l'évènement dans le calendrier
		ContentResolver cr = mActivity.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("dtstart"/*Events.DTSTART*/, startMillis);
		values.put("dtend"/*Events.DTEND*/, endMillis);
		values.put("title"/*Events.TITLE*/, type);
		values.put("allDay"/*Events.ALL_DAY*/, "1");
		values.put("eventTimezone"/*Events.TIMEZONE_UTC*/, "UTC");
		//values.put("description"/*Events.DESCRIPTION*/, "Group workout");
		values.put("calendar_id"/*Events.CALENDAR_ID*/, mCalID);
		Uri uri = cr.insert(calendarInfos.getEventsContentUri()/*Events.CONTENT_URI*/, values);
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
			calendarInfos.getEventsContentUri(),
			"calendar_id = ? AND dtstart >= ? AND dtend <= ? AND title = ?",
			new String[]{ String.valueOf(mCalID), String.valueOf(dayStart), String.valueOf(dayEnd), WORKEVENT_TITLE });
	}
	
	public void deleteDayTypeEvents(final int year, final int month, final int dayOfMonth) {
		if (mActivity == null || mCalID == -1) {
			return;
		}
		
		// Création des heures de l'évènement en millisecondes
		Calendar beginTime = Calendar.getInstance();
		beginTime.set(year, month, dayOfMonth, 0, 0);
		// Pour une raison obscure et inconnue l'ajout d'un évènement allDay=1
		// décale de -1 jour. On retire donc un jour ici pour correspondre au
		// jour ajouter lors de addDayTypeEvent.
		//beginTime.add(Calendar.DAY_OF_YEAR, 1);
		final long dayStart = beginTime.getTimeInMillis();
		Calendar endTime = Calendar.getInstance();
		endTime.set(year, month, dayOfMonth, 23, 59);
		//endTime.add(Calendar.DAY_OF_YEAR, 1);
		final long dayEnd = endTime.getTimeInMillis();
		
		int nbDel = mActivity.getContentResolver().delete(
			calendarInfos.getEventsContentUri(),
			"calendar_id = ? AND dtstart >= ? AND dtend <= ? AND allDay = ?",
			new String[]{ String.valueOf(mCalID), String.valueOf(dayStart), String.valueOf(dayEnd), "1" });
		Log.d("Welltime", "CalendarAccess.deleteDayTypeEvents nbDel=" + nbDel);
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
			calendarInfos.getEventsContentUri(),
			"calendar_id = ? AND dtstart >= ? AND dtend <= ?",
			new String[]{ String.valueOf(mCalID), String.valueOf(dayStart), String.valueOf(dayEnd) });
	}
	
	@Override
	public void onDeleteDay(long date) {
		if (!isCalendarAvailable()) {
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
