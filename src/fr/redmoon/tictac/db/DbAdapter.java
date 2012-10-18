package fr.redmoon.tictac.db;

import java.util.List;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.WeekBean;

public class DbAdapter {
	private static final String DATABASE_NAME = "TicTac.db";
	private static final int DATABASE_VERSION = 5;
	
	/**
	 * On dispose de 2 instances car le widget doit pouvoir acc�der � la base.
	 * Or, lorsqu'il est lanc�, il n'y a pas forc�ment de contexte li� �
	 * l'application principale. Il lui faut donc sa propre instance avec son
	 * propre contexte.
	 */
	private static final DbAdapter INSTANCE = new DbAdapter();
	
	private Context mCtx;
	private DatabaseHelper mDbHelper;

	private DaysTableHelper days;
	private CheckingsTableHelper checkings;
	private WeeksTableHelper weeks;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		final private DbAdapter dbAdapter;
		
		DatabaseHelper(final Context context, final DbAdapter dbAdapter) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.dbAdapter = dbAdapter;
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
        	dbAdapter.createTables(db);
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
//    		// Il faut tout casser et tout recr�er
//            Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
//            dbAdapter.dropTables(db);
//            onCreate(db);
        }
    }
	
	public static DbAdapter getInstance(final Context ctx) {
		INSTANCE.mCtx = ctx;
		return INSTANCE;
	}
	
    private DbAdapter() {
    	// Cr�e les objets permettant la manipulation des tables
    	// Ils doivent �tre cr��s AVANT le DatabaseHelper, car
    	// celui-ci les utilise pour conna�tre et cr�er la structure
    	// des tables.
    	weeks = new WeeksTableHelper();
        days = new DaysTableHelper();
        checkings = new CheckingsTableHelper();
    }
	
	/**
     * Ouvre la BD. Si elle ne peut pas �tre ouverte, on essaie d'en cr�er une
     * nouvelle. Si elle ne peut pas �tre cr��e, on l�ve une exception pour
     * signaler l'�chec.
     * 
     * @return this (r�f�rence vers l'objet lui-m�me, de fa�on � permettre le
     * 			chainage d'un appel d'initialisation
     * @throws SQLException si la DB n'a pu �tre ni ouverte ni cr��e
     */
    public DbAdapter openDatabase() throws SQLException {
    	// Cr�e les objets permettant la manipulation de la base
        mDbHelper = new DatabaseHelper(mCtx, this);
        return this;
    }
    
    /**
     * Ferme la base de donn�es.
     */
    public void closeDatabase() {
        mDbHelper.close();
    }

    /**
     * Cr�e l'ensemble des tables de la base.
     * @param db
     */
	public void createTables(final SQLiteDatabase db) {
		days.createTable(db);
		checkings.createTable(db);
		weeks.createTable(db);
	}
	
	/**
	 * Supprime l'ensemble des tables de la base.
	 * @param db
	 */
	public void dropTables(final SQLiteDatabase db) {
    	days.dropTable(db);
    	checkings.dropTable(db);
    	weeks.dropTable(db);
	}
	
	public void createDay(final DayBean day) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		day.isValid = days.createRecord(db, day) && checkings.createRecords(db, day);
	}
	
	public boolean createChecking(final long dayId, final int time) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		// Cr�ation du pointage
		return checkings.createRecord(db, dayId, time);
	}
	
	public boolean createFlexTime(final long dayId, final int time) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return weeks.createRecord(db, dayId, time);
	}

	public void fetchDay(final long id, final DayBean beanToFill) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		days.getDayById(db, id, beanToFill);
		if (beanToFill.isValid) {
			checkings.fetchCheckings(db, beanToFill);
		} else {
			beanToFill.emptyDayData();
		}
	}
	
	public void fetchCheckings(final long date, final List<Integer> listToFill) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		checkings.fetchCheckings(db, date, listToFill);
	}
	
	public void fetchWeeks(final long firstDay, final long lastDay, final List<WeekBean> listToFill) {
		listToFill.clear();
		
		// R�cup�ration des jours en bases
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		weeks.getWeeksBetween(db, firstDay, lastDay, listToFill);
		
		// R�cup�ration des pointages des jours
		for (WeekBean week : listToFill) {
			if (!week.isValid) {
				listToFill.remove(week);
			}
		}
	}
	
	public int fetchFlexTime(final long dayId) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return weeks.fetchFlexTime(db, dayId);
	}
	
	/**
	 * Retourne l'HV au jour s�lectionn�, ou le plus r�cent avant ce jour
	 * si aucun n'existe pour le jour indiqu�
	 * @param dayId
	 */
	public void fetchLastFlexTime(final long dayId, final WeekBean weekData) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		weeks.fetchLastFlexTime(db, dayId, weekData);
	}
	
	/**
	 * Retourne le premier HV enregistr� dans la base
	 */
	public void fetchLastFlexTime(final WeekBean weekData) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		weeks.fetchFirstFlexTime(db, weekData);
	}
	
	public void fetchPreviousDay(final long date, final DayBean beanToFill) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		beanToFill.isValid = days.getPreviousDay(db, date, beanToFill);
		if (beanToFill.isValid) {
			checkings.fetchCheckings(db, beanToFill);
		}
	}
	
	public long fetchPreviousDay(final long date) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return days.getPreviousDay(db, date);
	}
	
	public void fetchNextDay(final long date, final DayBean beanToFill) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		beanToFill.isValid = days.getNextDay(db, date, beanToFill);
		if (beanToFill.isValid) {
			checkings.fetchCheckings(db, beanToFill);
		}
	}
	
	public void fetchDays(final long firstDay, final long lastDay, final List<DayBean> listToFill) {
		listToFill.clear();
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
		// R�cup�ration des jours en bases
		days.getDaysBetween(db, firstDay, lastDay, listToFill);
		
		// R�cup�ration des pointages des jours
		for (DayBean day : listToFill) {
			if (day.isValid) {
				checkings.fetchCheckings(db, day);
			} else {
				listToFill.remove(day);
			}
		}
	}
	
	public int countDaysBetween(final long firstDay, final long lastDay) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		
		// R�cup�ration du nombre de jours en bases
		return days.getDaysCountBetween(db, firstDay, lastDay);
	}
	
	public void updateDay(final DayBean day) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		day.isValid = days.updateRecord(db, day) && checkings.updateRecords(db, day);
	}
	
	public boolean updateDayExtra(final long date, final int extra) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// Cr�ation du jour s'il n'existe pas
		if (!days.exists(db, date)) {
			final DayBean day = new DayBean();
			day.date = date;
			day.extra = extra;
			return days.createRecord(db, day);
		} else {
			return days.updateExtraTime(db, date, extra);
		}
	}
	
	public boolean updateDayType(final long date, final String typeMorning, final String typeAfternoon) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		if (!days.exists(db, date)) {
			final DayBean day = new DayBean();
			day.date = date;
			day.typeMorning = typeMorning;
			day.typeAfternoon = typeAfternoon;
			return days.createRecord(db, day);
		} else {
			return days.updateType(db, date, typeMorning, typeAfternoon);
		}
	}
	
	public int updateDayType(final String oldType, final String newType) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return days.updateType(db, oldType, newType);
	}
	
	public boolean updateDayNote(final long date, final String note) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		if (!days.exists(db, date)) {
			final DayBean day = new DayBean();
			day.date = date;
			day.note = note;
			return days.createRecord(db, day);
		} else {
			return days.updateNote(db, date, note);
		}
	}
	
	public boolean updateChecking(final long date, final int oldTime, final int newTime) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return checkings.updateRecord(db, date, oldTime, newTime);
	}
	
	public boolean updateFlexTime(final long date, final int flexTime) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		if (!weeks.exists(db, date)) {
			return createFlexTime(date, flexTime);
		} else {
			return weeks.updateRecord(db, date, flexTime);
		}
	}
	
	public boolean deleteChecking(final long date, final int checking) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return checkings.delete(db, date, checking);
	}
	
	public boolean deleteFlexTime(final long date) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return weeks.delete(db, date);
	}

	public boolean deleteDay(final long dayId) {
		// DBG Ne faudrait-il pas aussi supprimer les semaines si elles ne contiennent plus de jour ???
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return days.delete(db, dayId)
			&& checkings.delete(db, dayId);
	}
	
	public boolean deleteDays(final long firstDay, final long lastDay) {
		// DBG Ne faudrait-il pas aussi supprimer les semaines si elles ne contiennent plus de jour ???
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return days.delete(db, firstDay, lastDay) && checkings.delete(db, firstDay, lastDay);
	}

	public long getLastDayId() {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return days.getCount(db);
	}

	public boolean isDayExisting(final long date) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return days.exists(db, date);
	}
	
	public boolean isCheckingExisting(final long date, final int time) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return checkings.exists(db, date, time);
	}
	
	public boolean isWeekExisting(final long date) {
		final SQLiteDatabase db = mDbHelper.getReadableDatabase();
		return weeks.exists(db, date);
	}

	public void updateWeek(final WeekBean week) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		week.isValid = weeks.updateRecord(db, week);
	}

	public void createWeek(WeekBean week) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		week.isValid = weeks.createRecord(db, week);
	}
}
