package fr.redmoon.tictac.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import fr.redmoon.tictac.bus.bean.DayBean;

public class CheckingsTableHelper extends TableHelper {

	public static final String TABLE_NAME = "checkings";

	// Date du jour, entier au format yyyymmdd 
	private static final String COL_DATE = "date";
	//private static final int COL_DATE_INDEX = 0;
	
	// Pointage
	private static final String COL_TIME = "time";
	private static final int COL_TIME_INDEX = 1;
		
	public CheckingsTableHelper() {
		super(
			TABLE_NAME,
			new String[]{
				COL_DATE,
				COL_TIME
			},
			new String[]{
				SQLiteUtils.DATATYPE_INTEGER,
				SQLiteUtils.DATATYPE_INTEGER
			},
			new String[]{
				SQLiteUtils.CONSTRAINT_PRIMARY_KEY,
				SQLiteUtils.CONSTRAINT_PRIMARY_KEY
			});
	}
	
	public boolean createRecord(final long dayId, final int time) {
		final ContentValues values = new ContentValues();
		values.put(COL_DATE, dayId);
		values.put(COL_TIME, time);
		
		return createRecord(values) != -1;
	}
	
	public boolean createRecords(final DayBean day) {
		boolean done = true;
		if (day.checkings != null) {
			final ContentValues values = new ContentValues();
			values.put(COL_DATE, day.date);
			
			for (Integer time : day.checkings) {
				values.put(COL_TIME, time);
				done &= createRecord(values) != -1;
			}
		}
		return done;
	}
	
	public void fetchCheckings(final DayBean beanToFill) {
		final String whereClause = COL_DATE + "=" + beanToFill.date;
		final String orderClause = COL_TIME + " asc";
		final Cursor cursor = fetchWhere(whereClause, orderClause);
		final int nbDays = cursor.getCount();
		
		List<Integer> checkings = beanToFill.checkings;
		// On vide la liste avant de la remplir
		if (checkings == null) {
			checkings = new ArrayList<Integer>(nbDays);
		} else {
			checkings.clear();
		}
		
		// On remplit la liste
		if (nbDays > 0) {
			do {
				checkings.add(cursor.getInt(COL_TIME_INDEX));
			} while (cursor.moveToNext());
		}
		cursor.close();
	}
	
	public boolean updateRecord(final long dayId, final int oldTime, final int newTime) {
		final ContentValues values = new ContentValues();
		values.put(COL_DATE, dayId);
		values.put(COL_TIME, newTime);
		
		return updateRecord(dayId, oldTime, values);
	}
	
	public boolean updateRecords(final DayBean day) {
		// On supprime les pointages de ce jour
		delete(day.date);
		
		// Et on ajoute les nouveaux pointages
		return createRecords(day);
	}
	
	public boolean delete(final long date, final int checking) {
		return deleteWhere(
				COL_DATE + "=" + date + " and "
				+ COL_TIME + "=" + checking) != 0;
	}
	
	public boolean exists(final long date, final int checking) {
		final Cursor result = fetchWhere(COL_DATE + "=" + date + " and " + COL_TIME + "=" + checking);
		final boolean exists = result.getCount() > 0;
		result.close();
		return exists;
	}
}
