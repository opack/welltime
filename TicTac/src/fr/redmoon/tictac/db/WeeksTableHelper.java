package fr.redmoon.tictac.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.bus.bean.WeekBean;

public class WeeksTableHelper extends TableHelper {

	public static final String TABLE_NAME = "weeks";

	// Date du jour, entier au format yyyymmdd 
	private static final String COL_DATE = "date";
	private static final int COL_DATE_INDEX = 0;
	
	// HV � cette date
	private static final String COL_FLEX_TIME = "flex";
	private static final int COL_FLEX_TIME_INDEX = 1;
	
	private final ContentValues mTempContentValues = new ContentValues();
		
	public WeeksTableHelper() {
		super(
			TABLE_NAME,
			new String[]{
				COL_DATE,
				COL_FLEX_TIME
			},
			new String[]{
				SQLiteUtils.DATATYPE_INTEGER,
				SQLiteUtils.DATATYPE_INTEGER
			},
			new String[]{
				SQLiteUtils.CONSTRAINT_PRIMARY_KEY,
				SQLiteUtils.CONSTRAINT_NOT_NULL
			});
	}
	
	public boolean createRecord(final SQLiteDatabase db, final long dayId, final int flexTime) {
		final ContentValues values = new ContentValues();
		values.put(COL_DATE, dayId);
		values.put(COL_FLEX_TIME, flexTime);
		
		return createRecord(db, values) != -1;
	}
	
	public boolean createRecord(final SQLiteDatabase db, final WeekBean week) {
		fillContentValues(week);
		return createRecord(db, mTempContentValues) != -1;
	}
	
	public boolean updateRecord(final SQLiteDatabase db, final long dayId, final int newTime) {
		final ContentValues values = new ContentValues();
		values.put(COL_DATE, dayId);
		values.put(COL_FLEX_TIME, newTime);
		
		return updateRecord(db, dayId, values);
	}
	
	public boolean updateRecord(final SQLiteDatabase db, final WeekBean week) {
		fillContentValues(week);
		return updateRecord(db, week.date, mTempContentValues);
	}
	
	public boolean delete(final SQLiteDatabase db, final long date) {
		return deleteWhere(db, COL_DATE + "=" + date) != 0;
	}
	
	public boolean exists(final SQLiteDatabase db, final long date) {
		final Cursor result = fetchWhere(db, COL_DATE + "=" + date);
		final boolean exists = result.getCount() > 0;
		result.close();
		return exists;
	}

	public void fetchLastFlexTime(final SQLiteDatabase db, final long dayId, final WeekBean weekData) {
		final String whereClause = COL_DATE + "<=" + dayId + " and " + COL_FLEX_TIME + " is not null";
		final String orderClause = COL_DATE + " desc LIMIT 1";
		final Cursor cursor = fetchWhere(db, whereClause, orderClause);
		
		weekData.date = dayId;
		if (cursor.getCount() > 0) {
			weekData.flexTime = cursor.getInt(COL_FLEX_TIME_INDEX);
			weekData.isValid = true;
		} else {
			weekData.flexTime = TimeUtils.UNKNOWN_TIME;
			weekData.isValid = false;
		}
		cursor.close();
	}
	
	public void fetchFirstFlexTime(final SQLiteDatabase db, final WeekBean weekData) {
		final String whereClause = COL_FLEX_TIME + " is not null";
		final String orderClause = COL_DATE + " asc LIMIT 1";
		final Cursor cursor = fetchWhere(db, whereClause, orderClause);
		
		if (cursor.getCount() > 0) {
			weekData.date = cursor.getInt(COL_DATE_INDEX);
			weekData.flexTime = cursor.getInt(COL_FLEX_TIME_INDEX);
			weekData.isValid = true;
		} else {
			weekData.date = 0;
			weekData.flexTime = TimeUtils.UNKNOWN_TIME;
			weekData.isValid = false;
		}
		cursor.close();
	}

	public int fetchFlexTime(final SQLiteDatabase db, final long dayId) {
		final Cursor cursor = fetch(db, dayId);
		
		int flex = TimeUtils.UNKNOWN_TIME;
		if (cursor.getCount() > 0) {
			flex = cursor.getInt(COL_FLEX_TIME_INDEX);
		}
		cursor.close();
		return flex;
	}
	
	public List<WeekBean> getWeeksBetween(final SQLiteDatabase db, final long firstDay, final long lastDay) {
		return getWeeksBetween(db, firstDay, lastDay, null);
	}
	
	/**
	 * Attention ! Cette m�thode remplit le champ isValid du WeekBean !
	 * @param firstDay
	 * @param lastDay
	 * @param listToFill
	 * @return
	 */
	public List<WeekBean> getWeeksBetween(final SQLiteDatabase db, final long firstDay, final long lastDay, final List<WeekBean> listToFill) {
		final String whereClause = COL_DATE + ">=" + firstDay + " and " + COL_DATE + "<=" + lastDay;
		final String orderClause = COL_DATE + " asc";
		final Cursor cursorWeeks = fetchWhere(db, whereClause, orderClause);
		final int nbWeeks = cursorWeeks.getCount();
		
		List<WeekBean> weeks = listToFill;
		if (weeks == null) {
			weeks = new ArrayList<WeekBean>(nbWeeks);
		}
		if (nbWeeks > 0) {
			WeekBean week = null;
			do {
				week = new WeekBean();
				week.isValid = fillBean(cursorWeeks, week);
				weeks.add(week);
			} while (cursorWeeks.moveToNext());
		}
		cursorWeeks.close();
		return weeks;
	}
	
	public static boolean fillBean(final Cursor data, final WeekBean beanToFill) {
		// Comme il y a des donn�es en base, on les lit
		if (data.getCount() > 0) {
			beanToFill.date = data.getLong(COL_DATE_INDEX);
			beanToFill.flexTime = data.getInt(COL_FLEX_TIME_INDEX);
			return true;
		}
		return false;
	}

	private void fillContentValues(final WeekBean week) {
		mTempContentValues.clear();
		mTempContentValues.put(COL_DATE, week.date);
		mTempContentValues.put(COL_FLEX_TIME, week.flexTime);
	}
}
