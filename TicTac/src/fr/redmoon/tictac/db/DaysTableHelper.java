package fr.redmoon.tictac.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fr.redmoon.tictac.bus.DayTypes;
import fr.redmoon.tictac.bus.bean.DayBean;

public class DaysTableHelper extends TableHelper {

	public static final String TABLE_NAME = "days";

	// Date du jour, entier au format yyyymmdd 
	private static final String COL_DATE = "date";
	private static final int COL_DATE_INDEX = 0;
	
	// Type de la matinée : normale, CP, RTT...
	private static final String COL_TYPE_MORNING = "typeMorning";
	private static final int COL_TYPE_MORNING_INDEX = 1;
	
	// Type de la matinée : normale, CP, RTT...
	private static final String COL_TYPE_AFTERNOON = "typeAfternoon";
	private static final int COL_TYPE_AFTERNOON_INDEX = 2;
	
	// Temps supplémentaire à ajouter au jour, en minutes.
	private static final String COL_EXTRA = "extra";
	private static final int COL_EXTRA_INDEX = 3;
	
	// Note textuelle associée au jour
	private static final String COL_NOTE = "note";
	private static final int COL_NOTE_INDEX = 4;
	
	private final ContentValues mTempContentValues = new ContentValues();
	
	public DaysTableHelper() {
		super(
			TABLE_NAME,
			new String[]{
				COL_DATE,
				COL_TYPE_MORNING,
				COL_TYPE_AFTERNOON,
				COL_EXTRA,
				COL_NOTE},
			new String[]{
				SQLiteUtils.DATATYPE_INTEGER,
				SQLiteUtils.DATATYPE_INTEGER,
				SQLiteUtils.DATATYPE_INTEGER,
				SQLiteUtils.DATATYPE_INTEGER,
				SQLiteUtils.DATATYPE_TEXT
			},
			new String[]{
				SQLiteUtils.CONSTRAINT_PRIMARY_KEY,
				SQLiteUtils.CONSTRAINT_DEFAULT + DayTypes.normal.ordinal(),
				SQLiteUtils.CONSTRAINT_DEFAULT + DayTypes.normal.ordinal(),
				SQLiteUtils.CONSTRAINT_DEFAULT + "0",
				SQLiteUtils.CONSTRAINT_NONE
			});
	}
	
	public boolean getDayById(final SQLiteDatabase db, final long id, final DayBean beanToFill) {
		final Cursor day = fetch(db, id);
		beanToFill.isValid = fillBean(day, beanToFill);
		if (!beanToFill.isValid) {
			// Le jour n'est pas valide mais on va quand même conserver
			// la date qui n'existe pas en base.
			beanToFill.date = id;
		}
		day.close();
		return beanToFill.isValid;
	}
	
	public boolean getNextDay(final SQLiteDatabase db, final long date, final DayBean beanToFill) {
		final String whereClause = COL_DATE + ">" + date;
		final String orderClause = COL_DATE + " asc LIMIT 1";
		final Cursor day = fetchWhere(db, whereClause, orderClause);
		final boolean exists = fillBean(day, beanToFill);
		day.close();
		return exists;
	}
	
	public boolean getPreviousDay(final SQLiteDatabase db, final long date, final DayBean beanToFill) {
		final String whereClause = COL_DATE + "<" + date;
		final String orderClause = COL_DATE + " desc LIMIT 1";
		final Cursor day = fetchWhere(db, whereClause, orderClause);
		final boolean exists = fillBean(day, beanToFill);
		day.close();
		return exists;
	}
	
	public long getPreviousDay(final SQLiteDatabase db, final long date) {
		final String whereClause = COL_DATE + "<" + date;
		final String orderClause = COL_DATE + " desc LIMIT 1";
		final Cursor cursor = fetchWhere(db, whereClause, orderClause);
		
		long previousDayId = -1;
		if (cursor.getCount() > 0) {
			previousDayId = cursor.getLong(COL_DATE_INDEX);
		}
		cursor.close();
		return previousDayId;
	}

