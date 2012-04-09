package fr.redmoon.tictac.db;

import java.util.List;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.WeekBean;

public class DbAdapter {
	private static final String DATABASE_NAME = "TicTac.db";
	private static final int DATABASE_VERSION = 3;
	private static final String LOG_TAG = "TicTac (DB)";
	
	private final Context mCtx;
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

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
    		// Il faut tout casser et tout recréer
            Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            dbAdapter.dropTables(db);
            onCreate(db);
        }
    }
	
	/**
     * Constructeur : prend le contexte pour permettre la création
     * ou l'ouverture de la base de données. 
     * 
     * @param ctx Le Context dans lequel travailler
     */
    public DbAdapter(final Context ctx) {
        this.mCtx = ctx;
    }
	
	/**
     * Ouvre la BD. Si elle ne peut pas être ouverte, on essaie d'en créer une
     * nouvelle. Si elle ne peut pas être créée, on lève une exception pour
     * signaler l'échec.
     * 
     * @return this (référence vers l'objet lui-même, de façon à permettre le
     * 			chainage d'un appel d'initialisation
     * @throws SQLException si la DB n'a pu être ni ouverte ni créée
     */
    public DbAdapter openDatabase() throws SQLException {
    	// Crée les objets permettant la manipulation des tables
    	// Ils doivent être créés AVANT le DatabaseHelper, car
    	// celui-ci les utilise pour connaître et créer la structure
    	// des tables.
    	weeks = new WeeksTableHelper();
        days = new DaysTableHelper();
        checkings = new CheckingsTableHelper();
    	
    	// Crée les objets permettant la manipulation de la base
        mDbHelper = new DatabaseHelper(mCtx, this);
        mDb = mDbHelper.getWritableDatabase();
        
        // On dispose à présent d'un accès à la base. On le passe
        // aux objets manipulant les tables.
        weeks.setDB(mDb);
        days.setDB(mDb);
        checkings.setDB(mDb);
        
        return this;
    }
    
    /**
     * Ferme la base de données.
     */
    public void closeDatabase() {
        mDbHelper.close();
    }

    /**
     * Crée l'ensemble des tables de la base.
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
		day.isValid = days.createRecord(day) && checkings.createRecords(day);
	}
	
	public boolean createChecking(final long dayId, final int time) {
		// Création du jour s'il n'existe pas
		if (!days.exists(dayId)) {
			final DayBean day = new DayBean();
			day.date = dayId;
			days.createRecord(day);
		}
		
		// Création du pointage
		return checkings.createRecord(dayId, time);
	}
	
	public boolean createFlexTime(final long dayId, final int time) {
		return weeks.createRecord(dayId, time);
	}

	public void fetchDay(final long id, final DayBean beanToFill) {
		beanToFill.isValid = days.getDayById(id, beanToFill);
		if (beanToFill.isValid) {
			checkings.fetchCheckings(beanToFill);
		} else {
			beanToFill.emptyDayData();
		}
	}
	
	public void fetchWeeks(final long firstDay, final long lastDay, final List<WeekBean> listToFill) {
		listToFill.clear();
		
		// Récupération des jours en bases
		weeks.getWeeksBetween(firstDay, lastDay, listToFill);
		
		// Récupération des pointages des jours
		for (WeekBean week : listToFill) {
			if (!week.isValid) {
				listToFill.remove(week);
			}
		}
	}
	
	public int fetchFlexTime(final long dayId) {
		return weeks.fetchFlexTime(dayId);
	}
	
	/**
	 * Retourne l'HV au jour sélectionné, ou le plus récent avant ce jour
	 * si aucun n'existe pour le jour indiqué
	 * @param dayId
	 */
	public void fetchLastFlexTime(final long dayId, final WeekBean weekData) {
		weeks.fetchLastFlexTime(dayId, weekData);
	}
	
	public void fetchPreviousDay(final long date, final DayBean beanToFill) {
		beanToFill.isValid = days.getPreviousDay(date, beanToFill);
		if (beanToFill.isValid) {
			checkings.fetchCheckings(beanToFill);
		}
	}
	
	public long fetchPreviousDay(final long date) {
		return days.getPreviousDay(date);
	}
	
	public void fetchNextDay(final long date, final DayBean beanToFill) {
		beanToFill.isValid = days.getNextDay(date, beanToFill);
		if (beanToFill.isValid) {
			checkings.fetchCheckings(beanToFill);
		}
	}
	
	public void fetchDays(final long firstDay, final long lastDay, final List<DayBean> listToFill) {
		listToFill.clear();
		
		// Récupération des jours en bases
		days.getDaysBetween(firstDay, lastDay, listToFill);
		
		// Récupération des pointages des jours
		for (DayBean day : listToFill) {
			if (day.isValid) {
				checkings.fetchCheckings(day);
			} else {
				listToFill.remove(day);
			}
		}
	}
	
	public void updateDay(final DayBean day) {
		day.isValid = days.updateRecord(day) && checkings.updateRecords(day);
	}
	
	public boolean updateDayExtra(final long date, final int extra) {
		// Création du jour s'il n'existe pas
		if (!days.exists(date)) {
			final DayBean day = new DayBean();
			day.date = date;
			day.extra = extra;
			return days.createRecord(day);
		} else {
			return days.updateExtraTime(date, extra);
		}
	}
	
	public boolean updateDayType(final long date, final int type) {
		if (!days.exists(date)) {
			final DayBean day = new DayBean();
			day.date = date;
			day.type = type;
			return days.createRecord(day);
		} else {
			return days.updateType(date, type);
		}
	}
	
	public boolean updateDayNote(final long date, final String note) {
		if (!days.exists(date)) {
			final DayBean day = new DayBean();
			day.date = date;
			day.note = note;
			return days.createRecord(day);
		} else {
			return days.updateNote(date, note);
		}
	}
	
	public boolean updateChecking(final long date, final int oldTime, final int newTime) {
		return checkings.updateRecord(date, oldTime, newTime);
	}
	
	public boolean updateFlexTime(final long date, final int flexTime) {
		if (!weeks.exists(date)) {
			return createFlexTime(date, flexTime);
		} else {
			return weeks.updateRecord(date, flexTime);
		}
	}
	
	public boolean deleteChecking(final long date, final int checking) {
		return checkings.delete(date, checking);
	}
	
	public boolean deleteFlexTime(final long date) {
		return weeks.delete(date);
	}

	public boolean deleteDay(final long dayId) {
		return days.delete(dayId)
			&& checkings.delete(dayId);
	}

	public long getLastDayId() {
		return days.getCount();
	}

	public boolean isDayExisting(final long date) {
		return days.exists(date);
	}
	
	public boolean isCheckingExisting(final long date, final int time) {
		return checkings.exists(date, time);
	}
	
	public boolean isWeekExisting(final long date) {
		return weeks.exists(date);
	}

	public void updateWeek(final WeekBean week) {
		week.isValid = weeks.updateRecord(week);
	}

	public void createWeek(WeekBean week) {
		week.isValid = weeks.createRecord(week);
	}
}
