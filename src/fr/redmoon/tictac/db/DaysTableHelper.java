package fr.redmoon.tictac.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import fr.redmoon.tictac.bus.DayTypes;
import fr.redmoon.tictac.bus.bean.DayBean;

public class DaysTableHelper extends TableHelper {

	public static final String TABLE_NAME = "days";

	// Date du jour, entier au format yyyymmdd 
	private static final String COL_DATE = "date";
	private static final int COL_DATE_INDEX = 0;
	
	// Type de la journée : normale, CP, RTT...
	private static final String COL_TYPE = "type";
	private static final int COL_TYPE_INDEX = 1;
	
	// Temps supplémentaire à ajouter au jour, en minutes.
	private static final String COL_EXTRA = "extra";
	private static final int COL_EXTRA_INDEX = 2;
	
	// Note textuelle associée au jour
	private static final String COL_NOTE = "note";
	private static final int COL_NOTE_INDEX = 3;
	
	private final ContentValues mTempContentValues = new ContentValues();
	
	public DaysTableHelper() {
		super(
			TABLE_NAME,
			new String[]{
				COL_DATE,
				COL_TYPE,
				COL_EXTRA,
				COL_NOTE},
			new String[]{
				SQLiteUtils.DATATYPE_INTEGER,
				SQLiteUtils.DATATYPE_INTEGER,
				SQLiteUtils.DATATYPE_INTEGER,
				SQLiteUtils.DATATYPE_TEXT
			},
			new String[]{
				SQLiteUtils.CONSTRAINT_PRIMARY_KEY,
				SQLiteUtils.CONSTRAINT_DEFAULT + DayTypes.normal.ordinal(),
				SQLiteUtils.CONSTRAINT_DEFAULT + "0",
				SQLiteUtils.CONSTRAINT_NONE
			});
	}
	
	public boolean getDayById(final long id, final DayBean beanToFill) {
		final Cursor day = fetch(id);
		beanToFill.isValid = fillBean(day, beanToFill);
		if (!beanToFill.isValid) {
			// Le jour n'est pas valide mais on va quand même conserver
			// la date qui n'existe pas en base.
			beanToFill.date = id;
		}
		day.close();
		return beanToFill.isValid;
	}
	
	public boolean getNextDay(final long date, final DayBean beanToFill) {
		final String whereClause = COL_DATE + ">" + date;
		final String orderClause = COL_DATE + " asc LIMIT 1";
		final Cursor day = fetchWhere(whereClause, orderClause);
		final boolean exists = fillBean(day, beanToFill);
		day.close();
		return exists;
	}
	
	public boolean getPreviousDay(final long date, final DayBean beanToFill) {
		final String whereClause = COL_DATE + "<" + date;
		final String orderClause = COL_DATE + " desc LIMIT 1";
		final Cursor day = fetchWhere(whereClause, orderClause);
		final boolean exists = fillBean(day, beanToFill);
		day.close();
		return exists;
	}
	
	public long getPreviousDay(final long date) {
		final String whereClause = COL_DATE + "<" + date;
		final String orderClause = COL_DATE + " desc LIMIT 1";
		final Cursor cursor = fetchWhere(whereClause, orderClause);
		
		long previousDayId = -1;
		if (cursor.getCount() > 0) {
			previousDayId = cursor.getLong(COL_DATE_INDEX);
		}
		cursor.close();
		return previousDayId;
	}

	public List<DayBean> getDaysBetween(final long firstDay, final long lastDay) {
		return getDaysBetween(firstDay, lastDay, null);
	}
	
	/**
	 * Attention ! Cette méthode remplit le champ isValid du DayBean !
	 * @param firstDay
	 * @param lastDay
	 * @param listToFill
	 * @return
	 */
	public List<DayBean> getDaysBetween(final long firstDay, final long lastDay, final List<DayBean> listToFill) {
		final String whereClause = COL_DATE + ">=" + firstDay + " and " + COL_DATE + "<=" + lastDay;
		final String orderClause = COL_DATE + " asc";
		final Cursor cursorDays = fetchWhere(whereClause, orderClause);
		final int nbDays = cursorDays.getCount();
		
		List<DayBean> days = listToFill;
		if (days == null) {
			days = new ArrayList<DayBean>(nbDays);
		}
		if (nbDays > 0) {
			DayBean day = null;
			do {
				day = new DayBean();
				day.isValid = fillBean(cursorDays, day);
				days.add(day);
			} while (cursorDays.moveToNext());
		}
		cursorDays.close();
		return days;
	}
	
	public boolean createRecord(final DayBean day) {
		fillContentValues(day);
		return createRecord(mTempContentValues) != -1;
	}
	
	public boolean updateRecord(final DayBean day) {
		fillContentValues(day);
		return updateRecord(day.date, mTempContentValues);
	}
	
	public boolean updateExtraTime(final long dayId, final long time) {
		mTempContentValues.clear();
		mTempContentValues.put(COL_EXTRA, time);
		return updateRecord(dayId, mTempContentValues);
	}
	
	public boolean updateType(final long dayId, final int type) {
		mTempContentValues.clear();
		mTempContentValues.put(COL_TYPE, type);
		return updateRecord(dayId, mTempContentValues);
	}
	
	public boolean updateNote(final long dayId, final String note) {
		mTempContentValues.clear();
		mTempContentValues.put(COL_NOTE, note);
		return updateRecord(dayId, mTempContentValues);
	}
	
	public static boolean fillBean(final Cursor data, final DayBean beanToFill) {
		// Comme il y a des données en base, on les lit
		if (data.getCount() > 0) {
			beanToFill.date = data.getLong(COL_DATE_INDEX);
			beanToFill.type = data.getInt(COL_TYPE_INDEX);
			beanToFill.extra = data.getInt(COL_EXTRA_INDEX);
			beanToFill.note = data.getString(COL_NOTE_INDEX);
			return true;
		} else {
			// Le jour n'est pas renseigné en base
			beanToFill.type = DayTypes.not_worked.ordinal();
		}
		return false;
	}

	private void fillContentValues(final DayBean day) {
		mTempContentValues.clear();
		mTempContentValues.put(COL_DATE, day.date);
		mTempContentValues.put(COL_TYPE, day.type);
		mTempContentValues.put(COL_EXTRA, day.extra);
		mTempContentValues.put(COL_NOTE, day.note);
	}
}