	public List<DayBean> getDaysBetween(final SQLiteDatabase db, final long firstDay, final long lastDay) {
		return getDaysBetween(db, firstDay, lastDay, null);
	}
	
	/**
	 * Attention ! Cette méthode remplit le champ isValid du DayBean !
	 * @param firstDay
	 * @param lastDay
	 * @param listToFill
	 * @return
	 */
	public List<DayBean> getDaysBetween(final SQLiteDatabase db, final long firstDay, final long lastDay, final List<DayBean> listToFill) {
		final String whereClause = COL_DATE + ">=" + firstDay + " and " + COL_DATE + "<=" + lastDay;
		final String orderClause = COL_DATE + " asc";
		final Cursor cursorDays = fetchWhere(db, whereClause, orderClause);
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
	
	public int getDaysCountBetween(final SQLiteDatabase db, final long firstDay, final long lastDay) {
		final String whereClause = COL_DATE + ">=" + firstDay + " and " + COL_DATE + "<=" + lastDay;
		final String orderClause = COL_DATE + " asc";
		final Cursor cursorDays = fetchIdsWhere(db, whereClause, orderClause);
		final int nbDays = cursorDays.getCount();
		cursorDays.close();
		return nbDays;
	}
	
	public boolean createRecord(final SQLiteDatabase db, final DayBean day) {
		fillContentValues(day);
		return createRecord(db, mTempContentValues) != -1;
	}
	
	public boolean updateRecord(final SQLiteDatabase db, final DayBean day) {
		fillContentValues(day);
		return updateRecord(db, day.date, mTempContentValues);
	}
	
	public boolean updateExtraTime(final SQLiteDatabase db, final long dayId, final long time) {
		mTempContentValues.clear();
		mTempContentValues.put(COL_EXTRA, time);
		return updateRecord(db, dayId, mTempContentValues);
	}
	
	public boolean updateType(final SQLiteDatabase db, final long dayId, final int typeMorning, final int typeAfternoon) {
		mTempContentValues.clear();
		mTempContentValues.put(COL_TYPE_MORNING, typeMorning);
		mTempContentValues.put(COL_TYPE_AFTERNOON, typeAfternoon);
		return updateRecord(db, dayId, mTempContentValues);
	}
	
	public boolean updateNote(final SQLiteDatabase db, final long dayId, final String note) {
		mTempContentValues.clear();
		mTempContentValues.put(COL_NOTE, note);
		return updateRecord(db, dayId, mTempContentValues);
	}
	
	public static boolean fillBean(final Cursor data, final DayBean beanToFill) {
		// Comme il y a des données en base, on les lit
		if (data.getCount() > 0) {
			beanToFill.date = data.getLong(COL_DATE_INDEX);
			beanToFill.typeMorning = data.getInt(COL_TYPE_MORNING_INDEX);
			beanToFill.typeAfternoon = data.getInt(COL_TYPE_AFTERNOON_INDEX);
			beanToFill.extra = data.getInt(COL_EXTRA_INDEX);
			beanToFill.note = data.getString(COL_NOTE_INDEX);
			return true;
		} else {
			// Le jour n'est pas renseigné en base
			beanToFill.typeMorning = DayTypes.not_worked.ordinal();
			beanToFill.typeAfternoon = DayTypes.not_worked.ordinal();
		}
		return false;
	}

	private void fillContentValues(final DayBean day) {
		mTempContentValues.clear();
		mTempContentValues.put(COL_DATE, day.date);
		mTempContentValues.put(COL_TYPE_MORNING, day.typeMorning);
		mTempContentValues.put(COL_TYPE_AFTERNOON, day.typeAfternoon);
		mTempContentValues.put(COL_EXTRA, day.extra);
		mTempContentValues.put(COL_NOTE, day.note);
	}
}
