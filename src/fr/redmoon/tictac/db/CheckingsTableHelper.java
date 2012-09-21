package fr.redmoon.tictac.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fr.redmoon.tictac.bus.bean.DayBean;

public class CheckingsTableHelper extends TableHelper {

	public static final String TABLE_NAME = "checkings";

	// Date du jour, entier au format yyyymmdd 
	public static final String COL_DATE = "date";
	//private static final int COL_DATE_INDEX = 0;
	
	// Pointage
	public static final String COL_TIME = "time";
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
	
	public boolean createRecord(final SQLiteDatabase db, final long dayId, final int time) {
		final ContentValues values = new ContentValues();
		values.put(COL_DATE, dayId);
		values.put(COL_TIME, time);
		
		return createRecord(db, values) != -1;
	}
	
	public boolean createRecords(final SQLiteDatabase db, final DayBean day) {
		boolean done = true;
		if (day.checkings != null) {
			final ContentValues values = new ContentValues();
			values.put(COL_DATE, day.date);
			
			for (Integer time : day.checkings) {
				values.put(COL_TIME, time);
				done &= createRecord(db, values) != -1;
			}
		}
		return done;
	}
	
	public void fetchCheckings(final SQLiteDatabase db, final long date, final List<Integer> listToFill) {
		final String whereClause = COL_DATE + "=" + date;
		final String orderClause = COL_TIME + " asc";
		final Cursor cursor = fetchWhere(db, whereClause, orderClause);
		final int nbCheckings = cursor.getCount();
		
		// On vide la liste avant de la remplir
		listToFill.clear();
		
		// On remplit la liste
		if (nbCheckings > 0) {
			do {
				listToFill.add(cursor.getInt(COL_TIME_INDEX));
			} while (cursor.moveToNext());
		}
		cursor.close();
	}
	
	public List<Integer> fetchCheckings(final SQLiteDatabase db, final long date) {
		final List<Integer> listToFill = new ArrayList<Integer>();
		fetchCheckings(db, date, listToFill);
		return listToFill;
	}
	
	public void fetchCheckings(final SQLiteDatabase db, final DayBean beanToFill) {
		if (beanToFill.checkings == null) {
			beanToFill.checkings = new ArrayList<Integer>();
		}
		fetchCheckings(db, beanToFill.date, beanToFill.checkings);
	}
	
	public boolean updateRecord(final SQLiteDatabase db, final long dayId, final int oldTime, final int newTime) {
		final ContentValues values = new ContentValues();
		values.put(COL_DATE, dayId);
		values.put(COL_TIME, newTime);
		
		return updateRecord(db, dayId, oldTime, values);
	}
	
	public boolean updateRecords(final SQLiteDatabase db, final DayBean day) {
		// On supprime les pointages de ce jour
		delete(db, day.date);
		
		// Et on ajoute les nouveaux pointages
		return createRecords(db, day);
	}
	
	public boolean delete(final SQLiteDatabase db, final long date, final int checking) {
		return deleteWhere(
				db, 
				COL_DATE + "=" + date + " and "
				+ COL_TIME + "=" + checking) != 0;
	}
	
	public boolean exists(final SQLiteDatabase db, final long date, final int checking) {
		final Cursor result = fetchWhere(db, COL_DATE + "=" + date + " and " + COL_TIME + "=" + checking);
		final boolean exists = result.getCount() > 0;
		result.close();
		return exists;
	}
}
