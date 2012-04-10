package fr.redmoon.tictac.gui;

import java.util.Iterator;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import fr.redmoon.tictac.bus.DayTypes;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.WeekBean;
import fr.redmoon.tictac.db.DbAdapter;

public class DbInserterThread extends Thread {
	public final static int STATE_DONE = 0;
	public final static int STATE_IMPORTING_DAYS = 1;
	public final static int STATE_IMPORTING_WEEKS = 2;
	public final static int STATE_CANCEL = 3;
    
	private final DbAdapter mDb;
    private Handler mHandler;
    private int mState;
    
    private final List<DayBean> mDays;
    private final Iterator<DayBean> mDayIterator;
    private int nbDaysUpdated;
    private int nbDaysCreated;
    private final int nbDaysToProcess;

    private final List<WeekBean> mWeeks;
    private final Iterator<WeekBean> mWeekIterator;
    private int nbWeeksProcessed;
    private final int nbWeeksToProcess;
   
    public DbInserterThread(final List<DayBean> days, final List<WeekBean> weeks, final DbAdapter db) {
        mDb = db;

        mState = STATE_DONE;
        
        mDays = days;
        mDayIterator = days.iterator();
        nbDaysUpdated = 0;
        nbDaysCreated = 0;
        nbDaysToProcess = mDays.size();

        mWeeks = weeks;
        mWeekIterator = mWeeks.iterator();
        nbWeeksProcessed = 0;
        nbWeeksToProcess = mWeeks.size();
    }
   
    public void run() {
        mState = STATE_IMPORTING_DAYS;
        while (mState != STATE_DONE) {
        	switch (mState) {
        	case STATE_IMPORTING_DAYS:
        		importNextDay();
        		break;
        	case STATE_IMPORTING_WEEKS:
        		importNextWeek();
        		break;
        	case STATE_CANCEL:
        		cancelImport();
        		break;
        	}
        }
    }
    
    private void cancelImport() {
    	if (mHandler != null) {
            final Message msg = mHandler.obtainMessage();
            msg.what = STATE_CANCEL;
            msg.arg1 = nbDaysCreated;
            msg.arg2 = nbDaysUpdated;
            mHandler.sendMessage(msg);
		}
    	
    	// On arrête le thread
		mState = STATE_DONE;
	}

	private void importNextWeek() {
    	if (mWeekIterator.hasNext()) {
    		// Il reste des semaines à écrire : on écrit la suivante
    		// Notez qu'on ne tient pas le compte des semaines ajoutées.
    		// C'est transparent pour l'utilisateur.
        	final WeekBean week = mWeekIterator.next();
			if (mDb.isWeekExisting(week.date)) {
				mDb.updateWeek(week);
			} else {
				mDb.createWeek(week);
			}
			nbWeeksProcessed++;
			
			// Mise à jour de la barre de progression
			if (mHandler != null) {
	            final Message msg = mHandler.obtainMessage();
	            msg.what = STATE_IMPORTING_WEEKS;
	            msg.arg1 = (int)( (nbDaysCreated + nbDaysUpdated + nbWeeksProcessed) / (double)(nbDaysToProcess + nbWeeksToProcess) * 100.0 );
	            msg.arg2 = (int)( nbWeeksProcessed / (double)nbWeeksToProcess * 100.0 );
	            mHandler.sendMessage(msg);
			}
    	} else {
    		// On a lu tous les jours et toutes les semaines : on envoit
    		// un message qui va mettre à jour la barre de progression
    		// et afficher le nombre de jours créés et mis à jour.
    		if (mHandler != null) {
	            final Message msg = mHandler.obtainMessage();
	            msg.what = STATE_DONE;
	            msg.arg1 = nbDaysCreated;
	            msg.arg2 = nbDaysUpdated;
	            mHandler.sendMessage(msg);
    		}
    		
    		// On arrête le thread
			mState = STATE_DONE;
    	}
	}

	private void importNextDay() {
    	if (mDayIterator.hasNext()) {
    		// Il reste des jours à écrire : on écrit le suivant
        	final DayBean day = mDayIterator.next();
			if (mDb.isDayExisting(day.date)) {
				mDb.updateDay(day);
				if (day.isValid) {
					nbDaysUpdated++;
				}
			} else {
				// Le jour sera créé. On vient d'ajouter un pointage, donc c'est
	    		// un jour de type "normal"
	    		day.type = DayTypes.normal.ordinal();
	    		
				mDb.createDay(day);
				if (day.isValid) {
					nbDaysCreated++;
				}
			}
			
			// Mise à jour de la barre de progression
			if (mHandler != null) {
	            final Message msg = mHandler.obtainMessage();
	            msg.what = STATE_IMPORTING_DAYS;
	            msg.arg1 = (int)( (nbDaysCreated + nbDaysUpdated + nbWeeksProcessed) / (double)(nbDaysToProcess + nbWeeksToProcess) * 100.0 );
	            msg.arg2 = (int)( (nbDaysCreated + nbDaysUpdated) / (double)nbDaysToProcess * 100.0 );
	            mHandler.sendMessage(msg);
			}
    	} else {
    		// On démarre l'import des semaines.
    		mState = STATE_IMPORTING_DAYS;
    		
    		// On commence dès maintenant car après tout si on est
    		// ici c'est qu'on n'a rien fait à ce tour.
    		importNextWeek();
    	}
	}

    public void setState(int state) {
        mState = state;
    }

	public void setHandler(ProgressDialogHandler progressHandler) {
		mHandler = progressHandler;
	}

	public boolean isRunning() {
		return mState == STATE_IMPORTING_DAYS
			|| mState == STATE_IMPORTING_WEEKS;
	}
}