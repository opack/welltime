package fr.redmoon.tictac.db;

import java.util.List;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import fr.redmoon.tictac.bus.bean.DayBean;

public class DbAdapter {
	private static final String DATABASE_NAME = "TicTac.db";
	private static final int DATABASE_VERSION = 2;
	private static final String LOG_TAG = "TicTac (DB)";
	
	private final Context mCtx;
	private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

	private DaysTableHelper days;
	private CheckingsTableHelper checkings;
	
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
            Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            dbAdapter.dropTables(db);
            onCreate(db);
        }
    }
	
	/**
     * Constructeur : prend le contexte pour permettre la cr�ation
     * ou l'ouverture de la base de donn�es. 
     * 
     * @param ctx Le Context dans lequel travailler
     */
    public DbAdapter(final Context ctx) {
        this.mCtx = ctx;
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
    	// Cr�e les objets permettant la manipulation des tables
    	// Ils doivent �tre cr��s AVANT le DatabaseHelper, car
    	// celui-ci les utilise pour conna�tre et cr�er la structure
    	// des tables.
        days = new DaysTableHelper();
        checkings = new CheckingsTableHelper();
    	
    	// Cr�e les objets permettant la manipulation de la base
        mDbHelper = new DatabaseHelper(mCtx, this);
        mDb = mDbHelper.getWritableDatabase();
        
        // On dispose � pr�sent d'un acc�s � la base. On le passe
        // aux objets manipulant les tables.
        days.setDB(mDb);
        checkings.setDB(mDb);
        
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
	}
	
	/**
	 * Supprime l'ensemble des tables de la base.
	 * @param db
	 */
	public void dropTables(final SQLiteDatabase db) {
    	days.dropTable(db);
    	checkings.dropTable(db);
	}
	
	public void createDay(final DayBean day) {
		day.isValid = days.createRecord(day) && checkings.createRecords(day);
	}
	
	public boolean createChecking(final long dayId, final int time) {
		// Cr�ation du jour s'il n'existe pas
		if (!days.exists(dayId)) {
			final DayBean day = new DayBean();
			day.date = dayId;
			days.createRecord(day);
		}
		
		// Cr�ation du pointage
		return checkings.createRecord(dayId, time);
	}

	public void fetchDay(final long id, final DayBean beanToFill) {
		beanToFill.isValid = days.getDayById(id, beanToFill);
		if (beanToFill.isValid) {
			checkings.fetchCheckings(beanToFill);
		} else {
			beanToFill.emptyDayData();
		}
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
		
		// R�cup�ration des jours en bases
		days.getDaysBetween(firstDay, lastDay, listToFill);
		
		// R�cup�ration des pointages des jours
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
		// Cr�ation du jour s'il n'existe pas
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
	
	public boolean deleteChecking(final long date, final int checking) {
		return checkings.delete(date, checking);
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
}
