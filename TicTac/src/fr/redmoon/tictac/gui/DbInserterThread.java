package fr.redmoon.tictac.gui;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import fr.redmoon.tictac.bus.bean.DayBean;
import fr.redmoon.tictac.bus.bean.PreferencesBean;
import fr.redmoon.tictac.bus.bean.WeekBean;
import fr.redmoon.tictac.bus.export.tocalendar.CalendarAccess;
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
   
    public DbInserterThread(final Context context, final List<DayBean> days, final List<WeekBean> weeks) {
    	mDb = DbAdapter.getInstance(context);
    	
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
    
    @Override
    public synchronized void start() {
    	mDb.openDatabase();
    	super.start();
    }
    
    private void stopProcess() {
		mDb.closeDatabase();
    	
    	// On arr�te le thread
		mState = STATE_DONE;
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
    	
    	stopProcess();
	}

	private void importNextWeek() {
    	if (mWeekIterator.hasNext()) {
    		// Il reste des semaines � �crire : on �crit la suivante
    		// Notez qu'on ne tient pas le compte des semaines ajout�es.
    		// C'est transparent pour l'utilisateur.
        	final WeekBean week = mWeekIterator.next();
			if (mDb.isWeekExisting(week.date)) {
				mDb.updateWeek(week);
			} else {
				mDb.createWeek(week);
			}
			nbWeeksProcessed++;
			
			// Mise � jour de la barre de progression
			if (mHandler != null) {
	            final Message msg = mHandler.obtainMessage();
	            msg.what = STATE_IMPORTING_WEEKS;
	            msg.arg1 = nbDaysCreated + nbDaysUpdated + nbWeeksProcessed;
	            mHandler.sendMessage(msg);
			}
    	} else {
    		// On a lu tous les jours et toutes les semaines : on envoit
    		// un message qui va mettre � jour la barre de progression
    		// et afficher le nombre de jours cr��s et mis � jour.
    		if (mHandler != null) {
	            final Message msg = mHandler.obtainMessage();
	            msg.what = STATE_DONE;
	            msg.arg1 = nbDaysCreated;
	            msg.arg2 = nbDaysUpdated;
	            mHandler.sendMessage(msg);
    		}
    		
    		// On arr�te le thread
			stopProcess();
    	}
	}

	private void importNextDay() {
    	if (mDayIterator.hasNext()) {
    		// Il reste des jours � �crire : on �crit le suivant
        	final DayBean day = mDayIterator.next();
			if (mDb.isDayExisting(day.date)) {
				mDb.updateDay(day);
				if (day.isValid) {
					nbDaysUpdated++;
				}
			} else {
				mDb.createDay(day);
				if (day.isValid) {
					nbDaysCreated++;
				}
			}
			
			// Ajout des �v�nements dans le calendrier
			if (PreferencesBean.instance.syncCalendar) {
				CalendarAccess.getInstance().createWorkEvents(day.date, day.checkings);
				CalendarAccess.getInstance().createDayTypeEvent(day.date, day.typeMorning, day.typeAfternoon);
			}
			
			// Mise � jour de la barre de progression
			if (mHandler != null) {
	            final Message msg = mHandler.obtainMessage();
	            msg.what = STATE_IMPORTING_DAYS;
	            msg.arg1 = nbDaysCreated + nbDaysUpdated + nbWeeksProcessed;
	            mHandler.sendMessage(msg);
			}
    	} else {
    		// On d�marre l'import des semaines.
    		mState = STATE_IMPORTING_DAYS;
    		
    		// On commence d�s maintenant car apr�s tout si on est
    		// ici c'est qu'on n'a rien fait � ce tour.
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

	public int getMax() {
		return nbDaysToProcess + nbWeeksToProcess;
	}
}