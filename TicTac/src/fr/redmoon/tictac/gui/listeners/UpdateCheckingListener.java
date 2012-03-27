package fr.redmoon.tictac.gui.listeners;

import fr.redmoon.tictac.TicTacActivity;
import fr.redmoon.tictac.bus.DateUtils;
import fr.redmoon.tictac.bus.TimeUtils;
import fr.redmoon.tictac.gui.widgets.WidgetProvider;

public class UpdateCheckingListener extends TimeSetListener {
		private int mOldCheckingValue;
		
    	public UpdateCheckingListener(final TicTacActivity activity) {
    		super(activity);
		}
    	
    	public void prepare(final long date, final int checkingToEdit) {
    		super.prepare(date);
    		mOldCheckingValue = checkingToEdit;
    	}

		@Override
    	public void onTimeSet(final int newTime) {
			boolean dbUpdated = false;
			
			if (newTime != 0
			&& !mDb.isCheckingExisting(mDate, newTime)) {
				// Si on a choisit un pointage valide et qu'il n'existe
				// pas encore, on le crée et on supprime l'ancien.
				dbUpdated = mDb.createChecking(mDate, newTime);
			}
			if (mOldCheckingValue != TimeUtils.UNKNOWN_TIME && mOldCheckingValue != newTime) {
				// On souhaite supprimer l'ancien pointage s'il est différent du nouveau
				dbUpdated = mDb.deleteChecking(mDate, mOldCheckingValue);
			}
			
			// Mise à jour de l'affichage
			if (dbUpdated) {
				mActivity.populateView(mDate);
				
				// Mise à jour des widgets
				if (mDate == DateUtils.getCurrentDayId()) {
					WidgetProvider.updateClockinImage(mActivity);
				}
			}
		}
	}